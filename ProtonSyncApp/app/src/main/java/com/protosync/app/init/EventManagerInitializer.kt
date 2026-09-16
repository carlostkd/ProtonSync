package com.protosync.app.init

import android.content.Context
import androidx.startup.Initializer
import com.protosync.app.init.AppLoggerInitializer
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import me.proton.core.eventmanager.data.CoreEventManagerStarter

class EventManagerInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            EventManagerInitializerEntryPoint::class.java
        ).starter().start()
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = listOf(
        AppLoggerInitializer::class.java,
        WorkManagerInitializer::class.java
    )

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface EventManagerInitializerEntryPoint {
        fun starter(): CoreEventManagerStarter
    }
}