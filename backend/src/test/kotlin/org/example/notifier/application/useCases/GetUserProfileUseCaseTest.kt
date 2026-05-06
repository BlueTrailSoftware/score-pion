package org.example.notifier.application.useCases

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.service.core.UserService
import org.example.notifier.application.useCases.getUserProfile.GetUserProfileUseCase
import org.example.notifier.domain.user.User
import org.example.notifier.domain.user.UserRole
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import java.time.LocalDateTime

class GetUserProfileUseCaseTest {

    private lateinit var userService: UserService
    private lateinit var useCase: GetUserProfileUseCase

    private val now = LocalDateTime.now()

    private val user = User(
        id = "user-1",
        email = "user@example.com",
        name = "Test User",
        pictureUrl = "https://pic.example.com/avatar.jpg",
        role = UserRole.RECRUITER,
        isActive = true,
        createdAt = now,
        updatedAt = now
    )

    @BeforeEach
    fun setup() {
        userService = mock(UserService::class.java)
        useCase = GetUserProfileUseCase(userService)
    }

    @Test
    fun `execute returns UserProfileResult when user exists`() = runBlocking<Unit> {
        whenever(userService.findById("user-1")).thenReturn(user)

        val result = useCase.execute("user-1")

        assertEquals("user-1", result?.id)
        assertEquals("user@example.com", result?.email)
        assertEquals("Test User", result?.name)
        assertEquals("https://pic.example.com/avatar.jpg", result?.pictureUrl)
        assertEquals(UserRole.RECRUITER, result?.role)
        assertEquals(true, result?.isActive)
        assertEquals(now, result?.createdAt)
    }

    @Test
    fun `execute returns null when user does not exist`() = runBlocking<Unit> {
        whenever(userService.findById("missing")).thenReturn(null)

        val result = useCase.execute("missing")

        assertNull(result)
    }

    @Test
    fun `execute maps null pictureUrl correctly`() = runBlocking<Unit> {
        whenever(userService.findById("user-1")).thenReturn(user.copy(pictureUrl = null))

        val result = useCase.execute("user-1")

        assertNull(result?.pictureUrl)
    }
}
