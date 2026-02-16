package com.zerosignal.cognitedge.ai.repository

import android.content.Context
import android.util.Log
import com.zerosignal.cognitedge.ai.util.ModelFileListingUtil
import com.zerosignal.cognitedge.domain.model.ModelDownloadState
import com.zerosignal.cognitedge.domain.repository.ModelRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NexaModelRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : ModelRepository {

    companion object {
        private const val TAG = "NexaModelRepo"

        // HuggingFace repo IDs — file lists are discovered dynamically at runtime
        private const val NPU_REPO_ID = "NexaAI/Llama3.2-3B-NPU-Turbo-NPU-mobile"
        private const val GGUF_REPO_ID = "bartowski/Llama-3.2-3B-Instruct-GGUF"

        private const val MODEL_DIR_NAME = "llama-3.2-3b-npu-turbo"
        private const val GGUF_DIR_NAME = "llama-3.2-3b-gguf"
        private const val GGUF_FILE_NAME = "Llama-3.2-3B-Instruct-Q4_0.gguf"

        private const val NPU_MANIFEST_FILE = "file_manifest.txt"
    }

    private val modelsDir: File
        get() = File(context.noBackupFilesDir, "models").also { it.mkdirs() }

    private val modelDir: File
        get() = File(modelsDir, MODEL_DIR_NAME).also { it.mkdirs() }

    private val ggufDir: File
        get() = File(modelsDir, GGUF_DIR_NAME).also { it.mkdirs() }

    private val ggufFile: File
        get() = File(ggufDir, GGUF_FILE_NAME)

    private val npuManifestFile: File
        get() = File(modelDir, NPU_MANIFEST_FILE)

    // GGUF model for CPU/GPU fallback — same source as Nexa SDK's official demo app
    private val ggufModelUrl =
        ModelFileListingUtil.getDownloadUrl(GGUF_REPO_ID, GGUF_FILE_NAME)

    override fun isModelReady(): Boolean {
        // Model is ready if EITHER NPU model OR GGUF model is available
        val npuReady = isNpuModelReady()
        val ggufReady = isGgufModelReady()
        val ready = npuReady || ggufReady
        Log.d(TAG, "isModelReady=$ready (npu=$npuReady, gguf=$ggufReady)")
        return ready
    }

    /**
     * Checks NPU readiness using the cached manifest (dynamic file list).
     * If no manifest exists, NPU is not ready (files haven't been discovered yet).
     */
    private fun isNpuModelReady(): Boolean {
        val manifest = ModelFileListingUtil.readManifest(npuManifestFile) ?: return false
        if (manifest.isEmpty()) return false
        return ModelFileListingUtil.allFilesExist(modelDir, manifest)
    }

    private fun isGgufModelReady(): Boolean {
        return ggufFile.exists() && ggufFile.length() > 0
    }

    override fun downloadModel(): Flow<ModelDownloadState> = flow {
        emit(ModelDownloadState.NotStarted)

        if (isModelReady()) {
            Log.i(TAG, "Model already downloaded — skipping")
            emit(ModelDownloadState.Initializing)
            emit(ModelDownloadState.Ready)
            return@flow
        }

        try {
            emit(ModelDownloadState.Downloading(progress = 0f, bytesDownloaded = 0))

            var totalBytesDownloaded = 0L

            // --- NPU model: dynamically discover files from HuggingFace ---
            val npuFiles = discoverNpuFiles()
            Log.i(TAG, "NPU file list: ${npuFiles.map { "${it.name} (${it.size})" }}")

            // Compute total size from discovered metadata + GGUF estimate (~2GB)
            val npuTotalSize = npuFiles.sumOf { it.size }
            val ggufEstimate = 2_000_000_000L
            val totalSizeEstimate = if (npuTotalSize > 0) npuTotalSize + ggufEstimate else 5_500_000_000L

            // Download each NPU file
            for (entry in npuFiles) {
                val destFile = File(modelDir, entry.name)

                if (destFile.exists() && destFile.length() > 0) {
                    Log.d(TAG, "Skipping ${entry.name} (already exists, ${destFile.length()} bytes)")
                    totalBytesDownloaded += destFile.length()
                    continue
                }

                val fileUrl = ModelFileListingUtil.getDownloadUrl(NPU_REPO_ID, entry.name)
                Log.i(TAG, "Downloading ${entry.name} from $fileUrl")
                downloadFile(fileUrl, destFile) { bytesReadDelta ->
                    totalBytesDownloaded += bytesReadDelta
                    if (totalBytesDownloaded % (1024 * 1024) < 8192) {
                        val progress = (totalBytesDownloaded.toDouble() / totalSizeEstimate).toFloat().coerceIn(0f, 0.99f)
                        emit(ModelDownloadState.Downloading(progress = progress, bytesDownloaded = totalBytesDownloaded))
                    }
                }
                Log.i(TAG, "Finished downloading ${entry.name} (${destFile.length()} bytes)")
            }

            // --- GGUF model for CPU/GPU fallback ---
            if (!isGgufModelReady()) {
                Log.i(TAG, "Downloading GGUF fallback model: $GGUF_FILE_NAME")
                downloadFile(ggufModelUrl, ggufFile) { bytesReadDelta ->
                    totalBytesDownloaded += bytesReadDelta
                    if (totalBytesDownloaded % (1024 * 1024) < 8192) {
                        val progress = (totalBytesDownloaded.toDouble() / totalSizeEstimate).toFloat().coerceIn(0f, 0.99f)
                        emit(ModelDownloadState.Downloading(progress = progress, bytesDownloaded = totalBytesDownloaded))
                    }
                }
                Log.i(TAG, "Finished downloading GGUF model (${ggufFile.length()} bytes)")
            } else {
                Log.d(TAG, "Skipping GGUF model (already exists, ${ggufFile.length()} bytes)")
                totalBytesDownloaded += ggufFile.length()
            }

            emit(ModelDownloadState.Verifying)
            if (isModelReady()) {
                Log.i(TAG, "All model files verified — ready")
                emit(ModelDownloadState.Initializing)
                emit(ModelDownloadState.Ready)
            } else {
                Log.e(TAG, "Verification failed: some files are missing or incomplete")
                emit(ModelDownloadState.Error("Verification failed: incomplete files"))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Download failed", e)
            emit(ModelDownloadState.Error(e.message ?: "Download failed"))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Discovers NPU model files dynamically:
     * 1. Check for a cached local manifest first
     * 2. If no manifest, query HuggingFace API to list repo files
     * 3. Save the discovered list as a manifest for future use
     */
    private suspend fun discoverNpuFiles(): List<ModelFileListingUtil.FileEntry> {
        // Try cached manifest first
        val cached = ModelFileListingUtil.readManifest(npuManifestFile)
        if (cached != null && cached.isNotEmpty()) {
            Log.d(TAG, "Using cached NPU manifest (${cached.size} files)")
            return cached
        }

        // Query HuggingFace API for file list
        Log.i(TAG, "Fetching NPU file list from HuggingFace: $NPU_REPO_ID")
        val files = ModelFileListingUtil.listFilesFromHuggingFace(NPU_REPO_ID)

        if (files.isEmpty()) {
            Log.e(TAG, "HuggingFace returned empty file list for $NPU_REPO_ID")
            return emptyList()
        }

        // Cache the manifest locally
        ModelFileListingUtil.saveManifest(npuManifestFile, files)
        return files
    }

    private suspend fun downloadFile(urlStr: String, destFile: File, onProgress: suspend (Long) -> Unit) {
        // No withContext here — the calling flow already runs on Dispatchers.IO via flowOn.
        // Wrapping in withContext would create a new coroutine context, violating Flow's
        // emission invariant when onProgress calls emit().
        val connection = openConnectionFollowingRedirects(urlStr)
        try {
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("Server returned HTTP ${connection.responseCode} for $urlStr")
            }

            val contentLength = connection.contentLengthLong
            Log.d(TAG, "Downloading $urlStr — content-length=$contentLength")

            val input = connection.inputStream
            val output = FileOutputStream(destFile)
            val buffer = ByteArray(8192)
            var bytesRead: Int

            try {
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                    onProgress(bytesRead.toLong())
                }
                output.flush()
            } finally {
                output.close()
                input.close()
            }
        } finally {
            connection.disconnect()
        }
    }

    /**
     * Opens an HTTP connection, manually following redirects across hosts.
     * HttpURLConnection only auto-follows redirects within the same host,
     * but HuggingFace redirects to a CDN on a different host (302).
     */
    private fun openConnectionFollowingRedirects(urlStr: String, maxRedirects: Int = 5): HttpURLConnection {
        var currentUrl = urlStr
        var redirectCount = 0
        while (true) {
            val connection = URL(currentUrl).openConnection() as HttpURLConnection
            connection.connectTimeout = 30000
            connection.readTimeout = 60000
            connection.instanceFollowRedirects = false
            connection.connect()

            val code = connection.responseCode
            if (code in 301..303 || code == 307 || code == 308) {
                val location = connection.getHeaderField("Location")
                    ?: throw IOException("Redirect $code but no Location header for $currentUrl")
                connection.disconnect()
                redirectCount++
                if (redirectCount > maxRedirects) {
                    throw IOException("Too many redirects ($maxRedirects) for $urlStr")
                }
                // Resolve relative redirects against the current URL
                val resolvedUrl = if (location.startsWith("http://") || location.startsWith("https://")) {
                    location
                } else {
                    val base = URL(currentUrl)
                    URL(base, location).toString()
                }
                Log.d(TAG, "Following redirect $code: $currentUrl -> $resolvedUrl")
                currentUrl = resolvedUrl
            } else {
                return connection
            }
        }
    }

    override fun getModelPath(): String? {
        return if (isNpuModelReady()) modelDir.absolutePath else null
    }

    override fun getGgufModelPath(): String? {
        return if (isGgufModelReady()) ggufFile.absolutePath else null
    }
}
