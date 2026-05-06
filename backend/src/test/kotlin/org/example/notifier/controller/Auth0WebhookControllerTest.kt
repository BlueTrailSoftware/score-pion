package org.example.notifier.controller

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.service.core.RecruiterInvitationService
import org.example.notifier.application.service.core.UserService
import org.example.notifier.application.useCases.checkRecruiterInvitation.CheckRecruiterInvitationUseCase
import org.example.notifier.domain.invitation.RecruiterInvitation
import org.example.notifier.domain.user.User
import org.example.notifier.domain.user.UserRole
import org.example.notifier.infrastructure.controller.Auth0WebhookController
import org.example.notifier.infrastructure.logging.LoggerPort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

class Auth0WebhookControllerTest {

    private lateinit var recruiterInvitationService: RecruiterInvitationService
    private lateinit var userService: UserService
    private lateinit var logger: LoggerPort
    private lateinit var controller: Auth0WebhookController

    private val validSecret = "test-secret"
    private val adminEmail = "admin@example.com"

    @BeforeEach
    fun setup() {
        recruiterInvitationService = mock(RecruiterInvitationService::class.java)
        userService = mock(UserService::class.java)
        logger = mock(LoggerPort::class.java)
        val checkRecruiterInvitationUseCase = CheckRecruiterInvitationUseCase(
            recruiterInvitationService = recruiterInvitationService,
            userService = userService,
            logger = logger,
            adminEmail = adminEmail
        )
        controller = Auth0WebhookController(
            checkRecruiterInvitationUseCase = checkRecruiterInvitationUseCase,
            webhookSecret = validSecret,
            logger = logger
        )
    }

    @Test
    fun `checkInvitation returns 401 when secret is missing`() = runBlocking<Unit> {
        val response = controller.checkInvitation("recruiter@company.com", secret = null)
        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
    }

    @Test
    fun `checkInvitation returns 401 when secret is wrong`() = runBlocking<Unit> {
        val response = controller.checkInvitation("recruiter@company.com", secret = "wrong-secret")
        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
    }

    @Test
    fun `checkInvitation allows admin email regardless of invitation`() = runBlocking<Unit> {
        whenever(userService.findByEmail(adminEmail)).thenReturn(null)
        whenever(recruiterInvitationService.findByEmail(adminEmail)).thenReturn(null)

        val response = controller.checkInvitation(adminEmail, secret = validSecret)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(true, response.body?.get("allowed"))
    }

    @Test
    fun `checkInvitation allows new recruiter with valid pending invitation`() = runBlocking<Unit> {
        val email = "newrecruiter@company.com"
        val invitation = RecruiterInvitation(
            email = email,
            invitedBy = "admin-1",
            status = RecruiterInvitation.InvitationStatus.PENDING,
            expiresAt = LocalDateTime.now().plusDays(1)
        )

        whenever(userService.findByEmail(email)).thenReturn(null)
        whenever(recruiterInvitationService.findByEmail(email)).thenReturn(invitation)

        val response = controller.checkInvitation(email, secret = validSecret)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(true, response.body?.get("allowed"))
    }

    @Test
    fun `checkInvitation allows existing user even when invitation is already accepted`() = runBlocking<Unit> {
        val email = "existingrecruiter@company.com"
        val existingUser = User(
            id = "669338e8-758b-438f-a8b4-48c743bec14b",
            email = email,
            name = "Existing Recruiter",
            googleId = "google-123",
            role = UserRole.RECRUITER
        )
        val acceptedInvitation = RecruiterInvitation(
            email = email,
            invitedBy = "admin-1",
            status = RecruiterInvitation.InvitationStatus.ACCEPTED
        )

        whenever(userService.findByEmail(email)).thenReturn(existingUser)
        whenever(recruiterInvitationService.findByEmail(email)).thenReturn(acceptedInvitation)

        val response = controller.checkInvitation(email, secret = validSecret)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(true, response.body?.get("allowed"))
    }

    @Test
    fun `checkInvitation allows existing user even with no invitation at all`() = runBlocking<Unit> {
        val email = "existingrecruiter@company.com"
        val existingUser = User(
            id = "some-uuid",
            email = email,
            name = "Existing Recruiter",
            googleId = "google-456",
            role = UserRole.RECRUITER
        )

        whenever(userService.findByEmail(email)).thenReturn(existingUser)
        whenever(recruiterInvitationService.findByEmail(email)).thenReturn(null)

        val response = controller.checkInvitation(email, secret = validSecret)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(true, response.body?.get("allowed"))
    }

    @Test
    fun `checkInvitation blocks unknown email with no valid invitation`() = runBlocking<Unit> {
        val email = "unknown@company.com"

        whenever(userService.findByEmail(email)).thenReturn(null)
        whenever(recruiterInvitationService.findByEmail(email)).thenReturn(null)

        val response = controller.checkInvitation(email, secret = validSecret)

        assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
        assertEquals(false, response.body?.get("allowed"))
    }

    @Test
    fun `checkInvitation blocks unknown email with expired invitation`() = runBlocking<Unit> {
        val email = "expired@company.com"
        val expiredInvitation = RecruiterInvitation(
            email = email,
            invitedBy = "admin-1",
            status = RecruiterInvitation.InvitationStatus.PENDING,
            expiresAt = LocalDateTime.now().minusDays(1)
        )

        whenever(userService.findByEmail(email)).thenReturn(null)
        whenever(recruiterInvitationService.findByEmail(email)).thenReturn(expiredInvitation)

        val response = controller.checkInvitation(email, secret = validSecret)

        assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
        assertEquals(false, response.body?.get("allowed"))
    }
}
