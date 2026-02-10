package com.zerosignal.cognitedge.domain.usecase

import com.zerosignal.cognitedge.domain.model.ContextChunk
import com.zerosignal.cognitedge.domain.model.ResponseChunk
import com.zerosignal.cognitedge.domain.repository.ContextRepository
import com.zerosignal.cognitedge.domain.repository.InferenceRepository
import kotlinx.coroutines.flow.Flow

data class AskResult(
    val responseFlow: Flow<ResponseChunk>,
    val sourcesUsed: List<String>
)

class AskQuestionUseCase(
    private val contextRepository: ContextRepository,
    private val inferenceRepository: InferenceRepository,
    private val promptBuilder: (question: String, contexts: List<ContextChunk>) -> String
) {
    fun invoke(question: String, topK: Int = 2): AskResult {
        require(question.isNotBlank()) { "Question cannot be empty" }
        val matches = contextRepository.findRelevantChunks(question, topK)
        val prompt = promptBuilder(question, matches)
        return AskResult(
            responseFlow = inferenceRepository.streamCompletion(prompt),
            sourcesUsed = matches.map { it.id }
        )
    }
}
