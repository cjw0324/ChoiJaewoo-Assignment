package com.sionic_ai.chatbot_backend.infra.openai

data class OpenAiChatCompletionResponse(
    val choices: List<OpenAiChoice>
)

data class OpenAiChoice(
    val message: OpenAiAssistantMessage
)

data class OpenAiAssistantMessage(
    val role: String,
    val content: String
)