package com.zerosignal.cognitedge.logging

import android.util.Log as AndroidLog

/**
 * Drop-in wrapper around [android.util.Log] that also persists every log
 * entry to the on-device log file via [AppFileLogger].
 *
 * Usage — replace `Log.d(TAG, msg)` with `ZSLog.d(TAG, msg)` throughout
 * the codebase.  All six standard levels are supported.
 */
object ZSLog {

    // ── Verbose ───────────────────────────────────────────────────────
    fun v(tag: String, msg: String): Int {
        AppFileLogger.log("V", tag, msg)
        return AndroidLog.v(tag, msg)
    }

    fun v(tag: String, msg: String, tr: Throwable): Int {
        AppFileLogger.log("V", tag, msg, tr)
        return AndroidLog.v(tag, msg, tr)
    }

    // ── Debug ─────────────────────────────────────────────────────────
    fun d(tag: String, msg: String): Int {
        AppFileLogger.log("D", tag, msg)
        return AndroidLog.d(tag, msg)
    }

    fun d(tag: String, msg: String, tr: Throwable): Int {
        AppFileLogger.log("D", tag, msg, tr)
        return AndroidLog.d(tag, msg, tr)
    }

    // ── Info ──────────────────────────────────────────────────────────
    fun i(tag: String, msg: String): Int {
        AppFileLogger.log("I", tag, msg)
        return AndroidLog.i(tag, msg)
    }

    fun i(tag: String, msg: String, tr: Throwable): Int {
        AppFileLogger.log("I", tag, msg, tr)
        return AndroidLog.i(tag, msg, tr)
    }

    // ── Warn ──────────────────────────────────────────────────────────
    fun w(tag: String, msg: String): Int {
        AppFileLogger.log("W", tag, msg)
        return AndroidLog.w(tag, msg)
    }

    fun w(tag: String, msg: String, tr: Throwable): Int {
        AppFileLogger.log("W", tag, msg, tr)
        return AndroidLog.w(tag, msg, tr)
    }

    fun w(tag: String, tr: Throwable): Int {
        AppFileLogger.log("W", tag, tr.message ?: "Warning", tr)
        return AndroidLog.w(tag, tr)
    }

    // ── Error ─────────────────────────────────────────────────────────
    fun e(tag: String, msg: String): Int {
        AppFileLogger.log("E", tag, msg)
        return AndroidLog.e(tag, msg)
    }

    fun e(tag: String, msg: String, tr: Throwable): Int {
        AppFileLogger.log("E", tag, msg, tr)
        return AndroidLog.e(tag, msg, tr)
    }

    // ── WTF (What a Terrible Failure) ─────────────────────────────────
    fun wtf(tag: String, msg: String): Int {
        AppFileLogger.log("WTF", tag, msg)
        return AndroidLog.wtf(tag, msg)
    }

    fun wtf(tag: String, msg: String, tr: Throwable): Int {
        AppFileLogger.log("WTF", tag, msg, tr)
        return AndroidLog.wtf(tag, msg, tr)
    }

    fun wtf(tag: String, tr: Throwable): Int {
        AppFileLogger.log("WTF", tag, tr.message ?: "WTF", tr)
        return AndroidLog.wtf(tag, tr)
    }
}
