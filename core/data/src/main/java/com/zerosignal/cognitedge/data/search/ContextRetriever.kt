package com.zerosignal.cognitedge.data.search

import com.zerosignal.cognitedge.domain.model.ContextChunk
import com.zerosignal.cognitedge.domain.model.ContextPack

class ContextRetriever(private val contextPack: ContextPack) {

    fun topKMatches(queryVector: FloatArray, k: Int = 3): List<ContextChunk> {
        if (contextPack.chunks.isEmpty()) return emptyList()
        val scored = contextPack.chunks.map { chunk ->
            chunk to Similarity.cosine(queryVector, chunk.embedding)
        }
        return scored.sortedByDescending { it.second }.take(k).map { it.first }
    }
}
