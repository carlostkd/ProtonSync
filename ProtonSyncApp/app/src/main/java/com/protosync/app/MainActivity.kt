package com.protosync.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.protosync.app.ui.AboutScreen
import com.protosync.app.ui.LoginScreen
import com.protosync.app.ui.ProtonSyncTheme
import com.protosync.app.ui.SyncLogsScreen
import com.protosync.app.ui.SyncScreen
import com.protosync.app.vm.AccountViewModel
import com.protosync.app.vm.SyncViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private val accountViewModel: AccountViewModel by viewModels()
    private val syncViewModel: SyncViewModel by viewModels()

    private val runtimePermissions = arrayOf(
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.WRITE_CONTACTS,
        Manifest.permission.READ_CALENDAR,
        Manifest.permission.WRITE_CALENDAR,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        accountViewModel.register(this)

        setContent {
            ProtonSyncTheme {
                val accountState by accountViewModel.state.collectAsState()
                val syncState by syncViewModel.state.collectAsState()
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Main) }

                when {
                    currentScreen == Screen.About -> {
                        AboutScreen(onBack = { currentScreen = Screen.Main })
                    }
                    currentScreen == Screen.SyncLogs -> {
                        SyncLogsScreen(onBack = { currentScreen = Screen.Main })
                    }
                    accountState is AccountViewModel.State.LoginNeeded -> {
                        LoginScreen(onStartLogin = { accountViewModel.signIn() })
                    }
                    accountState is AccountViewModel.State.AccountList -> {
                        LaunchedEffect(Unit) { requestMissingPermissions() }
                        SyncScreen(
                            lastContactSyncTime = syncState.lastContactSyncTime,
                            lastCalendarSyncTime = syncState.lastCalendarSyncTime,
                            contactsSyncEnabled = syncState.contactsSyncEnabled,
                            calendarSyncEnabled = syncState.calendarSyncEnabled,
                            isSyncing = syncState.isSyncing,
                            message = syncState.message,
                            isError = syncState.isError,
                            onSyncNow = { syncViewModel.syncNow() },
                            onToggleContacts = { syncViewModel.toggleContactsSync() },
                            onToggleCalendar = { syncViewModel.toggleCalendarSync() },
                            onOpenAbout = { currentScreen = Screen.About },
                            onOpenSyncLogs = { currentScreen = Screen.SyncLogs },
                            onSignOut = {
                                lifecycleScope.launch {
                                    val accounts = (accountState as AccountViewModel.State.AccountList).accounts
                                    if (accounts.isNotEmpty()) syncViewModel.signOut(accounts.first().userId)
                                }
                            }
                        )
                    }
                    else -> { /* Processing spinner omitted for simplicity */ }
                }
            }
        }
    }

    private fun requestMissingPermissions() {
        val missing = runtimePermissions.filterNot {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            requestPermissions(missing.toTypedArray(), REQUEST_RUNTIME_PERMISSIONS)
        }
    }

    companion object {
        private const val REQUEST_RUNTIME_PERMISSIONS = 42
    }

    private enum class Screen { Main, About, SyncLogs }
}