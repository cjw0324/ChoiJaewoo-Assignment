package com.sionic_ai.chatbot_backend.chat.domain

import com.sionic_ai.chatbot_backend.user.domain.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "chats")
class Chat(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thread_id", nullable = false)
    val thread: ChatThread,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Lob
    @Column(nullable = false)
    val question: String,

    @Lob
    @Column(nullable = false)
    val answer: String,

    @Column(nullable = false)
    val model: String,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
)