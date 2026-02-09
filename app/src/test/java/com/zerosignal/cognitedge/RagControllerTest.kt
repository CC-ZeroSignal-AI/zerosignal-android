package com.zerosignal.cognitedge

import com.zerosignal.cognitedge.rag.ContextChunk
import com.zerosignal.cognitedge.rag.ContextPack
import com.zerosignal.cognitedge.rag.ContextPackMetadata
import com.zerosignal.cognitedge.rag.ContextRetriever
import com.zerosignal.cognitedge.rag.PromptBuilder
import com.zerosignal.cognitedge.rag.QueryVectorizer
import com.zerosignal.cognitedge.rag.RagController
import com.zerosignal.cognitedge.rag.ResponseChunk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

private class ImmediateEngine : com.zerosignal.cognitedge.rag.NexaInferenceEngine {
    override fun streamCompletion(prompt: String): Flow<ResponseChunk> = kotlinx.coroutines.flow.flow {
        emit(ResponseChunk(prompt, true))
    }
}

class RagControllerTest {
    @Test
    fun `retrieval selects chunk with matching embedding`() = runBlocking {
        val metadata = ContextPackMetadata("test", "en", "general", 2, 1)
        val chunks = listOf(
            ContextChunk("1", "alpha", floatArrayOf(1f, 0f), emptyMap()),
            ContextChunk("2", "beta", floatArrayOf(0f, 1f), emptyMap())
        )
        val pack = ContextPack(metadata, chunks)
        val controller = RagController(
            contextPack = pack,
            vectorizer = QueryVectorizer(metadata.embeddingDimensions),
            retriever = ContextRetriever(pack),
            promptBuilder = PromptBuilder(),
            inference = ImmediateEngine()
        )
        val response = controller.ask("alpha").toList().first()
        assertTrue(response.token.contains("alpha"))
    }
}
