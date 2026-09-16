package com.protosync.app.util

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Ensures the Android device account exists for the ProtonSync contact/calendar
 * sync-adapter entries. Without this account the Contacts and Calendar providers
 * reject writes for this account type, so sync silently fails.
 */
@Singleton
class SyncAccount @Inject constructor(@ApplicationContext private val context: Context) {

    private val TAG = "SyncAccount"

    fun ensureExists(accountType: String, accountName: String) {
        try {
            val accountManager = AccountManager.get(context)
            val exists = accountManager.getAccountsByType(accountType).any { it.name == accountName }
            if (!exists) {
                val added = accountManager.addAccountExplicitly(
                    Account(accountName, accountType),
                    null,
                    null
                )
                Log.i(TAG, "Created Android account $accountName ($accountType): $added")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not create Android account $accountName ($accountType): ${e.message}")
        }
    }
}