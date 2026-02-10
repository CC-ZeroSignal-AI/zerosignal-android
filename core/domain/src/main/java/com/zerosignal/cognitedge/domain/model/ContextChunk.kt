package com.zerosignal.cognitedge.domain.model

data class ContextChunk(
    val id: String,
    val text: String,
    val embedding: FloatArray,
    val metadata: Map<String, String>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ContextChunk) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
