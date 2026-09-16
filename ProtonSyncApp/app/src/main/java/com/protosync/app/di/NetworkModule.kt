package com.protosync.app.di

import com.protosync.app.api.ProtonApiClient
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.configuration.EnvironmentConfiguration
import me.proton.core.network.data.client.ExtraHeaderProviderImpl
import me.proton.core.network.data.di.AlternativeApiPins
import me.proton.core.network.data.di.BaseProtonApiUrl
import me.proton.core.network.data.di.CertificatePins
import me.proton.core.network.data.di.Constants
import me.proton.core.network.data.di.DohProviderUrls
import me.proton.core.network.domain.ApiClient
import me.proton.core.network.domain.client.ExtraHeaderProvider
import me.proton.core.network.domain.serverconnection.DohAlternativesListener
import me.proton.core.util.kotlin.takeIfNotBlank
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import javax.inject.Singleton
import me.proton.core.network.data.di.Constants as NetworkDataConstants

@Module
@InstallIn(SingletonComponent::class)
class NetworkConfigModule {
    @Provides
    @Singleton
    fun provideExtraHeaderProvider(envConfig: EnvironmentConfiguration): ExtraHeaderProvider =
        ExtraHeaderProviderImpl().apply {
            envConfig.proxyToken.takeIfNotBlank()?.let { addHeaders("X-atlas-secret" to it) }
        }
}

@Module
@InstallIn(SingletonComponent::class)
class NetworkConstantsModule {
    @Provides
    @BaseProtonApiUrl
    fun provideProtonApiUrl(envConfig: EnvironmentConfiguration): HttpUrl = envConfig.baseUrl.toHttpUrl()

    @DohProviderUrls
    @Provides
    fun provideDohProviderUrls(): Array<String> = NetworkDataConstants.DOH_PROVIDERS_URLS

    @CertificatePins
    @Provides
    fun provideCertificatePins(envConfig: EnvironmentConfiguration) =
        if (envConfig.useDefaultPins) Constants.DEFAULT_SPKI_PINS else emptyArray()

    @AlternativeApiPins
    @Provides
    fun provideAlternativeApiPins(envConfig: EnvironmentConfiguration) =
        if (envConfig.useDefaultPins) Constants.ALTERNATIVE_API_SPKI_PINS else emptyList()
}

@Module
@InstallIn(SingletonComponent::class)
class NetworkCallbacksModule {
    @Provides
    @Singleton
    fun provideDohAlternativesListener(): DohAlternativesListener? = null
}

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkBindsModule {
    @Binds
    @Singleton
    abstract fun provideApiClient(protonApiClient: ProtonApiClient): ApiClient
}