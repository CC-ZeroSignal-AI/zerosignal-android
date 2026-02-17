package com.zerosignal.cognitedge.ai.engine

import com.nexa.sdk.LlmWrapper
import com.zerosignal.cognitedge.ai.util.AiFileLog
import com.nexa.sdk.bean.ChatMessage
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
 * Matches the official Nexa SDK demo app pattern:
 * - Tries NPU plugin first with .nexa model files
 * - Falls back to cpu_gpu plugin with GGUF model
 * - Uses applyChatTemplate() before generateStreamFlow()
 */
class NexaSdkEngine(
    private val npuModelPath: String,
    private val ggufModelPath: String,
    private val nativeLibDir: String
) : NexaInferenceEngine {

    companion object {
        private const val TAG = "NexaSdkEngine"
    }

    private val mutex = Mutex()

    @Volatile
    private var llmWrapper: LlmWrapper? = null

    @Volatile
    private var activePlugin: String? = null

    private suspend fun ensureLoaded(): LlmWrapper {
        llmWrapper?.let {
            AiFileLog.d(TAG, "Model already loaded (plugin=$activePlugin), reusing")
            return it
        }
        return mutex.withLock {
            llmWrapper?.let {
                AiFileLog.d(TAG, "Model already loaded after lock (plugin=$activePlugin), reusing")
                return it
            }

            AiFileLog.i(TAG, "--- Model loading started ---")
            AiFileLog.i(TAG, "NPU model path:  '${npuModelPath.ifEmpty { "(empty)" }}'")
            AiFileLog.i(TAG, "GGUF model path: '${ggufModelPath.ifEmpty { "(empty)" }}'")
            AiFileLog.i(TAG, "Native lib dir:  '$nativeLibDir'")

            // Try NPU first if model path is available
            if (npuModelPath.isNotEmpty()) {
                AiFileLog.i(TAG, "Step 1: Attempting NPU load...")
                try {
                    val wrapper = createNpuWrapper()
                    llmWrapper = wrapper
                    activePlugin = "npu"
                    AiFileLog.i(TAG, "NPU load SUCCESS — model active via NPU plugin")
                    return wrapper
                } catch (e: Exception) {
                    AiFileLog.w(TAG, "NPU load FAILED: ${e.message}")
                    AiFileLog.w(TAG, "NPU exception class: ${e.javaClass.simpleName}")
                    AiFileLog.i(TAG, "Falling back to CPU/GPU...")
                }
            } else {
                AiFileLog.i(TAG, "Step 1: NPU skipped — no model path provided")
            }

            // Fall back to CPU/GPU with GGUF model
            if (ggufModelPath.isNotEmpty()) {
                AiFileLog.i(TAG, "Step 2: Attempting CPU/GPU load with GGUF...")
                try {
                    val wrapper = createCpuGpuWrapper()
                    llmWrapper = wrapper
                    activePlugin = "cpu_gpu"
                    AiFileLog.i(TAG, "CPU/GPU load SUCCESS — model active via cpu_gpu plugin")
                    return wrapper
                } catch (e: Exception) {
                    AiFileLog.e(TAG, "CPU/GPU load FAILED: ${e.message}", e)
                    throw RuntimeException(
                        "Failed to load model on both NPU and CPU/GPU: ${e.message}", e
                    )
                }
            } else {
                AiFileLog.e(TAG, "Step 2: CPU/GPU skipped — no GGUF model path provided")
            }

            AiFileLog.e(TAG, "--- Model loading FAILED — no usable model found ---")
            throw RuntimeException("No model available — NPU path: '$npuModelPath', GGUF path: '$ggufModelPath'")
        }
    }

    private suspend fun createNpuWrapper(): LlmWrapper {
        AiFileLog.i(TAG, "Loading NPU model from: $npuModelPath")

        // Match Nexa demo: explicitly set npu_lib_folder_path and npu_model_folder_path
        val modelConfig = ModelConfig(
            nCtx = 1024,
            npu_lib_folder_path = nativeLibDir,
            npu_model_folder_path = npuModelPath
        )

        // model_path should be the actual .nexa file (e.g. files-1-2.nexa)
        // The SDK uses nexa.manifest to derive the model name
        val manifestPath = "$npuModelPath/nexa.manifest"
        val input = LlmCreateInput(
            model_name = "Llama3.2-3B-NPU-Turbo",
            model_path = manifestPath,
            tokenizer_path = npuModelPath,
            config = modelConfig,
            plugin_id = "npu",
            device_id = ""
        )

        val result = LlmWrapper.builder()
            .llmCreateInput(input)
            .build()
        return result.getOrThrow()
    }

    private suspend fun createCpuGpuWrapper(): LlmWrapper {
        AiFileLog.i(TAG, "Loading GGUF model from: $ggufModelPath (plugin=cpu_gpu)")

        // Match Nexa demo: nCtx=1024, nGpuLayers=0 for CPU-only
        val modelConfig = ModelConfig(
            nCtx = 1024,
            nGpuLayers = 0
        )

        val input = LlmCreateInput(
            model_name = "",
            model_path = ggufModelPath,
            tokenizer_path = null,
            config = modelConfig,
            plugin_id = "cpu_gpu"
        )

        val result = LlmWrapper.builder()
            .llmCreateInput(input)
            .build()
        return result.getOrThrow()
    }

    override fun streamCompletion(prompt: String): Flow<ResponseChunk> = flow {
        AiFileLog.d(TAG, "streamCompletion called — prompt length=${prompt.length}")
        val wrapper = ensureLoaded()
        AiFileLog.d(TAG, "Using plugin: $activePlugin")

        // Apply chat template like the Nexa demo does
        val chatMessages = arrayOf(
            ChatMessage("user", prompt)
        )

        val formattedPrompt = try {
            val templateResult = wrapper.applyChatTemplate(chatMessages, null, false)
            templateResult.getOrNull()?.formattedText ?: prompt
        } catch (e: Exception) {
            AiFileLog.w(TAG, "applyChatTemplate failed, using raw prompt: ${e.message}")
            prompt
        }

        AiFileLog.d(TAG, "Formatted prompt length=${formattedPrompt.length}")

        // Match Nexa demo GenerationConfig
        val config = GenerationConfig(
            maxTokens = 2048
        )

        var tokenCount = 0
        wrapper.generateStreamFlow(formattedPrompt, config).collect { result ->
            when (result) {
                is LlmStreamResult.Token -> {
                    tokenCount++
                    emit(ResponseChunk(token = result.text, isFinal = false))
                }
                is LlmStreamResult.Completed -> {
                    AiFileLog.d(TAG, "Stream completed — $tokenCount tokens generated (plugin=$activePlugin)")
                    emit(ResponseChunk(token = "", isFinal = true))
                }
                is LlmStreamResult.Error -> {
                    AiFileLog.e(TAG, "Inference error", result.throwable)
                    throw RuntimeException("Inference error: ${result.throwable.message}")
                }
            }
        }
    }

    fun unload() {
        AiFileLog.i(TAG, "Unloading LLM model (plugin=$activePlugin)")
        val wrapper = llmWrapper
        if (wrapper != null) {
            try {
                wrapper.close()
                AiFileLog.i(TAG, "LLM model unloaded")
            } catch (e: Exception) {
                AiFileLog.w(TAG, "Error closing LlmWrapper", e)
            } finally {
                llmWrapper = null
                activePlugin = null
            }
        }
    }
}
