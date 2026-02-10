package com.zerosignal.cognitedge.ui.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zerosignal.cognitedge.ui.theme.SurfaceVariantDark
import com.zerosignal.cognitedge.ui.theme.ZeroSignalTheme

@Composable
fun ChatInputBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceVariantDark)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = inputText,
            onValueChange = onInputChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Type your question...") },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(24.dp),
            singleLine = false,
            maxLines = 4,
            enabled = enabled
        )
        IconButton(onClick = onSend, enabled = enabled && inputText.isNotBlank()) {
            Text(
                text = "\u27A4",
                style = MaterialTheme.typography.headlineMedium,
                color = if (enabled && inputText.isNotBlank()) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                }
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1E1E1E)
@Composable
private fun ChatInputBarPreview() {
    ZeroSignalTheme {
        ChatInputBar(
            inputText = "Hello",
            onInputChange = {},
            onSend = {}
        )
    }
}
