package com.zerosignal.cognitedge.ui.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zerosignal.cognitedge.ui.theme.Accent
import com.zerosignal.cognitedge.ui.theme.Obsidian
import com.zerosignal.cognitedge.ui.theme.Surface2
import com.zerosignal.cognitedge.ui.theme.Surface3
import com.zerosignal.cognitedge.ui.theme.TextTertiary
import com.zerosignal.cognitedge.ui.theme.ZeroSignalTheme

@Composable
fun ChatInputBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val isActive = enabled && inputText.isNotBlank()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Obsidian)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        TextField(
            value = inputText,
            onValueChange = onInputChange,
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp)),
            placeholder = {
                Text(
                    "Message...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextTertiary
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Surface2,
                unfocusedContainerColor = Surface2,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Accent
            ),
            shape = RoundedCornerShape(24.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            singleLine = false,
            maxLines = 4,
            enabled = enabled
        )
        IconButton(
            onClick = onSend,
            enabled = isActive,
            modifier = Modifier
                .padding(start = 10.dp)
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    if (isActive) MaterialTheme.colorScheme.onSurface
                    else Surface3,
                    CircleShape
                ),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.Transparent
            )
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowUpward,
                contentDescription = "Send",
                tint = if (isActive) Obsidian else TextTertiary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF09090B)
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

@Preview(showBackground = true, backgroundColor = 0xFF09090B)
@Composable
private fun ChatInputBarEmptyPreview() {
    ZeroSignalTheme {
        ChatInputBar(
            inputText = "",
            onInputChange = {},
            onSend = {}
        )
    }
}
