package com.zerosignal.cognitedge.ai.engine

import com.zerosignal.cognitedge.domain.model.NexaModelConfig
import com.zerosignal.cognitedge.domain.model.ResponseChunk
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

class NexaBridgeEngine(
    private val runtime: NexaRuntime,
    private val modelConfig: NexaModelConfig
) : NexaInferenceEngine {

    private val isLoaded = AtomicBoolean(false)

    override fun streamCompletion(prompt: String): Flow<ResponseChunk> = flow {
        if (isLoaded.compareAndSet(false, true)) {
            runtime.loadModel(modelConfig)
        }
        emitAll(runtime.streamCompletion(prompt))
    }
}
