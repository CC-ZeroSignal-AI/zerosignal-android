package com.zerosignal.cognitedge.rag

import java.security.MessageDigest

/**
 * Placeholder encoder so the pipeline can run on-device before the Nexa embedder is integrated.
 * Replaces words with deterministic buckets based on SHA-256 hashes and builds a simple bag-of-words vector.
 */
class QueryVectorizer(private val dimensions: Int) {
    private val digest = MessageDigest.getInstance("SHA-256")

    fun encode(text: String): FloatArray {
        val vector = FloatArray(dimensions)
        val tokens = text.lowercase().split(" ", "\n", "\t", "\r", ",", ".", "?", "!").filter { it.isNotBlank() }
        if (tokens.isEmpty()) return vector
        val weight = 1f / tokens.size
        for (token in tokens) {
            val hash = digest.digest(token.toByteArray())
            val bucket = ((hash[0].toInt() and 0xFF) shl 8 or (hash[1].toInt() and 0xFF)) % dimensions
            val idx = if (bucket < 0) bucket + dimensions else bucket
            vector[idx] += weight
        }
        return vector
    }
}
