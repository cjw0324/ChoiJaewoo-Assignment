package com.sionic_ai.chatbot_backend.infra.openai

data class OpenAiChatCompletionRequest(
    val model: String,
    val messages: List<OpenAiMessage>,
    val stream: Boolean = false
)

data class OpenAiMessage(
    val role: String,
    val content: String
)