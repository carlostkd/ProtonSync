package com.protosync.app.work

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.protosync.app.util.SyncLog as Log
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getPrimaryAccount
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.network.data.ApiProvider
import me.proton.core.user.domain.UserManager
import kotlinx.coroutines.flow.first
import com.protosync.app.calendar.CalendarApi
import com.protosync.app.calendar.CalendarSession
import com.protosync.app.calendar.CalendarSyncer
import com.protosync.app.util.SyncSettings

@HiltWorker
class CalendarSyncWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val accountManager: AccountManager,
    private val apiProvider: ApiProvider,
    private val cryptoContext: CryptoContext,
    private val calendarSyncer: CalendarSyncer,
    private val userManager: UserManager,
    private val syncSettings: SyncSettings
) : CoroutineWorker(appContext, params) {

    private val TAG = "CalendarSyncWorker"

    override suspend fun doWork(): Result {
        Log.i(TAG, "doWork START (runAttempt=${runAttemptCount})")
        if (!syncSettings.calendarSyncEnabled) return Result.success()

        if (!hasPermission(appContext, android.Manifest.permission.READ_CALENDAR) ||
            !hasPermission(appContext, android.Manifest.permission.WRITE_CALENDAR)
        ) {
            Log.w(TAG, "Missing calendar permissions; skipping sync (prompt user in app)")
            return Result.success()
        }

        val account = accountManager.getPrimaryAccount().first() ?: run {
            Log.w(TAG, "No primary account; skipping calendar sync")
            return Result.success()
        }

        return try {
            Log.i(TAG, "Obtaining authenticated calendar API...")
            val calendarApi = apiProvider.get<CalendarApi>(account.userId).invoke { this }.valueOrNull
                ?: run {
                    Log.w(TAG, "Failed to obtain authenticated calendar API")
                    return Result.retry()
                }
            Log.i(TAG, "Creating calendar session...")
            val user = userManager.getUser(account.userId)
            val addresses = userManager.getAddresses(account.userId)
            val session = CalendarSession.create(
                apiProvider = apiProvider,
                userId = account.userId,
                context = cryptoContext,
                user = user,
                addresses = addresses
            )

            Log.i(TAG, "Starting calendar sync...")
            calendarSyncer.sync(account.userId, session, calendarApi)
            syncSettings.lastCalendarSyncTime = System.currentTimeMillis()
            Log.i(TAG, "Calendar sync completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Calendar sync failed: ${e.message}", e)
            Result.retry()
        }
    }

    private fun hasPermission(context: Context, perm: String): Boolean {
        return ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
    }
}