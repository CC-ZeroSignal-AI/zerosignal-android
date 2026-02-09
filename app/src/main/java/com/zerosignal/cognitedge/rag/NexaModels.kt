package com.zerosignal.cognitedge.rag

/**
 * Model metadata derived from the Nexa SDK Android overview.
 * Keeping this in Kotlin makes it easy to toggle between Snapdragon-optimized builds.
 */
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

object NexaModelCatalog {
    private val models = listOf(
        NexaModelConfig(
            id = "omninneural-4b-q8",
            displayName = "OmniNeural 4B Q8",
            type = ModelType.LLM,
            parameterCountBillions = 4.0f,
            contextWindow = 8192,
            quantization = "INT8 weight-only",
            assetBundle = "omninneural_4b_q8.bin",
            recommendedUse = "Default multimodal-capable LLM tuned for factual responses on Snapdragon 8 Gen 4/Elite"
        ),
        NexaModelConfig(
            id = "granite-4-micro-3b-int4",
            displayName = "Granite 4.0 Micro 3B INT4",
            type = ModelType.LLM,
            parameterCountBillions = 3.0f,
            contextWindow = 4096,
            quantization = "INT4 NPU-friendly",
            assetBundle = "granite_micro_3b_int4.bin",
            recommendedUse = "Battery-conscious deployments needing steady 30+ tps on Snapdragon 8 series"
        ),
        NexaModelConfig(
            id = "llama-3.2-3b-q4",
            displayName = "Llama 3.2 3B Q4",
            type = ModelType.LLM,
            parameterCountBillions = 3.0f,
            contextWindow = 8192,
            quantization = "Q4_K_M",
            assetBundle = "llama_3_2_3b_q4_km.bin",
            recommendedUse = "Open-weight fallback for developer builds"
        ),
        NexaModelConfig(
            id = "jina-v2-rerank-npu",
            displayName = "Jina v2 Rerank NPU",
            type = ModelType.RERANKER,
            parameterCountBillions = 0.15f,
            contextWindow = 2048,
            quantization = "INT8",
            assetBundle = "jina_v2_rerank_npu.bin",
            recommendedUse = "Optional second-stage reranker for high recall context packs"
        )
    )

    fun defaultLlm(): NexaModelConfig = models.first { it.type == ModelType.LLM }

    fun byId(id: String): NexaModelConfig? = models.firstOrNull { it.id == id }

    fun allModels(): List<NexaModelConfig> = models
}
