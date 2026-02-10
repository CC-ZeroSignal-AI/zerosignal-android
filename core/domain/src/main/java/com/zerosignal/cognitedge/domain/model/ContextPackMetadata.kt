package com.zerosignal.cognitedge.domain.model

data class ContextPackMetadata(
    val packId: String,
    val language: String,
    val domain: String,
    val embeddingDimensions: Int,
    val version: Int
)
