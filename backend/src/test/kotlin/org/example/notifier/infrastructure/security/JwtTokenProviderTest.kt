package org.example.notifier.infrastructure.security

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class JwtTokenProviderTest {

    private lateinit var provider: JwtTokenProvider

    @BeforeEach
    fun setUp() {
        provider = JwtTokenProvider("a-test-secret-that-is-at-least-32-bytes-long-for-hmac-sha")
    }

    @Test
    fun `generateToken produces valid JWT with three dot-separated parts`() {
        val token = provider.generateToken("user-1", expirationMs = 60_000)
        assertEquals(3, token.split(".").size)
    }

    @Test
    fun `generateToken subject is recoverable via getSubject`() {
        val token = provider.generateToken("user-42", expirationMs = 60_000)
        assertEquals("user-42", provider.getSubject(token))
    }

    @Test
    fun `generateToken custom claims are present in parsed token`() {
        val claims = mapOf("email" to "test@example.com", "role" to "ADMIN")
        val token = provider.generateToken("user-1", claims, expirationMs = 60_000)

        val parsed = provider.parseToken(token)
        assertNotNull(parsed)
        assertEquals("test@example.com", parsed!!["email"])
        assertEquals("ADMIN", parsed["role"])
    }

    @Test
    fun `generateToken with expired token is not valid`() {
        val token = provider.generateToken("user-1", expirationMs = 1)
        Thread.sleep(50)
        assertFalse(provider.isTokenValid(token))
    }

    @Test
    fun `generateTokenWithHoursExpiration produces valid token`() {
        val token = provider.generateTokenWithHoursExpiration("user-1", expirationHours = 1)
        assertTrue(provider.isTokenValid(token))
        assertEquals("user-1", provider.getSubject(token))
    }

    @Test
    fun `generateTokenWithHoursExpiration includes custom claims`() {
        val claims = mapOf("purpose" to "deletion")
        val token = provider.generateTokenWithHoursExpiration("user-1", claims, expirationHours = 24)

        val parsed = provider.parseToken(token)
        assertNotNull(parsed)
        assertEquals("deletion", parsed!!["purpose"])
    }

    @Test
    fun `parseToken returns Claims for valid token`() {
        val token = provider.generateToken("sub-1", expirationMs = 60_000)
        val claims = provider.parseToken(token)
        assertNotNull(claims)
        assertEquals("sub-1", claims!!.subject)
    }

    @Test
    fun `parseToken returns null for tampered token`() {
        val token = provider.generateToken("user-1", expirationMs = 60_000)
        val tampered = token.dropLast(3) + "xyz"
        assertNull(provider.parseToken(tampered))
    }

    @Test
    fun `parseToken returns null for expired token`() {
        val token = provider.generateToken("user-1", expirationMs = 1)
        Thread.sleep(50)
        assertNull(provider.parseToken(token))
    }

    @Test
    fun `parseToken returns null for garbage string`() {
        assertNull(provider.parseToken("not-a-jwt"))
        assertNull(provider.parseToken(""))
    }

    @Test
    fun `isTokenValid returns true for fresh token`() {
        val token = provider.generateToken("user-1", expirationMs = 60_000)
        assertTrue(provider.isTokenValid(token))
    }

    @Test
    fun `extractFromHeader extracts token from Bearer header`() {
        assertEquals("my-token", provider.extractFromHeader("Bearer my-token"))
    }

    @Test
    fun `extractFromHeader returns null for null blank or missing Bearer prefix`() {
        assertNull(provider.extractFromHeader(null))
        assertNull(provider.extractFromHeader(""))
        assertNull(provider.extractFromHeader("   "))
        assertNull(provider.extractFromHeader("Basic abc123"))
        assertNull(provider.extractFromHeader("Token my-token"))
    }
}
