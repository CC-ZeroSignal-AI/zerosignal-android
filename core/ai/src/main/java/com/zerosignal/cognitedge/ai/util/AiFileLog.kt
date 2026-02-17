package com.zerosignal.cognitedge.ai.util

import android.content.Context
import android.os.Build
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Lightweight file logger for the core/ai module.
 * Writes to both Logcat and an on-device file so logs can be pulled for analysis.
 *
 * File location: /data/data/<pkg>/files/logs/ai_debug.log
 */
object AiFileLog {

    private const val LOG_FILE = "ai_debug.log"
    private const val LOG_DIR = "logs"

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    private val initialised = AtomicBoolean(false)
    private lateinit var logFile: File

    private val executor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "AiFileLog").apply { isDaemon = true }
    }

    fun init(context: Context) {
        if (initialised.getAndSet(true)) return
        val dir = File(context.filesDir, LOG_DIR).also { it.mkdirs() }
        logFile = File(dir, LOG_FILE)

        executor.execute {
            try {
                val ts = dateFormat.format(Date())
                val header = buildString {
                    appendLine()
                    appendLine("=== AI Debug Log — Session $ts ===")
                    appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
                    appendLine("Android: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
                    appendLine("Hardware: ${Build.HARDWARE} | Board: ${Build.BOARD}")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        appendLine("SoC: ${Build.SOC_MODEL} (${Build.SOC_MANUFACTURER})")
                    }
                    appendLine("======================================")
                }
                FileOutputStream(logFile, true).bufferedWriter().use { it.write(header) }
            } catch (_: Exception) {}
        }
    }

    fun i(tag: String, msg: String) {
        Log.i(tag, msg)
        write("I", tag, msg)
    }

    fun d(tag: String, msg: String) {
        Log.d(tag, msg)
        write("D", tag, msg)
    }

    fun w(tag: String, msg: String) {
        Log.w(tag, msg)
        write("W", tag, msg)
    }

    fun w(tag: String, msg: String, tr: Throwable) {
        Log.w(tag, msg, tr)
        write("W", tag, "$msg\n${stackTrace(tr)}")
    }

    fun e(tag: String, msg: String) {
        Log.e(tag, msg)
        write("E", tag, msg)
    }

    fun e(tag: String, msg: String, tr: Throwable) {
        Log.e(tag, msg, tr)
        write("E", tag, "$msg\n${stackTrace(tr)}")
    }

    private fun write(level: String, tag: String, msg: String) {
        if (!initialised.get()) return
        val ts = dateFormat.format(Date())
        val line = "$ts  $level/$tag: $msg\n"
        executor.execute {
            try {
                FileOutputStream(logFile, true).bufferedWriter().use { it.write(line) }
            } catch (_: Exception) {}
        }
    }

    private fun stackTrace(tr: Throwable): String {
        val sw = StringWriter()
        tr.printStackTrace(PrintWriter(sw))
        return sw.toString()
    }
}
