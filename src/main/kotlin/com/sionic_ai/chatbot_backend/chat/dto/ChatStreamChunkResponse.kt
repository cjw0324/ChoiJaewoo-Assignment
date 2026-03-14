package com.sionic_ai.chatbot_backend.chat.dto

data class ChatStreamChunkResponse(
    val type: String,
    val content: String? = null,
    val threadId: Long? = null,
    val chatId: Long? = null,
    val model: String? = null,
    val message: String? = null
)