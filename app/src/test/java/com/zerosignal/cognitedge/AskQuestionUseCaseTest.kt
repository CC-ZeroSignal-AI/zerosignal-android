package com.zerosignal.cognitedge

import com.zerosignal.cognitedge.domain.model.ContextChunk
import com.zerosignal.cognitedge.domain.model.ContextPack
import com.zerosignal.cognitedge.domain.model.ContextPackMetadata
import com.zerosignal.cognitedge.domain.model.ResponseChunk
import com.zerosignal.cognitedge.domain.repository.ContextRepository
import com.zerosignal.cognitedge.domain.repository.InferenceRepository
import com.zerosignal.cognitedge.domain.usecase.AskQuestionUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class AskQuestionUseCaseTest {

    @Test
    fun `retrieval selects chunk with matching embedding`() = runBlocking {
        val metadata = ContextPackMetadata("test", "en", "general", 2, 1)
        val chunks = listOf(
            ContextChunk("1", "alpha", floatArrayOf(1f, 0f), emptyMap()),
            ContextChunk("2", "beta", floatArrayOf(0f, 1f), emptyMap())
        )
        val pack = ContextPack(metadata, chunks)

        val contextRepo = object : ContextRepository {
            override fun loadContextPack(): ContextPack = pack
            override fun findRelevantChunks(query: String, topK: Int): List<ContextChunk> {
                return chunks.filter { it.text.contains(query, ignoreCase = true) }.take(topK)
            }
        }

        val inferenceRepo = object : InferenceRepository {
            override fun streamCompletion(prompt: String): Flow<ResponseChunk> = flow {
                emit(ResponseChunk(prompt, true))
            }
        }

        val useCase = AskQuestionUseCase(
            contextRepository = contextRepo,
            inferenceRepository = inferenceRepo,
            promptBuilder = { question, contexts ->
                buildString {
                    appendLine("Context:")
                    contexts.forEachIndexed { index, chunk ->
                        appendLine("[${index + 1}] ${chunk.text}")
                    }
                    appendLine("Question: $question")
                }
            }
        )

        val result = useCase.invoke("alpha")
        assertTrue(result.sourcesUsed.contains("1"))
        val response = result.responseFlow.toList().first()
        assertTrue(response.token.contains("alpha"))
    }
}
