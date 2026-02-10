package com.zerosignal.cognitedge.domain.model

data class ChatMessage(
    val id: String,
    val role: Role,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val sourcesUsed: List<String> = emptyList(),
    val isStreaming: Boolean = false
) {
    enum class Role { USER, ASSISTANT }
}
