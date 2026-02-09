package com.zerosignal.cognitedge.rag

/** Creates the prompt that feeds into the Nexa LLM once the native bridge is available. */
class PromptBuilder {
    fun buildPrompt(question: String, contexts: List<ContextChunk>): String {
        val builder = StringBuilder()
        builder.appendLine("You are CognitEdge, an offline expert assistant. Use the provided snippets only.")
        builder.appendLine("Context:")
        contexts.forEachIndexed { index, chunk ->
            builder.appendLine("[${index + 1}] ${chunk.text}")
        }
        builder.appendLine()
        builder.appendLine("Question: $question")
        builder.appendLine("Answer:")
        return builder.toString()
    }
}
