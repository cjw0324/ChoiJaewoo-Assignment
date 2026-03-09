package com.sionic_ai.chatbot_backend.user.repository

import com.sionic_ai.chatbot_backend.user.domain.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
    fun existsByEmail(email: String): Boolean
    fun findByEmail(email: String): User?
}