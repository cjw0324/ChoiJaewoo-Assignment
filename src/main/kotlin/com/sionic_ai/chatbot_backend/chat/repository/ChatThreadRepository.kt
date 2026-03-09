package com.sionic_ai.chatbot_backend.chat.repository

import com.sionic_ai.chatbot_backend.chat.domain.ChatThread
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface ChatThreadRepository : JpaRepository<ChatThread, Long> {
    fun findTopByUserIdOrderByLastChatAtDesc(userId: Long): ChatThread?
    fun findAllByUserId(userId: Long, pageable: Pageable): Page<ChatThread>
}