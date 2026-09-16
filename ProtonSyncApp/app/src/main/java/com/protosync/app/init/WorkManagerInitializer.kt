package com.protosync.app.init

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.startup.Initializer
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import com.protosync.app.init.AppLoggerInitializer

class WorkManagerInitializer : Initializer<WorkManager> {
    override fun create(context: Context): WorkManager {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WorkManagerEntryPoint::class.java
        )
        val config = Configuration.Builder()
            .setWorkerFactory(entryPoint.hiltWorkerFactory())
            .build()
        WorkManager.initialize(context, config)
        return WorkManager.getInstance(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = listOf(
        AppLoggerInitializer::class.java
    )

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WorkManagerEntryPoint {
        fun hiltWorkerFactory(): HiltWorkerFactory
    }
}