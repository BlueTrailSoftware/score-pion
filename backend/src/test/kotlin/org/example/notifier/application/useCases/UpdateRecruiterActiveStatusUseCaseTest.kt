package org.example.notifier.application.useCases

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.service.core.UserService
import org.example.notifier.application.useCases.updateRecruiterActiveStatus.UpdateRecruiterActiveStatusCommand
import org.example.notifier.application.useCases.updateRecruiterActiveStatus.UpdateRecruiterActiveStatusUseCase
import org.example.notifier.domain.user.User
import org.example.notifier.domain.user.UserRole
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.time.LocalDateTime

class UpdateRecruiterActiveStatusUseCaseTest {

    private lateinit var userService: UserService
    private lateinit var useCase: UpdateRecruiterActiveStatusUseCase

    private val now = LocalDateTime.now()

    private val recruiterUser = User(
        id = "rec-1",
        email = "rec@test.com",
        name = "Recruiter",
        role = UserRole.RECRUITER,
        isActive = true,
        createdAt = now,
        updatedAt = now
    )

    @BeforeEach
    fun setup() {
        userService = mock(UserService::class.java)
        useCase = UpdateRecruiterActiveStatusUseCase(userService)
    }

    @Test
    fun `execute should activate recruiter`() = runBlocking<Unit> {
        whenever(userService.updateActiveStatus("rec-1", true)).thenReturn(recruiterUser.copy(isActive = true))

        val result = useCase.execute(UpdateRecruiterActiveStatusCommand("rec-1", true))

        assertEquals("rec-1", result.id)
        assertEquals(true, result.isActive)
    }

    @Test
    fun `execute should deactivate recruiter`() = runBlocking<Unit> {
        whenever(userService.updateActiveStatus("rec-1", false)).thenReturn(recruiterUser.copy(isActive = false))

        val result = useCase.execute(UpdateRecruiterActiveStatusCommand("rec-1", false))

        assertEquals(false, result.isActive)
    }

    @Test
    fun `execute should throw IllegalArgumentException when user not found`() = runBlocking<Unit> {
        whenever(userService.updateActiveStatus(any(), any())).thenReturn(null)

        assertThrows<IllegalArgumentException> {
            useCase.execute(UpdateRecruiterActiveStatusCommand("rec-1", true))
        }
    }
}
