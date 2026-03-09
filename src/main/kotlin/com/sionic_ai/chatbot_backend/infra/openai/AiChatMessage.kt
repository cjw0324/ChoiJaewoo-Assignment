package com.sionic_ai.chatbot_backend.infra.openai

data class AiChatMessage(
    val role: String,
    val content: String
)