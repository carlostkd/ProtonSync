package com.protosync.app.work

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object SyncNow {
    const val CONTACTS_WORK = "proton_sync_contacts_periodic"
    const val CALENDAR_WORK = "proton_sync_calendar_periodic"
    const val ONETIME_CONTACTS = "proton_sync_contacts_once"
    const val ONETIME_CALENDAR = "proton_sync_calendar_once"

    fun enqueuePeriodic(workManager: WorkManager) {
        val contactsPeriodic = PeriodicWorkRequestBuilder<ContactsSyncWorker>(6, TimeUnit.HOURS)
            .build()
        val calendarPeriodic = PeriodicWorkRequestBuilder<CalendarSyncWorker>(6, TimeUnit.HOURS)
            .build()

        workManager.enqueueUniquePeriodicWork(CONTACTS_WORK, ExistingPeriodicWorkPolicy.KEEP, contactsPeriodic)
        workManager.enqueueUniquePeriodicWork(CALENDAR_WORK, ExistingPeriodicWorkPolicy.KEEP, calendarPeriodic)
    }

    fun triggerContactsOnce(workManager: WorkManager) {
        val contactsOnce = OneTimeWorkRequestBuilder<ContactsSyncWorker>().build()
        workManager.enqueueUniqueWork(ONETIME_CONTACTS, ExistingWorkPolicy.REPLACE, contactsOnce)
    }
}