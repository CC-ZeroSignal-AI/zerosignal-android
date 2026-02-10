package com.zerosignal.cognitedge.data.prompt

import com.zerosignal.cognitedge.domain.model.ContextChunk
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PromptBuilder @Inject constructor() {
    fun buildPrompt(question: String, contexts: List<ContextChunk>): String {
        val contextBlock = contexts.mapIndexed { index, chunk ->
            "[${index + 1}] ${chunk.text}"
        }.joinToString("\n")

        return """<|begin_of_text|><|start_header_id|>system<|end_header_id|>

You are ZeroSignal AI, an offline expert assistant running on-device. Use ONLY the provided context snippets to answer. If the context does not contain enough information, say so.

Context:
$contextBlock
<|eot_id|><|start_header_id|>user<|end_header_id|>

$question<|eot_id|><|start_header_id|>assistant<|end_header_id|>

"""
    }
}
