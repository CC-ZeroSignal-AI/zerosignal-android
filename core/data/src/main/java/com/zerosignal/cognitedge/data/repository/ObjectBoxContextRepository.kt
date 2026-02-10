package com.zerosignal.cognitedge.data.repository

import com.zerosignal.cognitedge.data.database.entity.VectorChunkEntity
import com.zerosignal.cognitedge.data.database.entity.VectorChunkEntity_
import com.zerosignal.cognitedge.data.loader.ContextPackIngester
import com.zerosignal.cognitedge.data.search.QueryVectorizer
import com.zerosignal.cognitedge.domain.model.ContextChunk
import com.zerosignal.cognitedge.domain.model.ContextPack
import com.zerosignal.cognitedge.domain.repository.ContextRepository
import io.objectbox.BoxStore
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObjectBoxContextRepository @Inject constructor(
    private val boxStore: BoxStore,
    private val ingester: ContextPackIngester
) : ContextRepository {

    private val pack: ContextPack by lazy { ingester.ingestIfNeeded() }
    private val vectorizer: QueryVectorizer by lazy { QueryVectorizer(VECTOR_DIMS) }

    override fun loadContextPack(): ContextPack = pack

    override fun findRelevantChunks(query: String, topK: Int): List<ContextChunk> {
        // Ensure pack is loaded (triggers ingestion)
        pack

        val queryVector = vectorizer.encode(query)
        val paddedQuery = ContextPackIngester.padTo768(queryVector)

        val box = boxStore.boxFor(VectorChunkEntity::class.java)
        val results = box.query(
            VectorChunkEntity_.embedding.nearestNeighbors(paddedQuery, topK)
        ).build().find()

        return results.map { entity -> entity.toDomain() }
    }

    companion object {
        private const val VECTOR_DIMS = 768
    }
}

private fun VectorChunkEntity.toDomain(): ContextChunk {
    val metaMap = try {
        val json = JSONObject(metadataJson)
        val map = mutableMapOf<String, String>()
        json.keys().forEach { key -> map[key] = json.getString(key) }
        map.toMap()
    } catch (_: Exception) {
        emptyMap()
    }
    return ContextChunk(
        id = chunkId,
        text = text,
        embedding = embedding,
        metadata = metaMap
    )
}
