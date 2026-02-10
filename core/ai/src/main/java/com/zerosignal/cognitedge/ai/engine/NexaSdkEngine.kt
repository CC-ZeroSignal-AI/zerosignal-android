package com.zerosignal.cognitedge.ai.engine

import com.zerosignal.cognitedge.domain.model.ResponseChunk
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Real Nexa SDK wrapper. When the ai.nexa:core dependency is available,
 * this will use NexaSdk.init(), LlmWrapper.builder() with plugin_id="npu",
 * and generateStreamFlow().
 *
 * Until the SDK artifact is available, this acts as a stub that delegates
 * to FakeNexaInferenceEngine behavior, allowing the full architecture to be
 * tested on emulator/non-Snapdragon devices.
 */
class NexaSdkEngine(
    private val modelPath: String,
    private val pluginId: String = "npu"
) : NexaInferenceEngine {

    // TODO: Replace with real Nexa SDK calls when artifact is available
    // private var llmWrapper: LlmWrapper? = null

    override fun streamCompletion(prompt: String): Flow<ResponseChunk> = flow {
        // Real implementation would be:
        // val wrapper = llmWrapper ?: throw IllegalStateException("Model not loaded")
        // wrapper.generateStreamFlow(prompt).collect { token ->
        //     emit(ResponseChunk(token = token, isFinal = false))
        // }
        // emit(ResponseChunk(token = "", isFinal = true))

        // Stub: delegate to fake streaming behavior until SDK is integrated
        val contextSection = prompt.substringAfter("Context:")
            .substringBefore("<|eot_id|>")
            .substringBefore("Question:") // fallback for legacy prompt format
        val contextLines = contextSection.lines()
            .map { it.substringAfter("]", it).trim() }
            .filter { it.isNotBlank() }
        val answer = buildString {
            append("[NPU:$pluginId] Key actions:\n")
            contextLines.forEach { append("- $it\n") }
        }
        val tokens = answer.split(" ")
        for ((index, token) in tokens.withIndex()) {
            emit(ResponseChunk(token = "$token ", isFinal = index == tokens.lastIndex))
            delay(30) // Simulate NPU inference speed (~30+ tps)
        }
    }
}
