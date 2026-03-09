package com.sionic_ai.chatbot_backend.infra.openai

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "openai")
data class OpenAiProperties(
    val apiKey: String,
    val baseUrl: String,
    val model: String
)