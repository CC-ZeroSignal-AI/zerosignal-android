package com.zerosignal.cognitedge.data.loader

import com.zerosignal.cognitedge.data.database.entity.VectorChunkEntity
import com.zerosignal.cognitedge.domain.model.ContextPack
import io.objectbox.Box
import io.objectbox.BoxStore
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContextPackIngester @Inject constructor(
    private val boxStore: BoxStore,
    private val loader: ContextPackLoader
) {
    fun ingestIfNeeded(): ContextPack {
        val pack = loader.loadFromAssets()
        val box: Box<VectorChunkEntity> = boxStore.boxFor(VectorChunkEntity::class.java)

        if (box.count() == 0L) {
            val entities = pack.chunks.map { chunk ->
                VectorChunkEntity(
                    chunkId = chunk.id,
                    text = chunk.text,
                    packId = pack.metadata.packId,
                    metadataJson = JSONObject(chunk.metadata).toString(),
                    embedding = padTo768(chunk.embedding)
                )
            }
            box.put(entities)
        }
        return pack
    }

    companion object {
        private const val TARGET_DIMS = 768

        fun padTo768(source: FloatArray): FloatArray {
            if (source.size >= TARGET_DIMS) return source.copyOf(TARGET_DIMS)
            val padded = FloatArray(TARGET_DIMS)
            source.copyInto(padded)
            return padded
        }
    }
}
