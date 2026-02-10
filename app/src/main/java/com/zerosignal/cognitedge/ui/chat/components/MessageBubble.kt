package com.zerosignal.cognitedge.ui.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zerosignal.cognitedge.domain.model.ChatMessage
import com.zerosignal.cognitedge.ui.theme.AssistantBubble
import com.zerosignal.cognitedge.ui.theme.UserBubble
import com.zerosignal.cognitedge.ui.theme.ZeroSignalTheme

@Composable
fun MessageBubble(message: ChatMessage) {
    val isUser = message.role == ChatMessage.Role.USER
    val bubbleColor = if (isUser) UserBubble else AssistantBubble
    val alignment = if (isUser) Arrangement.End else Arrangement.Start

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = alignment
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ))
                .background(bubbleColor)
                .padding(12.dp)
        ) {
            if (!isUser) {
                Text(
                    text = "ZeroSignal",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            MarkdownText(
                text = message.content,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (message.sourcesUsed.isNotEmpty()) {
                Text(
                    text = "Sources: ${message.sourcesUsed.joinToString(", ")}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (message.isStreaming) {
                Text(
                    text = "\u2588",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun MarkdownText(
    text: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    val annotated = buildAnnotatedString {
        var remaining = text
        while (remaining.isNotEmpty()) {
            val boldStart = remaining.indexOf("**")
            if (boldStart == -1) {
                append(remaining)
                break
            }
            append(remaining.substring(0, boldStart))
            remaining = remaining.substring(boldStart + 2)
            val boldEnd = remaining.indexOf("**")
            if (boldEnd == -1) {
                append("**")
                append(remaining)
                break
            }
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(remaining.substring(0, boldEnd))
            }
            remaining = remaining.substring(boldEnd + 2)
        }
    }

    Text(
        text = annotated,
        style = MaterialTheme.typography.bodyLarge,
        color = color,
        modifier = modifier
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun UserBubblePreview() {
    ZeroSignalTheme {
        MessageBubble(
            ChatMessage(
                id = "1",
                role = ChatMessage.Role.USER,
                content = "What is the weather today?"
            )
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun AssistantBubblePreview() {
    ZeroSignalTheme {
        MessageBubble(
            ChatMessage(
                id = "2",
                role = ChatMessage.Role.ASSISTANT,
                content = "Based on the context:\n- **Temperature**: 72F\n- **Conditions**: Sunny",
                sourcesUsed = listOf("chunk-1", "chunk-3")
            )
        )
    }
}
