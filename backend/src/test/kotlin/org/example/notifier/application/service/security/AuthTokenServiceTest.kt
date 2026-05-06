package org.example.notifier.application.service.security

import org.example.notifier.infrastructure.security.JwtTokenProvider
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.whenever

class AuthTokenServiceTest {

    private lateinit var jwtTokenProvider: JwtTokenProvider
    private lateinit var authTokenService: AuthTokenService

    @BeforeEach
    fun setUp() {
        jwtTokenProvider = mock(JwtTokenProvider::class.java)
        authTokenService = AuthTokenService(jwtTokenProvider, 86400000L)
    }

    @Test
    fun `generateToken delegates with correct subject claims and expiration`() {
        whenever(jwtTokenProvider.generateToken("user-1", mapOf("email" to "a@b.com", "role" to "ADMIN"), 86400000L))
            .thenReturn("generated-token")

        val result = authTokenService.generateToken("user-1", "a@b.com", "ADMIN")

        assertEquals("generated-token", result)
        verify(jwtTokenProvider).generateToken("user-1", mapOf("email" to "a@b.com", "role" to "ADMIN"), 86400000L)
    }

    @Test
    fun `getUserIdFromToken delegates to getSubject`() {
        whenever(jwtTokenProvider.getSubject("token-x")).thenReturn("user-42")

        assertEquals("user-42", authTokenService.getUserIdFromToken("token-x"))
    }

    @Test
    fun `validateToken delegates to isTokenValid`() {
        whenever(jwtTokenProvider.isTokenValid("valid")).thenReturn(true)
        whenever(jwtTokenProvider.isTokenValid("expired")).thenReturn(false)

        assertTrue(authTokenService.validateToken("valid"))
        assertFalse(authTokenService.validateToken("expired"))
    }

    @Test
    fun `extractTokenFromHeader delegates to extractFromHeader`() {
        whenever(jwtTokenProvider.extractFromHeader("Bearer abc")).thenReturn("abc")
        whenever(jwtTokenProvider.extractFromHeader(null)).thenReturn(null)

        assertEquals("abc", authTokenService.extractTokenFromHeader("Bearer abc"))
        assertNull(authTokenService.extractTokenFromHeader(null))
    }

    @Test
    fun `round-trip with real JwtTokenProvider`() {
        val realProvider = JwtTokenProvider("a-test-secret-that-is-at-least-32-bytes-long-for-hmac-sha")
        val service = AuthTokenService(realProvider, 60_000L)

        val token = service.generateToken("user-1", "test@example.com", "RECRUITER")
        assertTrue(service.validateToken(token))
        assertEquals("user-1", service.getUserIdFromToken(token))
    }
}
