package com.zerosignal.cognitedge.ai.repository

import android.content.Context
import com.zerosignal.cognitedge.domain.model.ModelDownloadState
import com.zerosignal.cognitedge.domain.repository.ModelRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NexaModelRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : ModelRepository {

    private val modelsDir: File
        get() = File(context.noBackupFilesDir, "models").also { it.mkdirs() }

    private val modelFile: File
        get() = File(modelsDir, MODEL_FILENAME)

    override fun isModelReady(): Boolean {
        return modelFile.exists() && modelFile.length() > 0
    }

    override fun downloadModel(): Flow<ModelDownloadState> = flow {
        emit(ModelDownloadState.NotStarted)

        if (isModelReady()) {
            emit(ModelDownloadState.Initializing)
            emit(ModelDownloadState.Ready)
            return@flow
        }

        try {
            // TODO: Replace with real Nexa SDK download when artifact is available
            // Real implementation: NexaSdk.downloadModel(MODEL_ID) { progress, bytes ->
            //     emit(ModelDownloadState.Downloading(progress, bytes))
            // }

            // Simulated download progress for development/emulator
            for (i in 1..10) {
                emit(ModelDownloadState.Downloading(
                    progress = i / 10f,
                    bytesDownloaded = i * 300_000_000L
                ))
                delay(500) // Simulated download time
            }

            emit(ModelDownloadState.Verifying)
            delay(300)

            // Create a marker file to indicate "download complete"
            modelFile.writeText("model-placeholder-${System.currentTimeMillis()}")

            emit(ModelDownloadState.Initializing)
            delay(200)

            emit(ModelDownloadState.Ready)
        } catch (e: Exception) {
            emit(ModelDownloadState.Error(e.message ?: "Download failed"))
        }
    }

    override fun getModelPath(): String? {
        return if (isModelReady()) modelFile.absolutePath else null
    }

    companion object {
        private const val MODEL_FILENAME = "llama-3.2-3b-npu-turbo.bin"
        const val MODEL_ID = "Llama3.2-3B-NPU-Turbo"
    }
}
