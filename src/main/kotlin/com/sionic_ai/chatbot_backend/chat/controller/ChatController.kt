package com.sionic_ai.chatbot_backend.chat.controller

import com.sionic_ai.chatbot_backend.auth.security.CustomUserPrincipal
import com.sionic_ai.chatbot_backend.chat.dto.CreateChatRequest
import com.sionic_ai.chatbot_backend.chat.dto.CreateChatResponse
import com.sionic_ai.chatbot_backend.chat.service.ChatService
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping("/api/chats")
class ChatController(
    private val chatService: ChatService
) {

    @PostMapping
    fun createChat(
        @AuthenticationPrincipal principal: CustomUserPrincipal,
        @Valid @RequestBody request: CreateChatRequest
    ): CreateChatResponse {
        return chatService.createChat(principal, request)
    }

    @PostMapping("/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun createChatStream(
        @AuthenticationPrincipal principal: CustomUserPrincipal,
        @Valid @RequestBody request: CreateChatRequest
    ): SseEmitter {
        return chatService.createChatStream(principal, request)
    }
}