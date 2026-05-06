package org.example.notifier.application.service.security

import org.example.notifier.infrastructure.security.JwtTokenProvider
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PrivacyTokenServiceTest {

    private lateinit var service: PrivacyTokenService

    @BeforeEach
    fun setUp() {
        val provider = JwtTokenProvider("a-test-secret-that-is-at-least-32-bytes-long-for-hmac-sha")
        service = PrivacyTokenService(provider)
    }

    @Test
    fun `deletion token round-trip returns email`() {
        val token = service.generateDeletionToken("user@example.com")
        assertEquals("user@example.com", service.validateDeletionToken(token))
    }

    @Test
    fun `download token round-trip returns email`() {
        val token = service.generateDownloadToken("user@example.com")
        assertEquals("user@example.com", service.validateDownloadToken(token))
    }

    @Test
    fun `deletion token does not validate as download token`() {
        val token = service.generateDeletionToken("user@example.com")
        assertNull(service.validateDownloadToken(token))
    }

    @Test
    fun `download token does not validate as deletion token`() {
        val token = service.generateDownloadToken("user@example.com")
        assertNull(service.validateDeletionToken(token))
    }

    @Test
    fun `invalid token returns null`() {
        assertNull(service.validateDeletionToken("garbage-token"))
        assertNull(service.validateDownloadToken("garbage-token"))
    }

    @Test
    fun `expired token returns null`() {
        val provider = JwtTokenProvider("a-test-secret-that-is-at-least-32-bytes-long-for-hmac-sha")
        val expiredToken = provider.generateToken("user@example.com", mapOf("purpose" to "data_deletion"), expirationMs = 1)
        Thread.sleep(50)
        assertNull(service.validateDeletionToken(expiredToken))
    }
}
