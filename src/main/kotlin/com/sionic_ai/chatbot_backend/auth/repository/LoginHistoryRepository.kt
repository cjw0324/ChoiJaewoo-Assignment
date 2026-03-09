package com.sionic_ai.chatbot_backend.auth.repository

import com.sionic_ai.chatbot_backend.auth.domain.LoginHistory
import org.springframework.data.jpa.repository.JpaRepository

interface LoginHistoryRepository : JpaRepository<LoginHistory, Long>