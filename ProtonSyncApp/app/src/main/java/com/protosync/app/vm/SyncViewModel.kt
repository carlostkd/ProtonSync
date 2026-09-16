package com.protosync.app.vm

import com.protosync.app.util.SyncLog as Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getPrimaryAccount
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import me.proton.core.user.domain.UserManager
import com.protosync.app.calendar.CalendarApi
import com.protosync.app.calendar.CalendarSession
import com.protosync.app.calendar.CalendarSyncer
import com.protosync.app.util.SyncSettings
import com.protosync.app.util.SyncAccount
import com.protosync.app.work.SyncNow
import javax.inject.Inject

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val workManager: WorkManager,
    private val syncSettings: SyncSettings,
    private val calendarSyncer: CalendarSyncer,
    private val syncAccount: SyncAccount,
    private val apiProvider: ApiProvider,
    private val cryptoContext: CryptoContext,
    private val userManager: UserManager
) : ViewModel() {

    data class SyncState(
        val isLoading: Boolean = true,
        val isLoggedIn: Boolean = false,
        val isSyncing: Boolean = false,
        val lastContactSyncTime: Long = 0L,
        val lastCalendarSyncTime: Long = 0L,
        val calendarSyncEnabled: Boolean = true,
        val contactsSyncEnabled: Boolean = true,
        val message: String? = null,
        val isError: Boolean = false,
    )

    private val _state = MutableStateFlow(SyncState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val account = accountManager.getPrimaryAccount().first()
            _state.value = SyncState(
                isLoading = false,
                isLoggedIn = account != null,
                lastContactSyncTime = syncSettings.lastContactSyncTime,
                lastCalendarSyncTime = syncSettings.lastCalendarSyncTime,
                calendarSyncEnabled = syncSettings.calendarSyncEnabled,
                contactsSyncEnabled = syncSettings.contactsSyncEnabled,
            )
        }
    }

    fun syncNow() {
        if (_state.value.isSyncing) return
        _state.value = _state.value.copy(isSyncing = true, message = null, isError = false)
        SyncNow.enqueuePeriodic(workManager)

        viewModelScope.launch {
            var contactsWritten = 0L
            var calendarSynced = false
            var error: String? = null

            val account = accountManager.getPrimaryAccount().first()
            if (account == null) {
                error = "Not logged in."
            } else {
                if (syncSettings.contactsSyncEnabled) {
                    SyncNow.triggerContactsOnce(workManager)
                    contactsWritten = pollContactsWork(workManager)?.let { run ->
                        run.getLong("contacts_written", 0L)
                    } ?: 0L
                }

                if (syncSettings.calendarSyncEnabled) {
                    val result = runCalendarSync(account.userId)
                    if (result == null) calendarSynced = true
                    else {
                        error = result
                        calendarSynced = false
                    }
                }
            }

            refreshTimes()
            val msg = error ?: when {
                contactsWritten > 0 && calendarSynced -> "Done — $contactsWritten contacts and calendar synced."
                contactsWritten > 0 -> "Done — $contactsWritten contacts written."
                calendarSynced -> "Calendar synced."
                else -> "Sync complete — no changes."
            }
            Log.i("SyncViewModel", "syncNow done: contactsWritten=$contactsWritten calendarSynced=$calendarSynced error=$error msg=$msg")
            _state.value = _state.value.copy(isSyncing = false, message = msg, isError = error != null)
        }
    }

    private suspend fun runCalendarSync(userId: UserId): String? {
        Log.i("SyncViewModel", "runCalendarSync: obtaining API for $userId")
        return try {
            val user = userManager.getUser(userId)
            val api = apiProvider.get<CalendarApi>(userId).invoke { this }.valueOrNull
                ?: run {
                    Log.e("SyncViewModel", "Calendar API unavailable for user $userId")
                    return "Could not start calendar sync (API unavailable)."
                }
            Log.i("SyncViewModel", "runCalendarSync: creating session")
            val session = CalendarSession.create(apiProvider, userId, cryptoContext, user, userManager.getAddresses(userId))
            Log.i("SyncViewModel", "runCalendarSync: starting sync")
            calendarSyncer.sync(userId, session, api)
            syncSettings.lastCalendarSyncTime = System.currentTimeMillis()
            Log.i("SyncViewModel", "Calendar sync completed in-process")
            null
        } catch (e: Exception) {
            Log.e("SyncViewModel", "Calendar sync failed: ${e.message}", e)
            "Calendar sync failed: ${e.message ?: "unknown error"}"
        }
    }

    private suspend fun pollContactsWork(workManager: WorkManager): androidx.work.Data? {
        val startMs = System.currentTimeMillis()
        val timeoutMs = 20_000L
        while (System.currentTimeMillis() - startMs < timeoutMs) {
            delay(1000)
            val infos = workManager.getWorkInfosForUniqueWorkLiveData(SyncNow.ONETIME_CONTACTS).awaitValue()
            if (infos.isEmpty()) continue
            val finished = infos.firstOrNull { it.state.isFinished } ?: continue
            return if (finished.state == WorkInfo.State.SUCCEEDED) finished.outputData else null
        }
        return null
    }

    fun toggleContactsSync() {
        syncSettings.contactsSyncEnabled = !syncSettings.contactsSyncEnabled
        _state.value = _state.value.copy(
            contactsSyncEnabled = syncSettings.contactsSyncEnabled,
            message = "Contacts sync ${if (syncSettings.contactsSyncEnabled) "enabled" else "disabled"}",
            isError = false,
        )
    }

    fun toggleCalendarSync() {
        syncSettings.calendarSyncEnabled = !syncSettings.calendarSyncEnabled
        _state.value = _state.value.copy(
            calendarSyncEnabled = syncSettings.calendarSyncEnabled,
            message = "Calendar sync ${if (syncSettings.calendarSyncEnabled) "enabled" else "disabled"}",
            isError = false,
        )
    }

    private fun refreshTimes() {
        viewModelScope.launch {
            val account = accountManager.getPrimaryAccount().first()
            _state.value = _state.value.copy(
                isLoading = false,
                isLoggedIn = account != null,
                lastContactSyncTime = syncSettings.lastContactSyncTime,
                lastCalendarSyncTime = syncSettings.lastCalendarSyncTime,
                calendarSyncEnabled = syncSettings.calendarSyncEnabled,
                contactsSyncEnabled = syncSettings.contactsSyncEnabled,
            )
        }
    }

    suspend fun signOut(userId: UserId) {
        accountManager.disableAccount(userId)
        refreshTimes()
    }

    private suspend fun <T> LiveData<T>.awaitValue(): T {
        return suspendCancellableCoroutine { continuation ->
            var observerRef: Observer<T>? = null
            val observer = Observer<T> { value ->
                if (continuation.isActive) {
                    continuation.resume(value, onCancellation = null)
                    observerRef?.let { this@awaitValue.removeObserver(it) }
                }
            }
            observerRef = observer
            observeForever(observer)
            continuation.invokeOnCancellation { observerRef?.let { this@awaitValue.removeObserver(it) } }
        }
    }
}