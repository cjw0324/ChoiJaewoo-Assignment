package com.sionic_ai.chatbot_backend.auth.jwt

import com.sionic_ai.chatbot_backend.auth.security.CustomUserPrincipal
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val bearerToken = request.getHeader("Authorization")

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            val token = bearerToken.removePrefix("Bearer ").trim()

            if (jwtTokenProvider.validateToken(token)) {
                val principal = CustomUserPrincipal(
                    userId = jwtTokenProvider.getUserId(token),
                    email = jwtTokenProvider.getEmail(token),
                    role = jwtTokenProvider.getRole(token)
                )

                val authentication = UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    listOf(SimpleGrantedAuthority("ROLE_${principal.role.name}"))
                )

                SecurityContextHolder.getContext().authentication = authentication
            }
        }

        filterChain.doFilter(request, response)
    }

    override fun shouldNotFilterAsyncDispatch(): Boolean {
        return false
    }
}