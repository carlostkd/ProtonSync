package com.protosync.app.init

import android.content.Context
import androidx.startup.Initializer
import com.protosync.app.init.AppLoggerInitializer
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import me.proton.core.accountmanager.data.AccountStateHandler

class AccountStateHandlerInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            AccountStateHandlerEntryPoint::class.java
        )
        entryPoint.handler().start()
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = listOf(
        AppLoggerInitializer::class.java
    )

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AccountStateHandlerEntryPoint {
        fun handler(): AccountStateHandler
    }
}