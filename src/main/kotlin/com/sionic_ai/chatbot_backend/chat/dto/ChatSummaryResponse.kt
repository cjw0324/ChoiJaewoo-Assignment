package com.sionic_ai.chatbot_backend.chat.dto

import java.time.LocalDateTime

data class ChatSummaryResponse(
    val chatId: Long,
    val question: String,
    val answer: String,
    val model: String,
    val createdAt: LocalDateTime
)