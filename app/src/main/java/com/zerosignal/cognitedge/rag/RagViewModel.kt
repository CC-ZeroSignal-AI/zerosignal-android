package com.zerosignal.cognitedge.rag

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RagUiState(
    val isThinking: Boolean = false,
    val answer: String = "",
    val errorMessage: String? = null
)

class RagViewModel(private val controller: RagController) : ViewModel() {
    private val _uiState = MutableStateFlow(RagUiState())
    val uiState: StateFlow<RagUiState> = _uiState.asStateFlow()

    private var inFlightJob: Job? = null

    fun ask(question: String) {
        inFlightJob?.cancel()
        inFlightJob = viewModelScope.launch {
            try {
                if (question.isBlank()) {
                    _uiState.value = RagUiState(errorMessage = "Question cannot be blank")
                    return@launch
                }
                _uiState.value = RagUiState(isThinking = true)
                val builder = StringBuilder()
                controller.ask(question).collect { chunk ->
                    builder.append(chunk.token)
                    _uiState.value = RagUiState(isThinking = true, answer = builder.toString())
                }
                _uiState.value = RagUiState(isThinking = false, answer = builder.toString())
            } catch (t: Throwable) {
                _uiState.value = RagUiState(errorMessage = t.message ?: "Unknown error")
            }
        }
    }
}

class RagViewModelFactory(
    private val appContext: Context,
    private val modelConfig: NexaModelConfig = NexaModelCatalog.defaultLlm(),
    private val inferenceFactory: (NexaModelConfig) -> NexaInferenceEngine = { FakeNexaInferenceEngine() }
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RagViewModel::class.java)) {
            val loader = ContextPackLoader(appContext)
            val pack = loader.loadFromAssets()
            val vectorizer = QueryVectorizer(pack.metadata.embeddingDimensions)
            val retriever = ContextRetriever(pack)
            val promptBuilder = PromptBuilder()
            val inference = inferenceFactory(modelConfig)
            val controller = RagController(pack, vectorizer, retriever, promptBuilder, inference)
            return RagViewModel(controller) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
