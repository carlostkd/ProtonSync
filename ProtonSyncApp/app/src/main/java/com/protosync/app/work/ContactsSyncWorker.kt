package com.protosync.app.work

import android.content.Context
import android.content.pm.PackageManager
import com.protosync.app.util.SyncLog as Log
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ezvcard.VCard
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getPrimaryAccount
import me.proton.core.contact.domain.decryptContactCard
import me.proton.core.contact.domain.repository.ContactRepository
import me.proton.core.key.domain.useKeys
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.user.domain.UserManager
import kotlinx.coroutines.flow.first
import com.protosync.app.sync.ContactsSyncer
import com.protosync.app.util.SyncSettings

@HiltWorker
class ContactsSyncWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val accountManager: AccountManager,
    private val contactRepository: ContactRepository,
    private val userManager: UserManager,
    private val cryptoContext: CryptoContext,
    private val contactsSyncer: ContactsSyncer,
    private val syncSettings: SyncSettings
) : CoroutineWorker(appContext, params) {

    private val TAG = "ContactsSyncWorker"

    override suspend fun doWork(): Result {
        if (!syncSettings.contactsSyncEnabled) return Result.success()

        if (ContextCompat.checkSelfPermission(appContext, android.Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Missing contacts permissions; skipping sync (prompt user in app)")
            return Result.success()
        }

        val account = accountManager.getPrimaryAccount().first() ?: run {
            Log.w(TAG, "No primary account; skipping contact sync")
            return Result.success()
        }

        return try {
            val user = userManager.getUser(account.userId)
            val contacts = contactRepository.getAllContacts(account.userId)
            val decryptedVCards = mutableListOf<VCard>()

            for (contact in contacts) {
                try {
                    val withCards = contactRepository.getContactWithCards(account.userId, contact.id)
                    val card = user.useKeys(cryptoContext) {
                        withCards.contactCards.firstNotNullOfOrNull { c ->
                            try {
                                decryptContactCard(c).card
                            } catch (e: Exception) {
                                Log.w(TAG, "Card decrypt failed for ${contact.id}: ${e.message}")
                                null
                            }
                        }
                    }
                    if (card != null) decryptedVCards += card
                } catch (e: Exception) {
                    Log.w(TAG, "Decrypt failed for contact ${contact.id}: ${e.message}")
                }
            }

            contactsSyncer.sync(decryptedVCards)
            syncSettings.lastContactSyncTime = System.currentTimeMillis()
            Log.i(TAG, "Contact sync completed: ${decryptedVCards.size}/${contacts.size} decrypted")
            Result.success(
                workDataOf(
                    "contacts_total" to contacts.size.toLong(),
                    "contacts_written" to decryptedVCards.size.toLong(),
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Contact sync failed: ${e.message}", e)
            Result.retry()
        }
    }
}