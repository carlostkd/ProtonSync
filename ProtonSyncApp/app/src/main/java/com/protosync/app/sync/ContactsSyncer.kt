package com.protosync.app.sync

import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.provider.ContactsContract
import com.protosync.app.util.SyncLog as Log
import dagger.hilt.android.qualifiers.ApplicationContext
import ezvcard.VCard
import com.protosync.app.util.SyncAccount
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactsSyncer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val syncAccount: SyncAccount
) {

    private val TAG = "ContactsSyncer"

    /**
     * Writes decrypted [vCards] into the Android contacts provider under the ProtonSync account.
     * A fresh raw contact is created for each vCard. Mapping between Proton contact IDs and
     * Android raw contact IDs is intentionally left to a future phase (idempotent update/delete).
     */
    fun sync(vCards: List<VCard>): Int {
        syncAccount.ensureExists(ACCOUNT_TYPE, ACCOUNT_NAME)
        val resolver = context.contentResolver
        var written = 0

        for (vCard in vCards) {
            val insert = ArrayList<ContentProviderOperation>()

            insert += ContentProviderOperation.newInsert(rawContactsUri())
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, ACCOUNT_TYPE)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, ACCOUNT_NAME)
                .build()

            val displayName = vCard.formattedName?.value
                ?: fullNameFromStructuredName(vCard)
                ?: vCard.organizations.firstOrNull()?.values?.firstOrNull()
                ?: vCard.emails.firstOrNull()?.value
                ?: vCard.telephoneNumbers.firstOrNull()?.text
                ?: "Unknown"

            insert += ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, displayName)
                .build()

            for (email in vCard.emails) {
                insert += ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Email.ADDRESS, email.value)
                    .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_HOME)
                    .build()
            }

            for (phone in vCard.telephoneNumbers) {
                insert += ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone.text)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .build()
            }

            for (org in vCard.organizations) {
                org.values.firstOrNull()?.let { company ->
                    insert += ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, company)
                        .build()
                }
            }

            try {
                resolver.applyBatch("com.android.contacts", insert)
                written++
            } catch (e: Exception) {
                Log.w(TAG, "Failed to insert contact $displayName: ${e.message}")
            }
        }
        Log.i(TAG, "Contacts sync wrote $written/${vCards.size} contacts")
        return written
    }

    private fun fullNameFromStructuredName(vCard: VCard): String? {
        val structuredName = vCard.structuredName ?: return null
        val parts = mutableListOf<String>()
        structuredName.prefixes.forEach { parts += it.trim() }
        structuredName.given?.let { parts += it.trim() }
        structuredName.additionalNames.forEach { parts += it.trim() }
        structuredName.family?.let { parts += it.trim() }
        structuredName.suffixes.forEach { parts += it.trim() }
        val fullName = parts.filter { it.isNotBlank() }.joinToString(" ")
        return fullName.takeIf { it.isNotEmpty() }
    }

    private fun rawContactsUri() = ContactsContract.RawContacts.CONTENT_URI.buildUpon()
        .appendQueryParameter(ContactsContract.RawContacts.ACCOUNT_TYPE, ACCOUNT_TYPE)
        .appendQueryParameter(ContactsContract.RawContacts.ACCOUNT_NAME, ACCOUNT_NAME)
        .build()

    companion object {
        const val ACCOUNT_TYPE = "com.protosync.app.account"
        const val ACCOUNT_NAME = "ProtonSync"
    }
}