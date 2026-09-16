package com.protosync.app.calendar

import android.util.Base64
import com.protosync.app.util.SyncLog as Log
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.pgp.UnlockedKey
import me.proton.core.crypto.common.pgp.decryptDataOrNull
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.entity.keyholder.KeyHolder
import me.proton.core.key.domain.entity.keyholder.KeyHolderContext
import me.proton.core.key.domain.decryptDataOrNull as decryptDataOrNullKeyHolder
import me.proton.core.key.domain.entity.key.UnlockedPrivateKey
import me.proton.core.key.domain.useKeys
import me.proton.core.network.data.ApiProvider
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.entity.UserAddress
import java.io.ByteArrayInputStream
import java.util.zip.GZIPInputStream

/**
 * Handles calendar event decryption using the Proton E2E chain:
 *  1. User keys → decrypt calendar passphrase KeyPacket → SessionKey
 *  2. SessionKey → decrypt calendar passphrase → plain passphrase
 *  3. Plain passphrase → unlock calendar key → UnlockedKey
 *  4. Calendar UnlockedKey → decrypt event SharedKeyPacket → event SessionKey
 *  5. Event SessionKey → decrypt Type 3 content → gunzip → plaintext VEVENT JSON
 */
class CalendarSession private constructor(
    private val apiProvider: ApiProvider,
    private val userId: UserId,
    private val context: CryptoContext,
    private val user: User,
    private val addresses: List<UserAddress>
) {
    private val TAG = "CalendarSession"

    /**
     * Decrypt all events for a given calendar, unlocking user keys and
     * address keys (calendar member passphrases are encrypted to address keys).
     * Returns a map of event ID → decrypted plaintext content.
     */
    suspend fun decryptEvents(
        calendarId: String,
        events: List<CalendarEventSyncData>
    ): Map<String, String> {
        if (events.isEmpty()) return emptyMap()
        var result: Map<String, String> = emptyMap()
        try {
            // First try user (account) keys, then each address keyring
            val keyHolders: List<KeyHolder> = listOf(user) + addresses
            for (holder in keyHolders) {
                holder.useKeys(context) {
                    val ctx = this.context
                    val resolvedKey = resolveCalendarKey(this, calendarId)
                    if (resolvedKey != null) {
                        val out = mutableMapOf<String, String>()
                        for (ev in events) {
                            val decrypted = decryptOneEvent(ctx, resolvedKey, ev)
                            if (decrypted != null) out[ev.id] = decrypted
                        }
                        result = out
                        Log.i(TAG, "decryptEvents: decrypted ${result.size}/${events.size} events for $calendarId (via ${holder::class.simpleName})")
                        return@useKeys
                    }
                }
                if (result.isNotEmpty()) break
            }
            if (result.isEmpty()) {
                Log.w(TAG, "decryptEvents: could not resolve calendar key for $calendarId with user or address keys")
            }
        } catch (e: Exception) {
            Log.e(TAG, "decryptEvents failed for $calendarId: ${e.message}", e)
        }
        return result
    }

    /**
     * Resolve the per-calendar UnlockedKey by fetching and decrypting the calendar passphrase.
     *
     * Chain:
     *  - GET calendar passphrase → { KeyPacket, Passphrase, Salt }
     *  - Use unlocked user keys to decrypt the KeyPacket → calendar passphrase SessionKey
     *  - Use that SessionKey to decrypt the Passphrase blob → plain calendar passphrase (UTF-8)
     *  - GET calendar keys → pick the passphrase key (flags == 0)
     *  - Unlock that key with the plain passphrase → UnlockedKey
     */
    private suspend fun resolveCalendarKey(
        keyHolderContext: KeyHolderContext,
        calendarId: String
    ): UnlockedKey? {
        try {
            val api = apiProvider.get<CalendarApi>(userId).invoke { this }.valueOrNull
            if (api == null) {
                Log.w(TAG, "resolveCalendarKey: CalendarApi unavailable")
                return null
            }

            // Step 1: fetch calendar passphrase
            val passResp = try {
                api.getCalendarPassphrase(calendarId)
            } catch (e: Exception) {
                Log.w(TAG, "resolveCalendarKey: getCalendarPassphrase failed: ${e.message}")
                return null
            }
            val calPassphrase = passResp.passphrases.firstOrNull()
            if (calPassphrase == null) {
                Log.w(TAG, "resolveCalendarKey: no passphrases in response")
                return null
            }
            Log.i(TAG, "resolveCalendarKey: passphrase entry id=${calPassphrase.id} calId=${calPassphrase.calendarId}")

            // Step 2: get the member passphrase (encrypted to member's key)
            val memberPass = calPassphrase.memberPassphrases.firstOrNull()
            if (memberPass == null) {
                Log.w(TAG, "resolveCalendarKey: no member passphrases")
                return null
            }

            // Decrypt the member passphrase with this key holder's keyring
            val decryptedPass = try {
                keyHolderContext.decryptDataOrNullKeyHolder(memberPass.passphrase)
            } catch (e: Exception) {
                Log.d(TAG, "resolveCalendarKey: decryptDataOrNull threw: ${e.message}")
                null
            }
            if (decryptedPass == null) {
                Log.d(TAG, "resolveCalendarKey: could not decrypt passphrase with this keyring")
                return null
            }
            val passphraseStr = decryptedPass.toString(Charsets.UTF_8)
            Log.i(TAG, "resolveCalendarKey: calendar passphrase obtained (length=${passphraseStr.length})")

            // Step 3: fetch calendar keys
            val keysResp = try {
                api.getCalendarKeys(calendarId)
            } catch (e: Exception) {
                Log.w(TAG, "resolveCalendarKey: getCalendarKeys failed: ${e.message}")
                return null
            }
            if (keysResp.keys.isEmpty()) {
                Log.w(TAG, "resolveCalendarKey: no calendar keys returned")
                return null
            }
            // In Proton: flags == 0 is the "passphrase" key (the one we can unlock with the passphrase)
            val calKey = keysResp.keys.firstOrNull { it.flags == 0 } ?: keysResp.keys.first()
            Log.i(TAG, "resolveCalendarKey: using calendar key id=${calKey.id} flags=${calKey.flags}")

            // Step 4: unlock the calendar key with the passphrase
            val unlockedCalKey = try {
                context.pgpCrypto.unlock(calKey.privateKey, decryptedPass)
            } catch (e: Exception) {
                Log.w(TAG, "resolveCalendarKey: unlock calendar key failed: ${e.message}")
                return null
            }
            Log.i(TAG, "resolveCalendarKey: calendar key unlocked")
            return unlockedCalKey

        } catch (e: Exception) {
            Log.w(TAG, "resolveCalendarKey error: ${e.message}", e)
            return null
        }
    }

    /**
     * Decrypt a single event using the unlocked calendar key.
     *
     * 1. Decode SharedKeyPacket → decrypt with calendar key → event SessionKey
     * 2. Decode encrypted Type 3 content → decrypt with event SessionKey → gunzip → text
     */
    private fun decryptOneEvent(
        ctx: CryptoContext,
        calendarKey: UnlockedKey,
        ev: CalendarEventSyncData
    ): String? {
        val keyPacketStr = ev.sharedKeyPacketForDecrypt
        val contentStr = ev.encryptedICalData
        if (keyPacketStr == null || contentStr == null) {
            Log.d(TAG, "decryptOneEvent ${ev.id}: no keyPacket or encryptedICalData")
            return null
        }

        return try {
            // Decode the event's SharedKeyPacket (encrypted to calendar key)
            val keyPacketBytes = base64DecodeOrNull(keyPacketStr)
            if (keyPacketBytes == null) {
                Log.w(TAG, "decryptOneEvent ${ev.id}: bad SharedKeyPacket base64")
                return null
            }

            // Decrypt the event session key using the calendar key
            val eventSessionKey = ctx.pgpCrypto.decryptSessionKey(keyPacketBytes, calendarKey.value)
            if (eventSessionKey == null) {
                Log.w(TAG, "decryptOneEvent ${ev.id}: could not decrypt event SessionKey")
                return null
            }

            // Decode and decrypt the Type 3 content
            val contentBytes = base64DecodeOrNull(contentStr)
            if (contentBytes == null) {
                Log.w(TAG, "decryptOneEvent ${ev.id}: bad encrypted content base64")
                return null
            }

            val decrypted = ctx.pgpCrypto.decryptDataOrNull(contentBytes, eventSessionKey)
            if (decrypted == null) {
                Log.w(TAG, "decryptOneEvent ${ev.id}: decryptDataOrNull returned null")
                return null
            }

            // Gunzip → plaintext
            val text = try {
                GZIPInputStream(ByteArrayInputStream(decrypted)).bufferedReader().use { it.readText() }
            } catch (e: Exception) {
                // Not gzipped — treat as raw UTF-8
                decrypted.toString(Charsets.UTF_8)
            }

            if (text.isNotEmpty()) Log.d(TAG, "decryptOneEvent ${ev.id}: decrypted ${text.length} chars")
            text.ifEmpty { null }

        } catch (e: Exception) {
            Log.w(TAG, "decryptOneEvent ${ev.id} failed: ${e.message}")
            null
        }
    }

    companion object {
        private const val TAG = "CalendarSession"

        private fun base64DecodeOrNull(input: String): ByteArray? = try {
            Base64.decode(input, Base64.DEFAULT)
        } catch (e: IllegalArgumentException) {
            null
        }

        /**
         * Build a [CalendarSession] bound to a [user] and their [addresses];
         * both account keys and address keys are tried to unlock the calendar passphrase.
         */
        suspend fun create(
            apiProvider: ApiProvider,
            userId: UserId,
            context: CryptoContext,
            user: User,
            addresses: List<UserAddress> = emptyList()
        ): CalendarSession =
            CalendarSession(apiProvider, userId, context, user, addresses)
    }
}
