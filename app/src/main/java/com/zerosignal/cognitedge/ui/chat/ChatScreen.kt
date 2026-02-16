package com.zerosignal.cognitedge.ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zerosignal.cognitedge.RagUiState
import com.zerosignal.cognitedge.RagViewModel
import com.zerosignal.cognitedge.domain.model.ChatMessage
import com.zerosignal.cognitedge.ui.chat.components.ChatInputBar
import com.zerosignal.cognitedge.ui.chat.components.MessageBubble
import com.zerosignal.cognitedge.ui.theme.BorderMuted
import com.zerosignal.cognitedge.ui.theme.TextQuaternary
import com.zerosignal.cognitedge.ui.theme.TextTertiary
import com.zerosignal.cognitedge.ui.theme.ZeroSignalTheme

@Composable
fun ChatScreen(viewModel: RagViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var inputText by rememberSaveable { mutableStateOf("") }

    ChatScreenContent(
        uiState = uiState,
        inputText = inputText,
        onInputChange = { inputText = it },
        onSend = {
            if (inputText.isNotBlank()) {
                viewModel.ask(inputText)
                inputText = ""
            }
        }
    )
}

@Composable
private fun ChatScreenContent(
    uiState: RagUiState,
    inputText: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit
) {
    val listState = rememberLazyListState()
    val messages = uiState.messages

    val isAtBottom by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem == null || lastVisibleItem.index >= listState.layoutInfo.totalItemsCount - 1
        }
    }

    LaunchedEffect(messages.size, messages.lastOrNull()?.content?.length) {
        if (messages.isNotEmpty() && isAtBottom) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (messages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxSize()
                            .padding(horizontal = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "ZeroSignal",
                                style = MaterialTheme.typography.displaySmall,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Offline Intelligence",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextTertiary
                            )
                            Spacer(modifier = Modifier.height(40.dp))
                            Text(
                                text = "Ask anything about your downloaded packs",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextQuaternary
                            )
                        }
                    }
                }
            }

            items(messages, key = { "${it.id}-${it.isStreaming}" }) { message ->
                MessageBubble(message = message)
            }

            if (uiState.errorMessage != null) {
                item {
                    Text(
                        text = uiState.errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }

        HorizontalDivider(color = BorderMuted, thickness = 0.5.dp)

        ChatInputBar(
            inputText = inputText,
            onInputChange = onInputChange,
            onSend = onSend,
            enabled = !uiState.isThinking
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF09090B)
@Composable
private fun ChatScreenEmptyPreview() {
    ZeroSignalTheme {
        ChatScreenContent(
            uiState = RagUiState(),
            inputText = "",
            onInputChange = {},
            onSend = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF09090B)
@Composable
private fun ChatScreenWithMessagesPreview() {
    ZeroSignalTheme {
        ChatScreenContent(
            uiState = RagUiState(
                messages = listOf(
                    ChatMessage(
                        id = "1",
                        role = ChatMessage.Role.USER,
                        content = "What is quantum computing?"
                    ),
                    ChatMessage(
                        id = "2",
                        role = ChatMessage.Role.ASSISTANT,
                        content = "Based on the context:\n- **Quantum computing** uses quantum bits (qubits)\n- It leverages superposition and entanglement\n- Potential for solving complex problems faster",
                        sourcesUsed = listOf("chunk-1", "chunk-3")
                    )
                )
            ),
            inputText = "",
            onInputChange = {},
            onSend = {}
        )
    }
}
