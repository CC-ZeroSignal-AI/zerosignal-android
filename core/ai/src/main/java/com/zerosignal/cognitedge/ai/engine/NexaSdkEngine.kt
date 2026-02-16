package com.zerosignal.cognitedge.ai.engine

import com.nexa.sdk.LlmWrapper
import com.nexa.sdk.bean.GenerationConfig
import com.nexa.sdk.bean.LlmCreateInput
import com.nexa.sdk.bean.LlmStreamResult
import com.nexa.sdk.bean.ModelConfig
import com.zerosignal.cognitedge.domain.model.ResponseChunk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Real Nexa SDK wrapper for on-device LLM inference.
 * Uses LlmWrapper with Qualcomm NPU (or CPU/GPU fallback) plugin.
 */
class NexaSdkEngine(
    private val modelPath: String,
    private val pluginId: String = "npu"
) : NexaInferenceEngine {

    private val mutex = Mutex()

    @Volatile
    private var llmWrapper: LlmWrapper? = null

    private suspend fun ensureLoaded(): LlmWrapper {
        llmWrapper?.let { return it }
        return mutex.withLock {
            llmWrapper?.let { return it }
            
            // Create ModelConfig with default values
            val modelConfig = ModelConfig()
            
            // Create LlmCreateInput
            // Constructor: (model_name, model_path, tokenizer_path, config, plugin_id, device_id)
            // We use modelPath for both model_path and tokenizer_path as it's a directory
            val input = LlmCreateInput(
                model_name = "Llama3.2-3B-NPU-Turbo",
                model_path = modelPath,
                tokenizer_path = modelPath,
                config = modelConfig,
                plugin_id = pluginId,
                device_id = "cpu" // Standard device ID for initialization context, actual execution uses plugin
            )

            // Build LlmWrapper using suspending builder
            val result = LlmWrapper.builder()
                .llmCreateInput(input)
                .build() // This is a suspending function returning Result<LlmWrapper>
            
            val wrapper = result.getOrThrow()
            llmWrapper = wrapper
            wrapper
        }
    }

    override fun streamCompletion(prompt: String): Flow<ResponseChunk> = flow {
        val wrapper = ensureLoaded()
        
        // Create GenerationConfig
        val config = GenerationConfig()
        config.maxTokens = 1024

        wrapper.generateStreamFlow(prompt, config).collect { result ->
            when (result) {
                is LlmStreamResult.Token -> {
                    emit(ResponseChunk(token = result.text, isFinal = false))
                }
                is LlmStreamResult.Completed -> {
                    emit(ResponseChunk(token = "", isFinal = true))
                }
                is LlmStreamResult.Error -> {
                    throw RuntimeException("Inference error: ${result.throwable.message}")
                }
            }
        }
    }

    fun unload() {
        // We can't use mutex.withLock in a non-suspend function if unload needs to be blocking
        // But unload is usually called from lifecycle methods which can launch a coroutine
        // For now, we'll just check null safely
        val wrapper = llmWrapper
        if (wrapper != null) {
            try {
                wrapper.close()
            } catch (e: Exception) {
                // Ignore close errors
            } finally {
                llmWrapper = null
            }
        }
    }
}
