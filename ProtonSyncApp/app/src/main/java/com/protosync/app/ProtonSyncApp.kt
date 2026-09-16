package com.protosync.app

import android.app.Application
import androidx.startup.AppInitializer
import com.protosync.app.init.AppLoggerInitializer
import com.protosync.app.init.WorkManagerInitializer
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ProtonSyncApp : Application() {

    override fun onCreate() {
        super.onCreate()
        val initializer = AppInitializer.getInstance(this)
        initializer.initializeComponent(AppLoggerInitializer::class.java)
        initializer.initializeComponent(WorkManagerInitializer::class.java)
        MainInitializer.init(this)
    }
}