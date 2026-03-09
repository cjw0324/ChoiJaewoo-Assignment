package com.sionic_ai.chatbot_backend.chat.repository

import com.sionic_ai.chatbot_backend.chat.domain.Chat
import org.springframework.data.jpa.repository.JpaRepository

interface ChatRepository : JpaRepository<Chat, Long> {
    fun findAllByThreadIdOrderByCreatedAtAsc(threadId: Long): List<Chat>
}