package com.zerosignal.cognitedge.domain.repository

import com.zerosignal.cognitedge.domain.model.ModelDownloadState
import kotlinx.coroutines.flow.Flow

interface ModelRepository {
    fun isModelReady(): Boolean
    fun downloadModel(): Flow<ModelDownloadState>
    fun getModelPath(): String?
}
