package org.example.notifier.application.useCases

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.service.core.UserService
import org.example.notifier.application.useCases.updateAdminActiveStatus.UpdateAdminActiveStatusCommand
import org.example.notifier.application.useCases.updateAdminActiveStatus.UpdateAdminActiveStatusUseCase
import org.example.notifier.domain.user.User
import org.example.notifier.domain.user.UserRole
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDateTime

class UpdateAdminActiveStatusUseCaseTest {

    private lateinit var userService: UserService
    private lateinit var useCase: UpdateAdminActiveStatusUseCase

    private val now = LocalDateTime.now()

    private val adminUser = User(
        id = "admin-1",
        email = "admin@test.com",
        name = "Admin",
        role = UserRole.ADMIN,
        isActive = true,
        createdAt = now,
        updatedAt = now
    )
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
        useCase = UpdateAdminActiveStatusUseCase(userService)
    }

    @Test
    fun `execute should activate admin when user is found and role is ADMIN`() = runBlocking<Unit> {
        whenever(userService.findById("admin-1")).thenReturn(adminUser)
        whenever(userService.updateActiveStatus("admin-1", true)).thenReturn(adminUser.copy(isActive = true))

        val result = useCase.execute(UpdateAdminActiveStatusCommand("admin-1", true))

        assertEquals(true, result.isActive)
    }

    @Test
    fun `execute should deactivate admin when user is found and role is ADMIN`() = runBlocking<Unit> {
        whenever(userService.findById("admin-1")).thenReturn(adminUser)
        whenever(userService.updateActiveStatus("admin-1", false)).thenReturn(adminUser.copy(isActive = false))

        val result = useCase.execute(UpdateAdminActiveStatusCommand("admin-1", false))

        assertEquals(false, result.isActive)
    }

    @Test
    fun `execute should throw IllegalArgumentException when user not found`() = runBlocking<Unit> {
        whenever(userService.findById("missing")).thenReturn(null)

        assertThrows<IllegalArgumentException> {
            useCase.execute(UpdateAdminActiveStatusCommand("missing", true))
        }

        verify(userService, never()).updateActiveStatus(any(), any())
    }

    @Test
    fun `execute should throw IllegalArgumentException when user role is not ADMIN`() = runBlocking<Unit> {
        whenever(userService.findById("rec-1")).thenReturn(recruiterUser)

        assertThrows<IllegalArgumentException> {
            useCase.execute(UpdateAdminActiveStatusCommand("rec-1", true))
        }

        verify(userService, never()).updateActiveStatus(any(), any())
    }

    @Test
    fun `execute should throw IllegalArgumentException when updateActiveStatus returns null`() = runBlocking<Unit> {
        whenever(userService.findById("admin-1")).thenReturn(adminUser)
        whenever(userService.updateActiveStatus(any(), any())).thenReturn(null)

        assertThrows<IllegalArgumentException> {
            useCase.execute(UpdateAdminActiveStatusCommand("admin-1", true))
        }
    }
}
