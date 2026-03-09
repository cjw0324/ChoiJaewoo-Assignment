package com.sionic_ai.chatbot_backend.chat.service

import com.sionic_ai.chatbot_backend.auth.security.CustomUserPrincipal
import com.sionic_ai.chatbot_backend.chat.dto.ChatSummaryResponse
import com.sionic_ai.chatbot_backend.chat.dto.ThreadChatsResponse
import com.sionic_ai.chatbot_backend.chat.repository.ChatRepository
import com.sionic_ai.chatbot_backend.chat.repository.ChatThreadRepository
import com.sionic_ai.chatbot_backend.user.domain.UserRole
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatQueryService(
    private val chatThreadRepository: ChatThreadRepository,
    private val chatRepository: ChatRepository
) {

    @Transactional(readOnly = true)
    fun getThreads(
        principal: CustomUserPrincipal,
        page: Int,
        size: Int
    ): List<ThreadChatsResponse> {
        val pageable = PageRequest.of(
            page,
            size,
            Sort.by(Sort.Direction.DESC, "lastChatAt")
        )

        val threadPage = if (principal.role == UserRole.ADMIN) {
            chatThreadRepository.findAll(pageable)
        } else {
            chatThreadRepository.findAllByUserId(principal.userId, pageable)
        }

        return threadPage.content.map { thread ->
            val threadId = thread.id ?: throw IllegalStateException("thread id가 없습니다.")

            val chats = chatRepository.findAllByThreadIdOrderByCreatedAtAsc(threadId)

            ThreadChatsResponse(
                threadId = threadId,
                createdAt = thread.createdAt,
                lastChatAt = thread.lastChatAt,
                chats = chats.map {
                    ChatSummaryResponse(
                        chatId = it.id ?: throw IllegalStateException("chat id가 없습니다."),
                        question = it.question,
                        answer = it.answer,
                        model = it.model,
                        createdAt = it.createdAt
                    )
                }
            )
        }
    }

    @Transactional
    fun deleteThread(
        principal: CustomUserPrincipal,
        threadId: Long
    ) {
        val thread = chatThreadRepository.findById(threadId)
            .orElseThrow { IllegalArgumentException("스레드를 찾을 수 없습니다.") }

        if (principal.role != UserRole.ADMIN && thread.user.id != principal.userId) {
            throw IllegalArgumentException("삭제 권한이 없습니다.")
        }

        chatThreadRepository.delete(thread)
    }
}