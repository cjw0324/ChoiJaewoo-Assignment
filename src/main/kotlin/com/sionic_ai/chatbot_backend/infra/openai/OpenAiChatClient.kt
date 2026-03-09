package com.sionic_ai.chatbot_backend.infra.openai

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Component
class OpenAiChatClient(
    private val openAiProperties: OpenAiProperties,
    private val objectMapper: ObjectMapper
) : AiChatClient {

    private val httpClient: HttpClient = HttpClient.newBuilder().build()

    override fun generateAnswer(
        model: String?,
        messages: List<AiChatMessage>
    ): String {
        val requestBody = mapOf(
            "model" to (model ?: openAiProperties.model),
            "messages" to messages.map {
                mapOf(
                    "role" to it.role,
                    "content" to it.content
                )
            },
            "stream" to false
        )

        val requestJson = objectMapper.writeValueAsString(requestBody)

        val request = HttpRequest.newBuilder()
            .uri(URI.create("${openAiProperties.baseUrl}/chat/completions"))
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${openAiProperties.apiKey}")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .POST(HttpRequest.BodyPublishers.ofString(requestJson))
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() !in 200..299) {
            throw IllegalStateException("OpenAI 호출 실패: status=${response.statusCode()}, body=${response.body()}")
        }

        val root = objectMapper.readTree(response.body())
        return root.path("choices")
            .firstOrNull()
            ?.path("message")
            ?.path("content")
            ?.asText()
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("OpenAI 응답 content가 없습니다.")
    }

    override fun streamAnswer(
        model: String?,
        messages: List<AiChatMessage>,
        onDelta: (String) -> Unit
    ): String {
        val requestBody = mapOf(
            "model" to (model ?: openAiProperties.model),
            "messages" to messages.map {
                mapOf(
                    "role" to it.role,
                    "content" to it.content
                )
            },
            "stream" to true
        )

        val requestJson = objectMapper.writeValueAsString(requestBody)

        val request = HttpRequest.newBuilder()
            .uri(URI.create("${openAiProperties.baseUrl}/chat/completions"))
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${openAiProperties.apiKey}")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .POST(HttpRequest.BodyPublishers.ofString(requestJson))
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofLines())

        if (response.statusCode() !in 200..299) {
            val errorBody = response.body().reduce("", { acc, line -> acc + line + "\n" })
            throw IllegalStateException("OpenAI 스트리밍 호출 실패: status=${response.statusCode()}, body=$errorBody")
        }

        val fullAnswer = StringBuilder()

        response.body().use { lines ->
            lines.forEach { line ->
                if (!line.startsWith("data:")) return@forEach

                val data = line.removePrefix("data:").trim()

                if (data == "[DONE]") {
                    return@forEach
                }

                if (data.isBlank()) {
                    return@forEach
                }

                val root: JsonNode = objectMapper.readTree(data)
                val choices = root.path("choices")
                if (!choices.isArray || choices.isEmpty) {
                    return@forEach
                }

                val deltaNode = choices[0].path("delta")
                val contentNode = deltaNode.get("content")

                if (contentNode != null && !contentNode.isNull) {
                    val delta = contentNode.asText()
                    if (delta.isNotEmpty()) {
                        fullAnswer.append(delta)
                        onDelta(delta)
                    }
                }
            }
        }

        return fullAnswer.toString()
    }
}