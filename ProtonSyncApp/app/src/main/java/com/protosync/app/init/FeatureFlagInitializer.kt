package com.protosync.app.init

import android.content.Context
import androidx.startup.Initializer
import com.protosync.app.BuildConfig
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import me.proton.core.featureflag.data.FeatureFlagRefreshStarter

class FeatureFlagInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            FeatureFlagInitializerEntryPoint::class.java
        ).featureFlagRefreshStarter().start(BuildConfig.DEBUG)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = listOf(
        WorkManagerInitializer::class.java
    )

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface FeatureFlagInitializerEntryPoint {
        fun featureFlagRefreshStarter(): FeatureFlagRefreshStarter
    }
}