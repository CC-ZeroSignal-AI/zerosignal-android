package com.zerosignal.cognitedge.ai.repository

import android.content.Context
import com.zerosignal.cognitedge.domain.model.ModelDownloadState
import com.zerosignal.cognitedge.domain.repository.ModelRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
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

    private val modelsDir: File
        get() = File(context.noBackupFilesDir, "models").also { it.mkdirs() }

    private val modelDir: File
        get() = File(modelsDir, MODEL_DIR_NAME).also { it.mkdirs() }

    // List of files to download: filename -> url
    private val modelFiles = listOf(
        "nexa.manifest" to "https://huggingface.co/NexaAI/Llama3.2-3B-NPU-Turbo-NPU-mobile/resolve/main/nexa.manifest",
        "files-1-2.nexa" to "https://huggingface.co/NexaAI/Llama3.2-3B-NPU-Turbo-NPU-mobile/resolve/main/files-1-2.nexa",
        "files-2-2.nexa" to "https://huggingface.co/NexaAI/Llama3.2-3B-NPU-Turbo-NPU-mobile/resolve/main/files-2-2.nexa",
        "weights-1-3.nexa" to "https://huggingface.co/NexaAI/Llama3.2-3B-NPU-Turbo-NPU-mobile/resolve/main/weights-1-3.nexa",
        "weights-2-3.nexa" to "https://huggingface.co/NexaAI/Llama3.2-3B-NPU-Turbo-NPU-mobile/resolve/main/weights-2-3.nexa",
        "weights-3-3.nexa" to "https://huggingface.co/NexaAI/Llama3.2-3B-NPU-Turbo-NPU-mobile/resolve/main/weights-3-3.nexa"
    )

    override fun isModelReady(): Boolean {
        if (!modelDir.exists() || !modelDir.isDirectory) return false
        
        // Check if all expected files exist and have non-zero size
        return modelFiles.all { (fileName, _) ->
            val file = File(modelDir, fileName)
            file.exists() && file.length() > 0
        }
    }

    override fun downloadModel(): Flow<ModelDownloadState> = flow {
        emit(ModelDownloadState.NotStarted)

        if (isModelReady()) {
            emit(ModelDownloadState.Initializing)
            emit(ModelDownloadState.Ready)
            return@flow
        }

        try {
            emit(ModelDownloadState.Downloading(progress = 0f, bytesDownloaded = 0))

            var totalBytesDownloaded = 0L
            val totalSizeEstimate = 3_500_000_000L // Approx 3.5GB

            for ((fileName, fileUrl) in modelFiles) {
                val destFile = File(modelDir, fileName)
                
                // Skip if already downloaded (basic check)
                if (destFile.exists() && destFile.length() > 0) {
                    totalBytesDownloaded += destFile.length()
                    continue 
                }

                downloadFile(fileUrl, destFile) { bytesReadDelta ->
                    totalBytesDownloaded += bytesReadDelta
                    
                    // Throttle updates: emit every ~1MB or 1% change
                    if (totalBytesDownloaded % (1024 * 1024) < 8192) { 
                        val progress = (totalBytesDownloaded.toDouble() / totalSizeEstimate).toFloat().coerceIn(0f, 1f)
                        emit(ModelDownloadState.Downloading(progress = progress, bytesDownloaded = totalBytesDownloaded))
                    }
                }
            }

            emit(ModelDownloadState.Verifying)
            // Final check
            if (isModelReady()) {
                emit(ModelDownloadState.Initializing)
                emit(ModelDownloadState.Ready)
            } else {
                emit(ModelDownloadState.Error("Verification failed: incomplete files"))
            }

        } catch (e: Exception) {
            emit(ModelDownloadState.Error(e.message ?: "Download failed"))
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun downloadFile(urlStr: String, destFile: File, onProgress: suspend (Long) -> Unit) {
        withContext(Dispatchers.IO) {
            val url = URL(urlStr)
            val connection = url.openConnection() as HttpURLConnection
            try {
                connection.connectTimeout = 30000
                connection.readTimeout = 30000
                connection.connect()
                
                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    throw IOException("Server returned HTTP ${connection.responseCode} for $urlStr")
                }

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
    }

    override fun getModelPath(): String? {
        return if (isModelReady()) modelDir.absolutePath else null
    }

    companion object {
        private const val MODEL_DIR_NAME = "llama-3.2-3b-npu-turbo"
    }
}
