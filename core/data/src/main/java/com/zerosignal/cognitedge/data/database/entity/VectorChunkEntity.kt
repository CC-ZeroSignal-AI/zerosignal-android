package com.zerosignal.cognitedge.data.database.entity

import io.objectbox.annotation.Entity
import io.objectbox.annotation.HnswIndex
import io.objectbox.annotation.Id

@Entity
data class VectorChunkEntity(
    @Id var objectBoxId: Long = 0,
    var chunkId: String = "",
    var text: String = "",
    var packId: String = "",
    var metadataJson: String = "{}",
    @HnswIndex(
        dimensions = 768,
        neighborsPerNode = 30,
        indexingSearchCount = 200
    )
    var embedding: FloatArray = FloatArray(768)
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VectorChunkEntity) return false
        return chunkId == other.chunkId
    }

    override fun hashCode(): Int = chunkId.hashCode()
}
