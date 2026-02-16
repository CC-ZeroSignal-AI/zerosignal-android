package com.zerosignal.cognitedge.logging

import android.content.Context
import android.os.Build
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Writes all application logs (verbose through error + crash traces) to a
 * rotating set of files in the app's private storage.
 *
 * Thread-safe — a single-threaded executor serialises all disk writes so
 * callers never block on I/O.
 *
 * File rotation: when the current file exceeds [MAX_FILE_SIZE] it is
 * renamed to `*.1.log` (keeping one previous generation) and a fresh file
 * is started.
 */
object AppFileLogger {

    private const val LOG_DIR = "logs"
    private const val CURRENT_LOG = "zerosignal.log"
    private const val PREVIOUS_LOG = "zerosignal.1.log"
    private const val MAX_FILE_SIZE = 5 * 1024 * 1024L          // 5 MB
    private const val FLUSH_BATCH_SIZE = 20                      // flush every N entries

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    private lateinit var logDir: File
    private lateinit var currentFile: File
    private lateinit var previousFile: File

    private val executor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "AppFileLogger").apply { isDaemon = true }
    }

    private val buffer = ConcurrentLinkedQueue<String>()
    private val initialised = AtomicBoolean(false)

    // ── Public API ────────────────────────────────────────────────────

    fun init(context: Context) {
        if (initialised.getAndSet(true)) return

        logDir = File(context.filesDir, LOG_DIR).also { it.mkdirs() }
        currentFile = File(logDir, CURRENT_LOG)
        previousFile = File(logDir, PREVIOUS_LOG)

        writeDeviceHeader()
    }

    /** Standard log entry (called by [ZSLog]). */
    fun log(level: String, tag: String, message: String) {
        if (!initialised.get()) return
        val timestamp = dateFormat.format(Date())
        val line = "$timestamp  $level/$tag: $message"
        enqueue(line)
    }

    /** Log a [Throwable] with its full stack trace. */
    fun log(level: String, tag: String, message: String, throwable: Throwable) {
        if (!initialised.get()) return
        val sw = StringWriter()
        throwable.printStackTrace(PrintWriter(sw))
        val timestamp = dateFormat.format(Date())
        val line = "$timestamp  $level/$tag: $message\n$sw"
        enqueue(line)
    }

    /** Write a fatal crash block (called by [CrashHandler]). */
    fun logCrash(thread: Thread, throwable: Throwable) {
        if (!initialised.get()) return

        val sw = StringWriter()
        throwable.printStackTrace(PrintWriter(sw))
        val timestamp = dateFormat.format(Date())

        val block = buildString {
            appendLine()
            appendLine("═══════════════════════════════════════════════════════")
            appendLine("  FATAL CRASH — $timestamp")
            appendLine("  Thread: ${thread.name} (id=${thread.id})")
            appendLine("═══════════════════════════════════════════════════════")
            appendLine(sw.toString())
            appendLine("═══════════════════════════════════════════════════════")
            appendLine()
        }

        // Crash path: write synchronously so the data is on disk before the
        // process dies.
        try {
            rotateIfNeeded()
            FileOutputStream(currentFile, true).bufferedWriter().use { it.write(block) }
        } catch (_: Exception) {
            // Last resort – nothing we can do.
        }
    }

    /** Return the current log file so it can be shared / inspected. */
    fun getCurrentLogFile(): File? = if (initialised.get()) currentFile else null

    /** Return the previous (rotated) log file if it exists. */
    fun getPreviousLogFile(): File? =
        if (initialised.get() && previousFile.exists()) previousFile else null

    /** Return all log files. */
    fun getAllLogFiles(): List<File> {
        if (!initialised.get()) return emptyList()
        return logDir.listFiles()?.filter { it.extension == "log" }?.sortedByDescending { it.lastModified() }
            ?: emptyList()
    }

    // ── Internals ─────────────────────────────────────────────────────

    private fun enqueue(line: String) {
        buffer.add(line)

        // Submit a drain task; the single-threaded executor guarantees ordering.
        executor.execute { drain() }
    }

    private fun drain() {
        val lines = mutableListOf<String>()
        var entry = buffer.poll()
        while (entry != null) {
            lines.add(entry)
            entry = buffer.poll()
        }

        if (lines.isEmpty()) return

        try {
            rotateIfNeeded()
            FileOutputStream(currentFile, true).bufferedWriter().use { writer ->
                for (l in lines) {
                    writer.write(l)
                    writer.newLine()
                }
            }
        } catch (_: Exception) {
            // Swallow – we must never crash the app because of logging.
        }
    }

    private fun rotateIfNeeded() {
        if (currentFile.exists() && currentFile.length() > MAX_FILE_SIZE) {
            previousFile.delete()
            currentFile.renameTo(previousFile)
        }
    }

    private fun writeDeviceHeader() {
        executor.execute {
            try {
                val timestamp = dateFormat.format(Date())
                val header = buildString {
                    appendLine("───────────────────────────────────────────────────────")
                    appendLine("  ZeroSignal Log — Session started $timestamp")
                    appendLine("  Device : ${Build.MANUFACTURER} ${Build.MODEL}")
                    appendLine("  Android: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
                    appendLine("  Board  : ${Build.BOARD}  Hardware: ${Build.HARDWARE}")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        appendLine("  SoC    : ${Build.SOC_MODEL}")
                    }
                    appendLine("───────────────────────────────────────────────────────")
                }
                FileOutputStream(currentFile, true).bufferedWriter().use { it.write(header) }
            } catch (_: Exception) { }
        }
    }
}
