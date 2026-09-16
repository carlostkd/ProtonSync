package com.protosync.app.util

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncSettings @Inject constructor(@ApplicationContext context: Context) {
    private val prefs = context.getSharedPreferences("sync_settings", Context.MODE_PRIVATE)

    var lastContactSyncTime: Long
        get() = prefs.getLong(KEY_LAST_CONTACT_SYNC, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_CONTACT_SYNC, value).apply()

    var lastCalendarSyncTime: Long
        get() = prefs.getLong(KEY_LAST_CALENDAR_SYNC, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_CALENDAR_SYNC, value).apply()

    var calendarSyncEnabled: Boolean
        get() = prefs.getBoolean(KEY_CALENDAR_SYNC_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_CALENDAR_SYNC_ENABLED, value).apply()

    var contactsSyncEnabled: Boolean
        get() = prefs.getBoolean(KEY_CONTACTS_SYNC_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_CONTACTS_SYNC_ENABLED, value).apply()

    companion object {
        private const val KEY_LAST_CONTACT_SYNC = "last_contact_sync"
        private const val KEY_LAST_CALENDAR_SYNC = "last_calendar_sync"
        private const val KEY_CALENDAR_SYNC_ENABLED = "calendar_sync_enabled"
        private const val KEY_CONTACTS_SYNC_ENABLED = "contacts_sync_enabled"
    }
}