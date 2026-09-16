package com.protosync.app.di

import com.protosync.app.api.ProtonApiClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.configuration.EnvironmentConfiguration
import me.proton.core.featureflag.domain.FeatureFlagOverrider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ConfigModule {
    @Provides
    @Singleton
    fun provideEnvironmentConfiguration(): EnvironmentConfiguration =
        EnvironmentConfiguration.fromMap(
            mapOf(
                "host" to "proton.me",
                "apiPrefix" to "mail-api",
                "useDefaultPins" to true
            )
        )

    @Provides
    @Singleton
    fun provideFeatureFlagOverrider(): FeatureFlagOverrider = FeatureFlagOverrider { null }
}
