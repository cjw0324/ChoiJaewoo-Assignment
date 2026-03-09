package com.sionic_ai.chatbot_backend.infra.openai

interface AiChatClient {
    fun generateAnswer(
        model: String?,
        messages: List<AiChatMessage>
    ): String

    fun streamAnswer(
        model: String?,
        messages: List<AiChatMessage>,
        onDelta: (String) -> Unit
    ): String
}