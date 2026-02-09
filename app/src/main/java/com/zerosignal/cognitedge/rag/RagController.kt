package com.zerosignal.cognitedge.rag

import kotlinx.coroutines.flow.Flow

/** Central orchestrator that ties retrieval with the (future) Nexa-powered generator. */
class RagController(
    private val contextPack: ContextPack,
    private val vectorizer: QueryVectorizer,
    private val retriever: ContextRetriever,
    private val promptBuilder: PromptBuilder,
    private val inference: NexaInferenceEngine
) {
    fun ask(question: String, topK: Int = 2): Flow<ResponseChunk> {
        require(question.isNotBlank()) { "Question cannot be empty" }
        val queryVector = vectorizer.encode(question)
        val matches = retriever.topKMatches(queryVector, topK)
        val prompt = promptBuilder.buildPrompt(question, matches)
        return inference.streamCompletion(prompt)
    }
}
