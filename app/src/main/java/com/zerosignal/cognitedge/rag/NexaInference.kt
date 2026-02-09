package com.zerosignal.cognitedge.rag

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** Simple data holder used to stream partial tokens back to the UI. */
data class ResponseChunk(
    val token: String,
    val isFinal: Boolean = false
)

/**
 * Abstraction over the Nexa SDK bridge so we can plug the real JNI backed implementation later.
 */
interface NexaInferenceEngine {
    fun streamCompletion(prompt: String): Flow<ResponseChunk>
}

/**
 * Fake implementation that fuses retrieved snippets with a templated answer so the rest of the stack
 * can be tested on regular Android devices before the actual SDK is dropped in.
 */
class FakeNexaInferenceEngine : NexaInferenceEngine {
    override fun streamCompletion(prompt: String): Flow<ResponseChunk> = flow {
        val contextSummary = parseContext(prompt)
        val answer = buildString {
            append("Key actions:\n")
            contextSummary.forEach { append("- $it\n") }
        }
        val tokens = answer.split(" ")
        for ((index, token) in tokens.withIndex()) {
            emit(ResponseChunk(token = "$token ", isFinal = index == tokens.lastIndex))
            delay(40)
        }
    }

    private fun parseContext(prompt: String): List<String> {
        val contextSection = prompt.substringAfter("Context:").substringBefore("Question:")
        return contextSection.lines()
            .map { it.substringAfter("]", it).trim() }
            .filter { it.isNotBlank() }
    }
}
