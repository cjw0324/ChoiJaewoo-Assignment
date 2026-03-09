package com.sionic_ai.chatbot_backend.auth.controller

import com.sionic_ai.chatbot_backend.auth.dto.AuthResponse
import com.sionic_ai.chatbot_backend.auth.dto.LoginRequest
import com.sionic_ai.chatbot_backend.auth.dto.SignupRequest
import com.sionic_ai.chatbot_backend.auth.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    fun signup(
        @Valid @RequestBody request: SignupRequest
    ) {
        authService.signup(request)
    }

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest
    ): AuthResponse {
        return authService.login(request)
    }
}