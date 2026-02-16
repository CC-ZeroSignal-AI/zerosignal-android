package com.zerosignal.cognitedge.logging

import android.util.Log

/**
 * Global uncaught-exception handler.
 *
 * When an uncaught [Throwable] reaches this handler it is:
 *   1. Written to the on-device log file via [AppFileLogger.logCrash]
 *   2. Forwarded to the previous default handler (if any) so the system
 *      can still show the standard crash dialog / kill the process.
 *
 * Install once from [ZeroSignalApp.onCreate]:
 * ```
 * CrashHandler.install()
 * ```
 */
object CrashHandler : Thread.UncaughtExceptionHandler {

    private const val TAG = "CrashHandler"

    private var defaultHandler: Thread.UncaughtExceptionHandler? = null

    /**
     * Installs this handler as the global default.
     * Safe to call multiple times — only the first call takes effect.
     */
    fun install() {
        if (defaultHandler != null) return            // already installed
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
        Log.i(TAG, "Global crash handler installed")
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            Log.e(TAG, "FATAL: Uncaught exception on thread '${thread.name}'", throwable)
            AppFileLogger.logCrash(thread, throwable)
        } catch (e: Exception) {
            // Must not throw from inside the handler.
            Log.e(TAG, "Error while logging crash", e)
        } finally {
            // Forward to the system / previous handler so Android can
            // display its crash dialog and terminate the process normally.
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
