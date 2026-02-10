package com.zerosignal.cognitedge.data.search

import java.security.MessageDigest

class QueryVectorizer(private val dimensions: Int = 768) {
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
