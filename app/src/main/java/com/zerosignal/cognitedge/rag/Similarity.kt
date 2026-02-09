package com.zerosignal.cognitedge.rag

import kotlin.math.sqrt

object Similarity {
    fun cosine(a: FloatArray, b: FloatArray): Float {
        require(a.size == b.size) { "Vectors must be the same size" }
        var dot = 0f
        var normA = 0f
        var normB = 0f
        for (i in a.indices) {
            dot += a[i] * b[i]
            normA += a[i] * a[i]
            normB += b[i] * b[i]
        }
        if (normA == 0f || normB == 0f) return 0f
        return (dot / (sqrt(normA) * sqrt(normB))).coerceIn(-1f, 1f)
    }
}
