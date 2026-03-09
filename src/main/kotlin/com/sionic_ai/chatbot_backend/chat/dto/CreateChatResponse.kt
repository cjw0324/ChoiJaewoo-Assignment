package com.sionic_ai.chatbot_backend.chat.dto

data class CreateChatResponse(
    val threadId: Long,
    val chatId: Long,
    val question: String,
    val answer: String,
    val model: String
)