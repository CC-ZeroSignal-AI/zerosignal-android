package com.zerosignal.cognitedge.ai.engine

import com.zerosignal.cognitedge.domain.model.ResponseChunk
import kotlinx.coroutines.flow.Flow

interface NexaInferenceEngine {
    fun streamCompletion(prompt: String): Flow<ResponseChunk>
}
