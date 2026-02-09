package com.zerosignal.cognitedge.rag

import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

/** Wraps the future Nexa JNI runtime so it matches the RagController expectations. */
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
