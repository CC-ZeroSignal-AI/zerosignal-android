package com.zerosignal.cognitedge.domain.model

data class ContextPack(
    val metadata: ContextPackMetadata,
    val chunks: List<ContextChunk>
)
