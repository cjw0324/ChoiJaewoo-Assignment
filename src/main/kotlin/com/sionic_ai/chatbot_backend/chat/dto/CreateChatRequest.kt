package com.sionic_ai.chatbot_backend.chat.dto

import jakarta.validation.constraints.NotBlank

data class CreateChatRequest(
    @field:NotBlank
    val question: String,
    val model: String? = null,
    val isStreaming: Boolean = false
)