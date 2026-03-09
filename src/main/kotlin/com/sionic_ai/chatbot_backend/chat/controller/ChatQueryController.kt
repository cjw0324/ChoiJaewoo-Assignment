package com.sionic_ai.chatbot_backend.chat.controller

import com.sionic_ai.chatbot_backend.auth.security.CustomUserPrincipal
import com.sionic_ai.chatbot_backend.chat.dto.ThreadChatsResponse
import com.sionic_ai.chatbot_backend.chat.service.ChatQueryService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class ChatQueryController(
    private val chatQueryService: ChatQueryService
) {

    @GetMapping("/threads")
    fun getThreads(
        @AuthenticationPrincipal principal: CustomUserPrincipal,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): List<ThreadChatsResponse> {
        return chatQueryService.getThreads(principal, page, size)
    }

    @DeleteMapping("/threads/{threadId}")
    fun deleteThread(
        @AuthenticationPrincipal principal: CustomUserPrincipal,
        @PathVariable threadId: Long
    ) {
        chatQueryService.deleteThread(principal, threadId)
    }
}