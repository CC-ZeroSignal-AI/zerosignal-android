package com.zerosignal.cognitedge.ai.repository

import com.zerosignal.cognitedge.ai.engine.NexaInferenceEngine
import com.zerosignal.cognitedge.domain.model.ResponseChunk
import com.zerosignal.cognitedge.domain.repository.InferenceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NexaInferenceRepository @Inject constructor(
    private val engine: NexaInferenceEngine
) : InferenceRepository {
    override fun streamCompletion(prompt: String): Flow<ResponseChunk> {
        return engine.streamCompletion(prompt)
    }
}
