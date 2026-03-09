package com.sionic_ai.chatbot_backend.auth.security

import com.sionic_ai.chatbot_backend.user.domain.UserRole

data class CustomUserPrincipal(
    val userId: Long,
    val email: String,
    val role: UserRole
)