package org.example.notifier.application.useCases

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.service.core.UserService
import org.example.notifier.application.service.security.AuthTokenService
import org.example.notifier.application.useCases.validateToken.ValidateTokenUseCase
import org.example.notifier.domain.user.User
import org.example.notifier.domain.user.UserRole
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import java.time.LocalDateTime

class ValidateTokenUseCaseTest {

    private lateinit var authTokenService: AuthTokenService
    private lateinit var userService: UserService
    private lateinit var useCase: ValidateTokenUseCase

    private val now = LocalDateTime.now()

    private val activeUser = User(
        id = "user-1",
        email = "user@example.com",
        name = "Test User",
        role = UserRole.RECRUITER,
        isActive = true,
        createdAt = now,
        updatedAt = now
    )

    private val inactiveUser = activeUser.copy(isActive = false)

    @BeforeEach
    fun setup() {
        authTokenService = mock(AuthTokenService::class.java)
        userService = mock(UserService::class.java)
        useCase = ValidateTokenUseCase(authTokenService, userService)
    }

    @Test
    fun `execute returns invalid when header has no token`() = runBlocking<Unit> {
        whenever(authTokenService.extractTokenFromHeader("Bearer ")).thenReturn(null)

        val result = useCase.execute("Bearer ")

        assertFalse(result.valid)
        assertEquals("No token provided", result.message)
    }

    @Test
    fun `execute returns invalid when token is invalid or expired`() = runBlocking<Unit> {
        whenever(authTokenService.extractTokenFromHeader("Bearer bad")).thenReturn("bad")
        whenever(authTokenService.validateAndExtractUserId("bad")).thenReturn(null)

        val result = useCase.execute("Bearer bad")

        assertFalse(result.valid)
        assertEquals("Invalid token", result.message)
    }

    @Test
    fun `execute returns invalid when getUserIdFromToken returns null`() = runBlocking<Unit> {
        whenever(authTokenService.extractTokenFromHeader("Bearer tok")).thenReturn("tok")
        whenever(authTokenService.validateToken("tok")).thenReturn(true)
        whenever(authTokenService.getUserIdFromToken("tok")).thenReturn(null)

        val result = useCase.execute("Bearer tok")

        assertFalse(result.valid)
        assertEquals("Invalid token", result.message)
    }

    @Test
    fun `execute returns valid when token is valid and user is active`() = runBlocking<Unit> {
        whenever(authTokenService.extractTokenFromHeader("Bearer good")).thenReturn("good")
        whenever(authTokenService.validateAndExtractUserId("good")).thenReturn("user-1")
        whenever(userService.findById("user-1")).thenReturn(activeUser)

        val result = useCase.execute("Bearer good")

        assertTrue(result.valid)
        assertEquals("Token is valid", result.message)
    }

    @Test
    fun `execute returns invalid when user is not found`() = runBlocking<Unit> {
        whenever(authTokenService.extractTokenFromHeader("Bearer good")).thenReturn("good")
        whenever(authTokenService.validateAndExtractUserId("good")).thenReturn("user-1")
        whenever(userService.findById("user-1")).thenReturn(null)

        val result = useCase.execute("Bearer good")

        assertFalse(result.valid)
        assertEquals("User not found or inactive", result.message)
    }

    @Test
    fun `execute returns invalid when user is inactive`() = runBlocking<Unit> {
        whenever(authTokenService.extractTokenFromHeader("Bearer good")).thenReturn("good")
        whenever(authTokenService.validateAndExtractUserId("good")).thenReturn("user-1")
        whenever(userService.findById("user-1")).thenReturn(inactiveUser)

        val result = useCase.execute("Bearer good")

        assertFalse(result.valid)
        assertEquals("User not found or inactive", result.message)
    }
}
