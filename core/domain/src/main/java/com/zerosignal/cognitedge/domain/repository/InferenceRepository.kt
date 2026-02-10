package com.zerosignal.cognitedge.domain.repository

import com.zerosignal.cognitedge.domain.model.ResponseChunk
import kotlinx.coroutines.flow.Flow

interface InferenceRepository {
    fun streamCompletion(prompt: String): Flow<ResponseChunk>
}
