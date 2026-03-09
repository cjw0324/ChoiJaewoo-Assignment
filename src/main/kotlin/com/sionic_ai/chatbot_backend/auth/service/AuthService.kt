package com.sionic_ai.chatbot_backend.auth.service

import com.sionic_ai.chatbot_backend.auth.domain.LoginHistory
import com.sionic_ai.chatbot_backend.auth.dto.AuthResponse
import com.sionic_ai.chatbot_backend.auth.dto.LoginRequest
import com.sionic_ai.chatbot_backend.auth.dto.SignupRequest
import com.sionic_ai.chatbot_backend.auth.jwt.JwtTokenProvider
import com.sionic_ai.chatbot_backend.auth.repository.LoginHistoryRepository
import com.sionic_ai.chatbot_backend.user.domain.User
import com.sionic_ai.chatbot_backend.user.domain.UserRole
import com.sionic_ai.chatbot_backend.user.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val loginHistoryRepository: LoginHistoryRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider
) {

    @Transactional
    fun signup(request: SignupRequest) {
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("이미 가입된 이메일입니다.")
        }

        val user = User(
            email = request.email,
            password = passwordEncoder.encode(request.password),
            name = request.name,
            role = UserRole.MEMBER
        )

        userRepository.save(user)
    }

    @Transactional
    fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByEmail(request.email)
            ?: throw IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.")

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.")
        }

        loginHistoryRepository.save(LoginHistory(user))

        val accessToken = jwtTokenProvider.generateAccessToken(
            userId = user.id!!,
            email = user.email,
            role = user.role
        )

        return AuthResponse(accessToken)
    }
}