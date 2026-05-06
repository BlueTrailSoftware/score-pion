package org.example.notifier.application.useCases.getRecruiters

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.service.core.OpenPositionService
import org.example.notifier.application.service.core.RecruiterInvitationService
import org.example.notifier.application.service.core.UserService
import org.example.notifier.domain.position.OpenPosition
import org.example.notifier.domain.invitation.RecruiterInvitation
import org.example.notifier.domain.user.User
import org.example.notifier.application.model.user.RecruiterListItem
import org.example.notifier.domain.user.UserRole
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDateTime

class GetRecruitersUseCaseTest {

    private lateinit var userService: UserService
    private lateinit var openPositionService: OpenPositionService
    private lateinit var recruiterInvitationService: RecruiterInvitationService
    private lateinit var useCase: GetRecruitersUseCase

    private val now = LocalDateTime.now()

    @BeforeEach
    fun setup() {
        userService = mock(UserService::class.java)
        openPositionService = mock(OpenPositionService::class.java)
        recruiterInvitationService = mock(RecruiterInvitationService::class.java)
        useCase = GetRecruitersUseCase(userService, openPositionService, recruiterInvitationService)
    }

    @Test
    fun `execute returns active recruiters with positionsCount from openPositionService`() = runBlocking<Unit> {
        val recruiter = User(id = "r-1", email = "r@example.com", name = "Alice", isActive = true, createdAt = now)
        val positions = listOf(
            OpenPosition(id = "p-1", title = "Dev", description = "", createdBy = "r-1"),
            OpenPosition(id = "p-2", title = "QA", description = "", createdBy = "r-1")
        )

        whenever(userService.findAllByRole(UserRole.RECRUITER)).thenReturn(listOf(recruiter))
        whenever(openPositionService.getRecruiterPositions("r-1")).thenReturn(positions)
        whenever(recruiterInvitationService.getAllPendingInvitations()).thenReturn(emptyList())

        val result = useCase.execute(GetRecruitersCommand())

        assertEquals(1, result.items.size)
        assertEquals(1, result.total)
        with(result.items[0]) {
            assertEquals("r-1", id)
            assertEquals("r@example.com", email)
            assertEquals("Alice", name)
            assertEquals(true, isActive)
            assertEquals("Active", status)
            assertEquals(2, positionsCount)
            assertEquals(now, createdAt)
        }
    }

    @Test
    fun `execute sets Inactive status for inactive recruiters`() = runBlocking<Unit> {
        val recruiter = User(id = "r-2", email = "inactive@example.com", name = "Bob", isActive = false, createdAt = now)

        whenever(userService.findAllByRole(UserRole.RECRUITER)).thenReturn(listOf(recruiter))
        whenever(openPositionService.getRecruiterPositions("r-2")).thenReturn(emptyList())
        whenever(recruiterInvitationService.getAllPendingInvitations()).thenReturn(emptyList())

        val result = useCase.execute(GetRecruitersCommand())

        assertEquals("Inactive", result.items[0].status)
        assertEquals(false, result.items[0].isActive)
    }

    @Test
    fun `execute returns pending invitations with Pending status`() = runBlocking<Unit> {
        val invitation = RecruiterInvitation(
            id = "inv-1",
            email = "pending@example.com",
            invitedBy = "admin-1",
            assignedPositions = listOf("p-1", "p-2"),
            createdAt = now
        )

        whenever(userService.findAllByRole(UserRole.RECRUITER)).thenReturn(emptyList())
        whenever(recruiterInvitationService.getAllPendingInvitations()).thenReturn(listOf(invitation))

        val result = useCase.execute(GetRecruitersCommand())

        assertEquals(1, result.items.size)
        assertEquals(1, result.total)
        with(result.items[0]) {
            assertEquals("", id)
            assertEquals("pending@example.com", email)
            assertEquals("", name)
            assertEquals(false, isActive)
            assertEquals("Pending", status)
            assertEquals(2, positionsCount)
            assertEquals(now, createdAt)
        }
    }

    @Test
    fun `execute sorts combined list by createdAt descending`() = runBlocking<Unit> {
        val recruiter = User(id = "r-1", email = "active@example.com", name = "Alice", isActive = true, createdAt = now)
        val invitation = RecruiterInvitation(email = "pending@example.com", invitedBy = "admin-1", createdAt = now.minusDays(1))

        whenever(userService.findAllByRole(UserRole.RECRUITER)).thenReturn(listOf(recruiter))
        whenever(openPositionService.getRecruiterPositions("r-1")).thenReturn(emptyList())
        whenever(recruiterInvitationService.getAllPendingInvitations()).thenReturn(listOf(invitation))

        val result = useCase.execute(GetRecruitersCommand())

        assertEquals(2, result.items.size)
        assertEquals("Active", result.items[0].status)
        assertEquals("Pending", result.items[1].status)
    }

    @Test
    fun `execute returns empty PagedResult when no recruiters and no invitations`() = runBlocking<Unit> {
        whenever(userService.findAllByRole(UserRole.RECRUITER)).thenReturn(emptyList())
        whenever(recruiterInvitationService.getAllPendingInvitations()).thenReturn(emptyList())

        val result = useCase.execute(GetRecruitersCommand())

        assertTrue(result.items.isEmpty())
        assertEquals(0, result.total)
    }

    @Test
    fun `execute calls getRecruiterPositions once per active recruiter`() = runBlocking<Unit> {
        val r1 = User(id = "r-1", email = "a@example.com", name = "A", isActive = true, createdAt = now)
        val r2 = User(id = "r-2", email = "b@example.com", name = "B", isActive = true, createdAt = now)

        whenever(userService.findAllByRole(UserRole.RECRUITER)).thenReturn(listOf(r1, r2))
        whenever(openPositionService.getRecruiterPositions("r-1")).thenReturn(emptyList())
        whenever(openPositionService.getRecruiterPositions("r-2")).thenReturn(emptyList())
        whenever(recruiterInvitationService.getAllPendingInvitations()).thenReturn(emptyList())

        useCase.execute(GetRecruitersCommand())

        verify(openPositionService).getRecruiterPositions("r-1")
        verify(openPositionService).getRecruiterPositions("r-2")
    }

    @Test
    fun `execute paginates combined list correctly`() = runBlocking<Unit> {
        val r1 = User(id = "r-1", email = "a@example.com", name = "A", isActive = true, createdAt = now)
        val r2 = User(id = "r-2", email = "b@example.com", name = "B", isActive = true, createdAt = now.minusDays(1))
        val r3 = User(id = "r-3", email = "c@example.com", name = "C", isActive = true, createdAt = now.minusDays(2))

        whenever(userService.findAllByRole(UserRole.RECRUITER)).thenReturn(listOf(r1, r2, r3))
        whenever(openPositionService.getRecruiterPositions("r-1")).thenReturn(emptyList())
        whenever(openPositionService.getRecruiterPositions("r-2")).thenReturn(emptyList())
        whenever(openPositionService.getRecruiterPositions("r-3")).thenReturn(emptyList())
        whenever(recruiterInvitationService.getAllPendingInvitations()).thenReturn(emptyList())

        val page0 = useCase.execute(GetRecruitersCommand(page = 0, pageSize = 2))
        val page1 = useCase.execute(GetRecruitersCommand(page = 1, pageSize = 2))

        assertEquals(3, page0.total)
        assertEquals(2, page0.items.size)
        assertEquals(3, page1.total)
        assertEquals(1, page1.items.size)
    }
}
