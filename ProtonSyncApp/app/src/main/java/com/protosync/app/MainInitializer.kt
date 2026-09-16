package com.protosync.app

import android.content.Context
import androidx.startup.AppInitializer
import androidx.startup.Initializer
import com.protosync.app.init.AccountStateHandlerInitializer
import com.protosync.app.init.AppLoggerInitializer
import com.protosync.app.init.EventManagerInitializer
import com.protosync.app.init.FeatureFlagInitializer
import com.protosync.app.init.WorkManagerInitializer
import me.proton.core.auth.presentation.MissingScopeInitializer
import me.proton.core.crypto.validator.presentation.init.CryptoValidatorInitializer
import me.proton.core.humanverification.presentation.HumanVerificationInitializer
import me.proton.core.network.presentation.init.UnAuthSessionFetcherInitializer
import me.proton.core.paymentiap.presentation.GooglePurchaseHandlerInitializer
import me.proton.core.plan.presentation.PurchaseHandlerInitializer
import me.proton.core.plan.presentation.UnredeemedPurchaseInitializer

class MainInitializer : Initializer<Unit> {

    override fun create(context: Context) {
    }

    override fun dependencies() = listOf(
        EventManagerInitializer::class.java,
        CryptoValidatorInitializer::class.java,
        PurchaseHandlerInitializer::class.java,
        GooglePurchaseHandlerInitializer::class.java,
        UnredeemedPurchaseInitializer::class.java,
        MissingScopeInitializer::class.java,
        HumanVerificationInitializer::class.java,
        UnAuthSessionFetcherInitializer::class.java,
        AppLoggerInitializer::class.java,
        WorkManagerInitializer::class.java,
        FeatureFlagInitializer::class.java,
        AccountStateHandlerInitializer::class.java
    )

    companion object {
        fun init(appContext: Context) {
            with(AppInitializer.getInstance(appContext)) {
                initializeComponent(MainInitializer::class.java)
            }
        }
    }
}