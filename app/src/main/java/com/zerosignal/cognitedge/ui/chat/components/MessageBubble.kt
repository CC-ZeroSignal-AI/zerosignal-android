package com.zerosignal.cognitedge.ui.chat.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zerosignal.cognitedge.domain.model.ChatMessage
import com.zerosignal.cognitedge.ui.theme.Accent
import com.zerosignal.cognitedge.ui.theme.Surface2
import com.zerosignal.cognitedge.ui.theme.TextSecondary
import com.zerosignal.cognitedge.ui.theme.TextTertiary
import com.zerosignal.cognitedge.ui.theme.ZeroSignalTheme

@Composable
fun MessageBubble(message: ChatMessage) {
    val isUser = message.role == ChatMessage.Role.USER
    val alignment = if (isUser) Arrangement.End else Arrangement.Start

    val shape = RoundedCornerShape(
        topStart = 20.dp,
        topEnd = 20.dp,
        bottomStart = if (isUser) 20.dp else 4.dp,
        bottomEnd = if (isUser) 4.dp else 20.dp
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp),
        horizontalArrangement = alignment
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(shape)
                .background(
                    if (isUser) Accent.copy(alpha = 0.12f)
                    else Surface2
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            if (!isUser) {
                Text(
                    text = "ZeroSignal",
                    style = MaterialTheme.typography.labelSmall,
                    color = Accent.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            MarkdownText(
                text = message.content,
                color = if (isUser) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    TextSecondary
                }
            )

            if (message.sourcesUsed.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${message.sourcesUsed.size} sources",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
            }

            if (message.isStreaming) {
                val transition = rememberInfiniteTransition(label = "cursor")
                val alpha by transition.animateFloat(
                    initialValue = 1f,
                    targetValue = 0.15f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "cursorBlink"
                )
                Text(
                    text = "\u2588",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Accent.copy(alpha = alpha)
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
            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
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

@Preview(showBackground = true, backgroundColor = 0xFF09090B)
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

@Preview(showBackground = true, backgroundColor = 0xFF09090B)
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
