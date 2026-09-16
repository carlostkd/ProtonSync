package com.protosync.app.util

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.ArrayDeque
import java.util.Date
import java.util.Locale

/**
 * In-memory ring buffer of sync log lines for user transparency.
 * Captures app log lines (sync/calendarserver/account related) and keeps
 * the last [MAX_LINES] entries, exposed as a StateFlow for Compose.
 */
object SyncLogStore {
    private const val MAX_LINES = 500
    private const val TAG = "SyncLogStore"

    data class LogLine(val timestamp: Long, val level: Char, val tag: String, val message: String) {
        val formatted: String
            get() = "${SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US).format(Date(timestamp))} $level/$tag: $message"
    }

    private val _lines = MutableStateFlow<List<LogLine>>(emptyList())
    val lines = _lines.asStateFlow()

    @Synchronized
    fun add(level: Char, tag: String, message: String) {
        val deque = ArrayDeque(_lines.value)
        deque.addLast(LogLine(System.currentTimeMillis(), level, tag, message))
        while (deque.size > MAX_LINES) deque.removeFirst()
        _lines.value = deque.toList()
    }

    @Synchronized
    fun clear() {
        _lines.value = emptyList()
    }

    fun exportText(): String = _lines.value.joinToString("\n") { it.formatted }

    /**
     * Install a hook so that standard android.util.Log calls from our sync
     * components are mirrored into the store. Tagged logs only from this app's
     * sync components are captured.
     */
    fun installHook() {
        try {
            val logClass = Class.forName("android.util.Log")
            // Hooking android.util.Log system-wide is not possible without
            // instrumentation; instead, components call SyncLog.d/i/w/e helpers.
        } catch (_: Exception) {
        }
    }
}

/** Convenience wrappers mirroring android.util.Log into the store. */
object SyncLog {
    fun d(tag: String, msg: String) { Log.d(tag, msg); SyncLogStore.add('D', tag, msg) }
    fun i(tag: String, msg: String) { Log.i(tag, msg); SyncLogStore.add('I', tag, msg) }
    fun w(tag: String, msg: String) { Log.w(tag, msg); SyncLogStore.add('W', tag, msg) }
    fun w(tag: String, msg: String, tr: Throwable?) { Log.w(tag, msg, tr); SyncLogStore.add('W', tag, "$msg ${tr?.message ?: ""}") }
    fun e(tag: String, msg: String) { Log.e(tag, msg); SyncLogStore.add('E', tag, msg) }
    fun e(tag: String, msg: String, tr: Throwable?) { Log.e(tag, msg, tr); SyncLogStore.add('E', tag, "$msg ${tr?.message ?: ""}") }
}
