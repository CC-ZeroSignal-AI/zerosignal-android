package com.zerosignal.cognitedge.domain.model

data class NexaModelConfig(
    val id: String,
    val displayName: String,
    val type: ModelType,
    val parameterCountBillions: Float,
    val contextWindow: Int,
    val quantization: String,
    val assetBundle: String,
    val recommendedUse: String
)

enum class ModelType { LLM, RERANKER, EMBEDDER }
