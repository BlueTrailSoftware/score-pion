package org.example.notifier.application.useCases

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.service.core.RecruiterInvitationService
import org.example.notifier.application.service.core.UserService
import org.example.notifier.application.useCases.getAllAdmins.GetAllAdminsCommand
import org.example.notifier.application.useCases.getAllAdmins.GetAllAdminsUseCase
import org.example.notifier.domain.invitation.RecruiterInvitation
import org.example.notifier.domain.user.User
import org.example.notifier.domain.user.UserRole
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import java.time.LocalDateTime

class GetAllAdminsUseCaseTest {

    private lateinit var userService: UserService
    private lateinit var recruiterInvitationService: RecruiterInvitationService
    private lateinit var useCase: GetAllAdminsUseCase

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
    private val inactiveAdmin = adminUser.copy(id = "admin-2", isActive = false)
    private val pendingAdminInvitation = RecruiterInvitation(
        id = "inv-1",
        email = "new@test.com",
        invitedBy = "admin-1",
        assignedPositions = emptyList(),
        role = UserRole.ADMIN
    )
    private val pendingRecruiterInvitation = RecruiterInvitation(
        id = "inv-2",
        email = "rec@test.com",
        invitedBy = "admin-1",
        assignedPositions = listOf("pos-1"),
        role = UserRole.RECRUITER
    )

    @BeforeEach
    fun setup() {
        userService = mock(UserService::class.java)
        recruiterInvitationService = mock(RecruiterInvitationService::class.java)
        useCase = GetAllAdminsUseCase(userService, recruiterInvitationService)
    }

    @Test
    fun `execute should return active admins with Active status`() = runBlocking<Unit> {
        whenever(userService.findAllByRole(UserRole.ADMIN)).thenReturn(listOf(adminUser))
        whenever(recruiterInvitationService.getAllPendingInvitations()).thenReturn(listOf(pendingRecruiterInvitation))

        val result = useCase.execute(GetAllAdminsCommand())

        assertEquals(1, result.items.size)
        assertEquals(1, result.total)
        assertEquals("admin@test.com", result.items[0].email)
        assertEquals("Active", result.items[0].status)
    }

    @Test
    fun `execute should return inactive admin with Inactive status`() = runBlocking<Unit> {
        whenever(userService.findAllByRole(UserRole.ADMIN)).thenReturn(listOf(inactiveAdmin))
        whenever(recruiterInvitationService.getAllPendingInvitations()).thenReturn(emptyList())

        val result = useCase.execute(GetAllAdminsCommand())

        assertEquals("Inactive", result.items[0].status)
    }

    @Test
    fun `execute should include pending admin invitations`() = runBlocking<Unit> {
        whenever(userService.findAllByRole(UserRole.ADMIN)).thenReturn(emptyList())
        whenever(recruiterInvitationService.getAllPendingInvitations()).thenReturn(listOf(pendingAdminInvitation))

        val result = useCase.execute(GetAllAdminsCommand())

        assertEquals(1, result.items.size)
        assertEquals("Pending", result.items[0].status)
        assertEquals("", result.items[0].id)
    }

    @Test
    fun `execute should filter out pending recruiter invitations`() = runBlocking<Unit> {
        whenever(userService.findAllByRole(UserRole.ADMIN)).thenReturn(emptyList())
        whenever(recruiterInvitationService.getAllPendingInvitations()).thenReturn(listOf(pendingRecruiterInvitation))

        val result = useCase.execute(GetAllAdminsCommand())

        assertEquals(0, result.items.size)
    }

    @Test
    fun `execute should return empty PagedResult when no admins or invitations`() = runBlocking<Unit> {
        whenever(userService.findAllByRole(UserRole.ADMIN)).thenReturn(emptyList())
        whenever(recruiterInvitationService.getAllPendingInvitations()).thenReturn(emptyList())

        val result = useCase.execute(GetAllAdminsCommand())

        assertTrue(result.items.isEmpty())
        assertEquals(0, result.total)
    }

    @Test
    fun `execute paginates results correctly`() = runBlocking<Unit> {
        val admins = listOf(
            adminUser.copy(id = "a-1", createdAt = now),
            adminUser.copy(id = "a-2", createdAt = now.minusDays(1)),
            adminUser.copy(id = "a-3", createdAt = now.minusDays(2))
        )
        whenever(userService.findAllByRole(UserRole.ADMIN)).thenReturn(admins)
        whenever(recruiterInvitationService.getAllPendingInvitations()).thenReturn(emptyList())

        val page0 = useCase.execute(GetAllAdminsCommand(page = 0, pageSize = 2))
        val page1 = useCase.execute(GetAllAdminsCommand(page = 1, pageSize = 2))

        assertEquals(3, page0.total)
        assertEquals(2, page0.items.size)
        assertEquals(3, page1.total)
        assertEquals(1, page1.items.size)
    }
}
