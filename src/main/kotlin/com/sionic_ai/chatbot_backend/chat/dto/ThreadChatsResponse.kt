package com.sionic_ai.chatbot_backend.chat.dto

import java.time.LocalDateTime

data class ThreadChatsResponse(
    val threadId: Long,
    val createdAt: LocalDateTime,
    val lastChatAt: LocalDateTime,
    val chats: List<ChatSummaryResponse>
)