package com.zerosignal.cognitedge.data.repository

import com.zerosignal.cognitedge.data.loader.ContextPackLoader
import com.zerosignal.cognitedge.data.search.ContextRetriever
import com.zerosignal.cognitedge.data.search.QueryVectorizer
import com.zerosignal.cognitedge.domain.model.ContextChunk
import com.zerosignal.cognitedge.domain.model.ContextPack
import com.zerosignal.cognitedge.domain.repository.ContextRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalContextRepository @Inject constructor(
    private val loader: ContextPackLoader
) : ContextRepository {

    private val pack: ContextPack by lazy { loader.loadFromAssets() }
    private val vectorizer: QueryVectorizer by lazy { QueryVectorizer(pack.metadata.embeddingDimensions) }
    private val retriever: ContextRetriever by lazy { ContextRetriever(pack) }

    override fun loadContextPack(): ContextPack = pack

    override fun findRelevantChunks(query: String, topK: Int): List<ContextChunk> {
        val queryVector = vectorizer.encode(query)
        return retriever.topKMatches(queryVector, topK)
    }
}
