package org.example.notifier.application.useCases

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.service.core.OpenPositionService
import org.example.notifier.application.service.core.UserService
import org.example.notifier.application.service.notification.NotificationOrchestrator
import org.example.notifier.application.useCases.syncRecruiterPositions.SyncRecruiterPositionsCommand
import org.example.notifier.application.useCases.syncRecruiterPositions.SyncRecruiterPositionsUseCase
import org.example.notifier.domain.position.OpenPosition
import org.example.notifier.domain.user.User
import org.example.notifier.infrastructure.logging.LoggerPort
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDateTime

class SyncRecruiterPositionsUseCaseTest {

    private lateinit var openPositionService: OpenPositionService
    private lateinit var userService: UserService
    private lateinit var notificationOrchestrator: NotificationOrchestrator
    private lateinit var logger: LoggerPort
    private lateinit var useCase: SyncRecruiterPositionsUseCase

    @BeforeEach
    fun setup() {
        openPositionService = mock(OpenPositionService::class.java)
        userService = mock(UserService::class.java)
        notificationOrchestrator = mock(NotificationOrchestrator::class.java)
        logger = mock(LoggerPort::class.java)
        useCase = SyncRecruiterPositionsUseCase(openPositionService, userService, notificationOrchestrator, logger)
    }

    private fun position(id: String) = OpenPosition(
        id = id,
        title = "Position $id",
        description = "",
        external = false,
        createdBy = "admin-1",
        createdAt = LocalDateTime.now()
    )

    @Test
    fun `grants access to new positions not currently assigned`() = runBlocking<Unit> {
        whenever(openPositionService.getRecruiterPositions("rec-1")).thenReturn(emptyList())
        whenever(openPositionService.getPositionsByIdsBatch(listOf("pos-1"))).thenReturn(listOf(position("pos-1")))

        useCase.execute(SyncRecruiterPositionsCommand("rec-1", listOf("pos-1"), "admin-1"))

        verify(openPositionService).grantRecruiterAccess("rec-1", "pos-1", "admin-1")
        verify(openPositionService, never()).revokeRecruiterAccess(any(), any())
    }

    @Test
    fun `revokes access to positions no longer in desired list`() = runBlocking<Unit> {
        whenever(openPositionService.getRecruiterPositions("rec-1")).thenReturn(listOf(position("pos-old")))
        whenever(openPositionService.getPositionsByIdsBatch(emptyList())).thenReturn(emptyList())

        useCase.execute(SyncRecruiterPositionsCommand("rec-1", emptyList(), "admin-1"))

        verify(openPositionService).revokeRecruiterAccess("rec-1", "pos-old")
        verify(openPositionService, never()).grantRecruiterAccess(any(), any(), any())
    }

    @Test
    fun `positions present in both current and desired are left unchanged`() = runBlocking<Unit> {
        whenever(openPositionService.getRecruiterPositions("rec-1")).thenReturn(listOf(position("pos-keep")))
        whenever(openPositionService.getPositionsByIdsBatch(listOf("pos-keep"))).thenReturn(listOf(position("pos-keep")))

        useCase.execute(SyncRecruiterPositionsCommand("rec-1", listOf("pos-keep"), "admin-1"))

        verify(openPositionService, never()).grantRecruiterAccess(any(), any(), any())
        verify(openPositionService, never()).revokeRecruiterAccess(any(), any())
    }

    @Test
    fun `sends notification with assigned positions after sync`() = runBlocking<Unit> {
        val pos1 = position("pos-1")
        val recruiter = User(id = "rec-1", email = "recruiter@example.com", name = "Alice")
        whenever(openPositionService.getRecruiterPositions("rec-1")).thenReturn(emptyList())
        whenever(openPositionService.getPositionsByIdsBatch(listOf("pos-1"))).thenReturn(listOf(pos1))
        whenever(userService.findById("rec-1")).thenReturn(recruiter)

        useCase.execute(SyncRecruiterPositionsCommand("rec-1", listOf("pos-1"), "admin-1"))

        verify(notificationOrchestrator).notifyPositionAssignment("recruiter@example.com", "Alice", listOf(pos1))
    }

    @Test
    fun `skips notification when recruiter is not found`() = runBlocking<Unit> {
        whenever(openPositionService.getRecruiterPositions("rec-1")).thenReturn(emptyList())
        whenever(openPositionService.getPositionsByIdsBatch(listOf("pos-1"))).thenReturn(listOf(position("pos-1")))
        whenever(userService.findById("rec-1")).thenReturn(null)

        useCase.execute(SyncRecruiterPositionsCommand("rec-1", listOf("pos-1"), "admin-1"))

        verify(notificationOrchestrator, never()).notifyPositionAssignment(any(), any(), any())
    }

    @Test
    fun `continues sync when granting access to an invalid position throws`() = runBlocking<Unit> {
        whenever(openPositionService.getRecruiterPositions("rec-1")).thenReturn(emptyList())
        whenever(openPositionService.grantRecruiterAccess("rec-1", "pos-bad", "admin-1"))
            .thenThrow(IllegalArgumentException("Position not found"))
        whenever(openPositionService.grantRecruiterAccess("rec-1", "pos-ok", "admin-1")).thenReturn(mock())
        whenever(openPositionService.getPositionsByIdsBatch(any())).thenReturn(emptyList())

        useCase.execute(SyncRecruiterPositionsCommand("rec-1", listOf("pos-bad", "pos-ok"), "admin-1"))

        verify(openPositionService).grantRecruiterAccess("rec-1", "pos-bad", "admin-1")
        verify(openPositionService).grantRecruiterAccess("rec-1", "pos-ok", "admin-1")
    }
}