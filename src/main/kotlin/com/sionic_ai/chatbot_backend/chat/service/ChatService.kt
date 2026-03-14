package com.sionic_ai.chatbot_backend.chat.service

import com.sionic_ai.chatbot_backend.auth.security.CustomUserPrincipal
import com.sionic_ai.chatbot_backend.chat.domain.Chat
import com.sionic_ai.chatbot_backend.chat.domain.ChatThread
import com.sionic_ai.chatbot_backend.chat.dto.ChatStreamChunkResponse
import com.sionic_ai.chatbot_backend.chat.dto.CreateChatRequest
import com.sionic_ai.chatbot_backend.chat.dto.CreateChatResponse
import com.sionic_ai.chatbot_backend.chat.repository.ChatRepository
import com.sionic_ai.chatbot_backend.chat.repository.ChatThreadRepository
import com.sionic_ai.chatbot_backend.infra.openai.AiChatClient
import com.sionic_ai.chatbot_backend.infra.openai.AiChatMessage
import com.sionic_ai.chatbot_backend.user.domain.User
import com.sionic_ai.chatbot_backend.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.time.LocalDateTime
import java.util.concurrent.Executors

@Service
class ChatService(
    private val userRepository: UserRepository,
    private val chatThreadRepository: ChatThreadRepository,
    private val chatRepository: ChatRepository,
    private val aiChatClient: AiChatClient
) {
    private val sseExecutor = Executors.newCachedThreadPool()

    @Transactional
    fun createChat(
        principal: CustomUserPrincipal,
        request: CreateChatRequest
    ): CreateChatResponse {
        val user = userRepository.findById(principal.userId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }

        val now = LocalDateTime.now()
        val thread = findOrCreateThread(user.id!!, user, now)
        val threadId = thread.id ?: throw IllegalStateException("thread id가 없습니다.")

        val previousChats = chatRepository.findAllByThreadIdOrderByCreatedAtAsc(threadId)
        val messages = buildMessages(previousChats.map { it.question to it.answer }, request.question)

        val selectedModel = request.model ?: "gpt-4o-mini"
        val answer = aiChatClient.generateAnswer(selectedModel, messages)

        val chat = chatRepository.save(
            Chat(
                thread = thread,
                user = user,
                question = request.question,
                answer = answer,
                model = selectedModel,
                createdAt = now
            )
        )

        thread.lastChatAt = now

        return CreateChatResponse(
            threadId = threadId,
            chatId = chat.id ?: throw IllegalStateException("chat id가 없습니다."),
            question = chat.question,
            answer = chat.answer,
            model = chat.model
        )
    }

    fun createChatStream(
        principal: CustomUserPrincipal,
        request: CreateChatRequest
    ): SseEmitter {
        val emitter = SseEmitter(60_000L)

        sseExecutor.execute {
            try {
                val user = userRepository.findById(principal.userId)
                    .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }

                val now = LocalDateTime.now()
                val thread = findOrCreateThread(user.id!!, user, now)
                val threadId = thread.id ?: throw IllegalStateException("thread id가 없습니다.")

                val previousChats = chatRepository.findAllByThreadIdOrderByCreatedAtAsc(threadId)
                val messages = buildMessages(previousChats.map { it.question to it.answer }, request.question)
                val selectedModel = request.model ?: "gpt-4o-mini"

                val fullAnswer = aiChatClient.streamAnswer(selectedModel, messages) { delta ->
                    runCatching {
                        emitter.send(
                            SseEmitter.event()
                                .name("message")
                                .data(
                                    ChatStreamChunkResponse(
                                        type = "chunk",
                                        content = delta
                                    )
                                )
                        )
                    }.onFailure { throw it }
                }

                val chat = saveStreamedChat(
                    user = user,
                    thread = thread,
                    question = request.question,
                    answer = fullAnswer,
                    model = selectedModel,
                    now = now
                )

                emitter.send(
                    SseEmitter.event()
                        .name("done")
                        .data(
                            ChatStreamChunkResponse(
                                type = "done",
                                threadId = threadId,
                                chatId = chat.id ?: throw IllegalStateException("chat id가 없습니다."),
                                model = chat.model
                            )
                        )
                )

                emitter.complete()
            } catch (e: Exception) {
                runCatching {
                    emitter.send(
                        SseEmitter.event()
                            .name("error")
                            .data(
                                ChatStreamChunkResponse(
                                    type = "error",
                                    message = e.message ?: "알 수 없는 오류가 발생했습니다."
                                )
                            )
                    )
                }
                emitter.complete()
            }
        }

        return emitter
    }

    @Transactional
    fun saveStreamedChat(
        user: User,
        thread: ChatThread,
        question: String,
        answer: String,
        model: String,
        now: LocalDateTime
    ): Chat {
        val chat = chatRepository.save(
            Chat(
                thread = thread,
                user = user,
                question = question,
                answer = answer,
                model = model,
                createdAt = now
            )
        )
        thread.lastChatAt = now
        return chat
    }

    private fun buildMessages(
        previousChats: List<Pair<String, String>>,
        currentQuestion: String
    ): List<AiChatMessage> {
        val messages = mutableListOf<AiChatMessage>()
        messages.add(AiChatMessage("system", "You are a helpful assistant."))

        previousChats.forEach { (question, answer) ->
            messages.add(AiChatMessage("user", question))
            messages.add(AiChatMessage("assistant", answer))
        }

        messages.add(AiChatMessage("user", currentQuestion))
        return messages
    }

    private fun findOrCreateThread(
        userId: Long,
        user: User,
        now: LocalDateTime
    ): ChatThread {
        val latestThread = chatThreadRepository.findTopByUserIdOrderByLastChatAtDesc(userId)

        if (latestThread == null) {
            return chatThreadRepository.save(ChatThread(user = user, lastChatAt = now, createdAt = now))
        }

        return if (latestThread.lastChatAt.plusMinutes(30).isBefore(now)) {
            chatThreadRepository.save(ChatThread(user = user, lastChatAt = now, createdAt = now))
        } else {
            latestThread
        }
    }
}