package com.protosync.app.util

import android.util.Log
import me.proton.core.util.kotlin.CoreLogger
import me.proton.core.util.kotlin.Logger
import javax.inject.Inject

class AppLogger @Inject constructor() : Logger {

    override fun e(tag: String, message: String) {
        Log.e(tag, message)
    }

    override fun e(tag: String, throwable: Throwable) {
        Log.e(tag, throwable.message ?: "", throwable)
    }

    override fun e(tag: String, throwable: Throwable, message: String) {
        Log.e(tag, message, throwable)
    }

    override fun w(tag: String, message: String) {
        Log.w(tag, message)
    }

    override fun w(tag: String, throwable: Throwable) {
        Log.w(tag, throwable.message ?: "", throwable)
    }

    override fun w(tag: String, throwable: Throwable, message: String) {
        Log.w(tag, message, throwable)
    }

    override fun i(tag: String, message: String) {
        Log.i(tag, message)
    }

    override fun i(tag: String, throwable: Throwable, message: String) {
        Log.i(tag, message, throwable)
    }

    override fun d(tag: String, message: String) {
        Log.d(tag, message)
    }

    override fun d(tag: String, throwable: Throwable, message: String) {
        Log.d(tag, message, throwable)
    }

    override fun v(tag: String, message: String) {
        Log.v(tag, message)
    }

    override fun v(tag: String, throwable: Throwable, message: String) {
        Log.v(tag, message, throwable)
    }

    companion object {
        fun install() {
            CoreLogger.set(AppLogger())
        }
    }
}