package com.zerosignal.cognitedge.rag

/** Interface that the upcoming Nexa SDK JNI layer will implement. */
interface NexaRuntime {
    /** Loads/initializes the desired model weights on the Hexagon NPU. */
    suspend fun loadModel(config: NexaModelConfig)

    /** Streams decoded tokens for the previously loaded model. */
    fun streamCompletion(prompt: String): kotlinx.coroutines.flow.Flow<ResponseChunk>
}
