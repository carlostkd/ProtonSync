package com.protosync.app.init

import android.content.Context
import androidx.startup.Initializer
import com.protosync.app.util.AppLogger

class AppLoggerInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        AppLogger.install()
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}