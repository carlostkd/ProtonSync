package com.protosync.app.api

import android.os.Build
import com.protosync.app.BuildConfig
import me.proton.core.network.domain.ApiClient
import me.proton.core.network.domain.ApiClient.DohRecordType
import java.util.Locale
import javax.inject.Inject

open class ProtonApiClient @Inject constructor() : ApiClient {
    protected open val appName = "android-mail"
    protected open val productName = "Proton Sync"
    protected open val versionName = "6.0.0"
    protected open val versionSuffix = if (BuildConfig.DEBUG) "-dev" else ""

    override suspend fun shouldUseDoh(): Boolean = BuildConfig.USE_DOH

    override suspend fun skipPrimaryRoute(): Boolean = false

    override val appVersionHeader: String
        get() = "$appName@$versionName$versionSuffix"

    override val userAgent: String
        get() = String.format(
            Locale.US,
            "%s/%s (Android %s; %s; %s %s; %s)",
            productName,
            versionName,
            Build.VERSION.RELEASE,
            Build.MODEL,
            Build.BRAND,
            Build.DEVICE,
            Locale.getDefault().language
        )

    override val connectTimeoutSeconds: Long = 30

    override val readTimeoutSeconds: Long = 30

    override val writeTimeoutSeconds: Long = 30

    override val callTimeoutSeconds: Long = 60

    override val pingTimeoutSeconds: Int = 5

    override val proxyValidityPeriodMs: Long = 7 * 24 * 3600L * 1000L

    override val dohServiceTimeoutMs: Long = 2000

    override val alternativesTotalTimeout: Long = 20000

    override val backoffRetryCount: Int = 3

    override val backoffBaseDelayMs: Int = 500

    override val enableDebugLogging: Boolean
        get() = BuildConfig.DEBUG

    override val dohRecordType: DohRecordType
        get() = DohRecordType.TXT

    override val useAltRoutingCertVerificationForMainRoute: Boolean = false

    override fun forceUpdate(errorMessage: String) {
        // No in-app update mechanism for a sideloaded sync app.
    }
}