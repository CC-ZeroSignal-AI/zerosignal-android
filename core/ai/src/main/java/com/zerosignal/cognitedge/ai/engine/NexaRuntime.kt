package com.zerosignal.cognitedge.ai.engine

import com.zerosignal.cognitedge.domain.model.NexaModelConfig
import com.zerosignal.cognitedge.domain.model.ResponseChunk
import kotlinx.coroutines.flow.Flow

interface NexaRuntime {
    suspend fun loadModel(config: NexaModelConfig)
    fun streamCompletion(prompt: String): Flow<ResponseChunk>
}
