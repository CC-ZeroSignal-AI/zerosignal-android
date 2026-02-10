package com.zerosignal.cognitedge.ai.engine

import com.zerosignal.cognitedge.domain.model.ResponseChunk
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class FakeNexaInferenceEngine @Inject constructor() : NexaInferenceEngine {
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
        val contextSection = prompt.substringAfter("Context:")
            .substringBefore("<|eot_id|>")
            .substringBefore("Question:") // fallback for legacy prompt format
        return contextSection.lines()
            .map { it.substringAfter("]", it).trim() }
            .filter { it.isNotBlank() }
    }
}
