package org.example.notifier.application.event

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.service.core.OpenPositionService
import org.example.notifier.domain.invitation.RecruiterInvitation
import org.example.notifier.domain.user.User
import org.example.notifier.domain.event.RecruiterCreatedEvent
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.*

class RecruiterEventListenerTest {

    private lateinit var openPositionService: OpenPositionService
    private lateinit var listener: RecruiterEventListener

    @BeforeEach
    fun setUp() {
        openPositionService = mock(OpenPositionService::class.java)
        listener = RecruiterEventListener(openPositionService)
    }

    private fun buildRecruiter() = User(
        id = "rec-1",
        email = "recruiter@test.com",
        name = "Recruiter A",
        role = "RECRUITER"
    )

    private fun buildInvitation(positions: List<String> = emptyList()) = RecruiterInvitation(
        email = "recruiter@test.com",
        invitedBy = "admin-1",
        assignedPositions = positions
    )

    // onRecruiterCreated launches a fire-and-forget CoroutineScope(Dispatchers.IO).launch.
    // Since the coroutine scope is not injectable, we wait for IO dispatcher completion.
    private fun awaitCoroutine() = Thread.sleep(300)

    @Test
    fun `onRecruiterCreated with positions grants access for each position`() = runBlocking<Unit> {
        val event = RecruiterCreatedEvent(
            recruiter = buildRecruiter(),
            invitation = buildInvitation(listOf("pos-1", "pos-2"))
        )

        listener.onRecruiterCreated(event)
        awaitCoroutine()

        verify(openPositionService).grantRecruiterAccess(
            recruiterId = "rec-1", positionId = "pos-1", grantedBy = "admin-1"
        )
        verify(openPositionService).grantRecruiterAccess(
            recruiterId = "rec-1", positionId = "pos-2", grantedBy = "admin-1"
        )
    }

    @Test
    fun `onRecruiterCreated with empty positions does not call grantRecruiterAccess`() = runBlocking<Unit> {
        val event = RecruiterCreatedEvent(
            recruiter = buildRecruiter(),
            invitation = buildInvitation(emptyList())
        )

        listener.onRecruiterCreated(event)
        awaitCoroutine()

        verifyNoInteractions(openPositionService)
    }

    @Test
    fun `onRecruiterCreated when grantRecruiterAccess throws exception is caught and does not propagate`() = runBlocking<Unit> {
        whenever(openPositionService.grantRecruiterAccess(any(), any(), any()))
            .thenThrow(RuntimeException("DynamoDB error"))

        val event = RecruiterCreatedEvent(
            recruiter = buildRecruiter(),
            invitation = buildInvitation(listOf("pos-1", "pos-2"))
        )

        listener.onRecruiterCreated(event)
        awaitCoroutine()

        // Exception was caught — only first call was attempted, forEach stops after the throw
        verify(openPositionService, times(1)).grantRecruiterAccess(any(), any(), any())
    }
}
