package com.zerosignal.cognitedge.domain.model

data class ResponseChunk(
    val token: String,
    val isFinal: Boolean = false
)
