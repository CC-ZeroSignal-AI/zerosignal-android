package com.zerosignal.cognitedge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zerosignal.cognitedge.domain.model.ChatMessage
import com.zerosignal.cognitedge.domain.usecase.AskQuestionUseCase
import com.zerosignal.cognitedge.logging.ZSLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class RagUiState(
    val isThinking: Boolean = false,
    val answer: String = "",
    val errorMessage: String? = null,
    val messages: List<ChatMessage> = emptyList()
)

@HiltViewModel
class RagViewModel @Inject constructor(
    private val askQuestionUseCase: AskQuestionUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "RagViewModel"
    }

    private val _uiState = MutableStateFlow(RagUiState())
    val uiState: StateFlow<RagUiState> = _uiState.asStateFlow()

    private var inFlightJob: Job? = null

    fun ask(question: String) {
        inFlightJob?.cancel()
        inFlightJob = viewModelScope.launch {
            try {
                if (question.isBlank()) {
                    ZSLog.w(TAG, "Empty question submitted")
                    _uiState.update { it.copy(errorMessage = "Question cannot be blank") }
                    return@launch
                }

                ZSLog.i(TAG, "User asked: ${question.take(80)}...")

                // Add user message
                val userMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    role = ChatMessage.Role.USER,
                    content = question
                )
                _uiState.update { state ->
                    state.copy(
                        messages = state.messages + userMsg,
                        isThinking = true,
                        errorMessage = null
                    )
                }

                // Single call — retrieves context + streams response
                val askResult = askQuestionUseCase.invoke(question)
                val sourceIds = askResult.sourcesUsed
                ZSLog.d(TAG, "Sources used: $sourceIds")

                val assistantId = UUID.randomUUID().toString()
                val builder = StringBuilder()

                askResult.responseFlow.collect { chunk ->
                    builder.append(chunk.token)
                    val streamingMsg = ChatMessage(
                        id = assistantId,
                        role = ChatMessage.Role.ASSISTANT,
                        content = builder.toString(),
                        sourcesUsed = sourceIds,
                        isStreaming = true
                    )
                    _uiState.update { state ->
                        val msgs = state.messages.toMutableList()
                        val existingIdx = msgs.indexOfFirst { it.id == assistantId }
                        if (existingIdx >= 0) {
                            msgs[existingIdx] = streamingMsg
                        } else {
                            msgs.add(streamingMsg)
                        }
                        state.copy(
                            messages = msgs,
                            isThinking = true,
                            answer = builder.toString()
                        )
                    }
                }

                // Finalize message
                val finalMsg = ChatMessage(
                    id = assistantId,
                    role = ChatMessage.Role.ASSISTANT,
                    content = builder.toString(),
                    sourcesUsed = sourceIds,
                    isStreaming = false
                )
                _uiState.update { state ->
                    val msgs = state.messages.toMutableList()
                    val existingIdx = msgs.indexOfFirst { it.id == assistantId }
                    if (existingIdx >= 0) {
                        msgs[existingIdx] = finalMsg
                    }
                    state.copy(
                        messages = msgs,
                        isThinking = false,
                        answer = builder.toString()
                    )
                }
                ZSLog.i(TAG, "Response complete — ${builder.length} chars")
            } catch (t: Throwable) {
                ZSLog.e(TAG, "Error during ask()", t)
                _uiState.update { it.copy(isThinking = false, errorMessage = t.message ?: "Unknown error") }
            }
        }
    }
}
