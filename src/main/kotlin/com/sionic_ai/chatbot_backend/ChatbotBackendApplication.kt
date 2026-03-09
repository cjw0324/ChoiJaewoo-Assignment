package com.sionic_ai.chatbot_backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@ConfigurationPropertiesScan
@SpringBootApplication
class ChatbotBackendApplication

fun main(args: Array<String>) {
	runApplication<ChatbotBackendApplication>(*args)
}
