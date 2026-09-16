package com.protosync.app.vm

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.proton.core.account.domain.entity.Account
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getPrimaryAccount
import me.proton.core.accountmanager.presentation.observe
import me.proton.core.accountmanager.presentation.onAccountCreateAccountFailed
import me.proton.core.accountmanager.presentation.onAccountCreateAccountNeeded
import me.proton.core.accountmanager.presentation.onAccountCreateAddressFailed
import me.proton.core.accountmanager.presentation.onAccountCreateAddressNeeded
import me.proton.core.accountmanager.presentation.onAccountDeviceSecretFailed
import me.proton.core.accountmanager.presentation.onAccountDeviceSecretNeeded
import me.proton.core.accountmanager.presentation.onAccountMigrationNeeded
import me.proton.core.accountmanager.presentation.onAccountTwoPassModeFailed
import me.proton.core.accountmanager.presentation.onAccountTwoPassModeNeeded
import me.proton.core.accountmanager.presentation.onSessionSecondFactorFailed
import me.proton.core.accountmanager.presentation.onSessionSecondFactorNeeded
import me.proton.core.accountmanager.presentation.onUserAddressKeyCheckFailed
import me.proton.core.accountmanager.presentation.onUserKeyCheckFailed
import me.proton.core.auth.presentation.AuthOrchestrator
import me.proton.core.auth.presentation.observe
import me.proton.core.auth.presentation.onMissingScopeFailed
import me.proton.core.auth.presentation.onMissingScopeSuccess
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.scopes.MissingScopeListener
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private var authOrchestrator: AuthOrchestrator,
    private val missingScopeListener: MissingScopeListener
) : ViewModel() {

    sealed class State {
        object Processing : State()
        object LoginNeeded : State()
        data class AccountList(val accounts: List<Account>) : State()
    }

    private val _state = MutableStateFlow<State>(State.Processing)
    val state = _state.asStateFlow()

    fun register(context: FragmentActivity) {
        authOrchestrator.register(context)

        accountManager.getAccounts()
            .flowWithLifecycle(context.lifecycle, minActiveState = Lifecycle.State.CREATED)
            .onEach { accounts ->
                if (accounts.isEmpty()) _state.emit(State.LoginNeeded)
                else _state.emit(State.AccountList(accounts))
            }
            .launchIn(context.lifecycleScope)

        authOrchestrator.setOnAddAccountResult {
            if (it == null) {
                viewModelScope.launch {
                    val accounts = accountManager.getAccounts().first()
                    _state.emit(State.AccountList(accounts))
                }
            }
        }

        with(authOrchestrator) {
            accountManager.observe(context.lifecycle, minActiveState = Lifecycle.State.CREATED)
                .onSessionSecondFactorNeeded { startSecondFactorWorkflow(it) }
                .onSessionSecondFactorFailed { /* Retried automatically by the account manager */ }
                .onAccountTwoPassModeNeeded { startTwoPassModeWorkflow(it) }
                .onAccountCreateAddressNeeded { startChooseAddressWorkflow(it) }
                .onAccountCreateAccountNeeded { startSignupWorkflow(cancellable = false) }
                .onAccountCreateAccountFailed { accountManager.disableAccount(it.userId) }
                .onAccountDeviceSecretNeeded { startDeviceSecretWorkflow(it) }
                .onAccountDeviceSecretFailed { accountManager.disableAccount(it.userId) }
                .onAccountTwoPassModeFailed { accountManager.disableAccount(it.userId) }
                .onAccountCreateAddressFailed { accountManager.disableAccount(it.userId) }
                .onAccountMigrationNeeded { /* no-op */ }
                .onUserKeyCheckFailed { /* no-op */ }
                .onUserAddressKeyCheckFailed { /* no-op */ }
        }

        missingScopeListener.observe(context.lifecycle, minActiveState = Lifecycle.State.CREATED)
            .onMissingScopeSuccess { /* no-op */ }
            .onMissingScopeFailed { /* no-op */ }
    }

    suspend fun signOut(userId: UserId) = accountManager.disableAccount(userId)

    fun signIn(username: String? = null) = authOrchestrator.startLoginWorkflow(username)
}