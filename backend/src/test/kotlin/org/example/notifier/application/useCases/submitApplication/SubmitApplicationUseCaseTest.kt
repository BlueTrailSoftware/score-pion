package org.example.notifier.application.useCases.submitApplication

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.service.core.ApplicantService
import org.example.notifier.application.service.core.InvitationService
import org.example.notifier.application.service.core.OpenPositionService
import org.example.notifier.application.service.file.FileService
import org.example.notifier.application.service.integration.CaptchaService
import org.example.notifier.domain.applicant.Applicant
import org.example.notifier.domain.applicant.ApplicantStatus
import org.example.notifier.domain.event.CandidateApplicationEvent
import org.example.notifier.domain.position.OpenPosition
import org.example.notifier.infrastructure.logging.LoggerPort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.codec.multipart.FilePart
import java.time.LocalDateTime

class SubmitApplicationUseCaseTest {

    private lateinit var captchaService: CaptchaService
    private lateinit var applicantService: ApplicantService
    private lateinit var invitationService: InvitationService
    private lateinit var openPositionService: OpenPositionService
    private lateinit var fileService: FileService
    private lateinit var eventPublisher: ApplicationEventPublisher
    private lateinit var logger: LoggerPort
    private lateinit var useCase: SubmitApplicationUseCase

    private val now = LocalDateTime.now()
    private val positionId = "pos-1"

    private val activePosition = OpenPosition(
        id = positionId,
        title = "Backend Engineer",
        description = "Kotlin dev",
        isActive = true,
        createdBy = "admin@example.com"
    )

    private val savedApplicant = Applicant(
        id = "app-1",
        name = "Jane Doe",
        email = "jane@example.com",
        phone = "123456789",
        positionId = positionId,
        status = ApplicantStatus.PENDING,
        createdAt = now,
        updatedAt = now,
        deleteAfter = now.plusMonths(12)
    )

    @BeforeEach
    fun setup() {
        captchaService = mock(CaptchaService::class.java)
        applicantService = mock(ApplicantService::class.java)
        invitationService = mock(InvitationService::class.java)
        openPositionService = mock(OpenPositionService::class.java)
        fileService = mock(FileService::class.java)
        eventPublisher = mock(ApplicationEventPublisher::class.java)
        logger = mock(LoggerPort::class.java)
        useCase = SubmitApplicationUseCase(
            captchaService = captchaService,
            applicantService = applicantService,
            invitationService = invitationService,
            openPositionService = openPositionService,
            fileService = fileService,
            eventPublisher = eventPublisher,
            logger = logger,
            dataRetentionMonths = 12L
        )
    }

    @Test
    fun `execute throws when captcha validation fails`() = runBlocking<Unit> {
        whenever(captchaService.validateToken("bad-token", "apply")).thenReturn(false)

        val ex = assertThrows<IllegalArgumentException> {
            useCase.execute(buildCommand(captchaToken = "bad-token"))
        }
        assertEquals("Security validation failed. Please try again.", ex.message)
        verify(openPositionService, never()).getPosition(any())
    }

    @Test
    fun `execute throws when filePart is null and linkedinUrl is blank`() = runBlocking<Unit> {
        whenever(captchaService.validateToken(any(), any())).thenReturn(true)

        val ex = assertThrows<IllegalArgumentException> {
            useCase.execute(buildCommand(filePart = null, linkedinUrl = null))
        }
        assertEquals("Please provide either a CV file or a LinkedIn profile URL", ex.message)
        verify(openPositionService, never()).getPosition(any())
    }

    @Test
    fun `execute throws when position not found`() = runBlocking<Unit> {
        whenever(captchaService.validateToken(any(), any())).thenReturn(true)
        whenever(openPositionService.getPosition(positionId)).thenReturn(null)

        val ex = assertThrows<IllegalArgumentException> {
            useCase.execute(buildCommand())
        }
        assertEquals("Position not found with id: $positionId", ex.message)
    }

    @Test
    fun `execute throws when position is not active`() = runBlocking<Unit> {
        whenever(captchaService.validateToken(any(), any())).thenReturn(true)
        whenever(openPositionService.getPosition(positionId)).thenReturn(activePosition.copy(isActive = false))

        val ex = assertThrows<IllegalArgumentException> {
            useCase.execute(buildCommand())
        }
        assertEquals("Position $positionId is not currently accepting applications", ex.message)
    }

    @Test
    fun `execute throws on invalid email format`() = runBlocking<Unit> {
        whenever(captchaService.validateToken(any(), any())).thenReturn(true)
        whenever(openPositionService.getPosition(positionId)).thenReturn(activePosition)

        val ex = assertThrows<IllegalArgumentException> {
            useCase.execute(buildCommand(email = "not-an-email"))
        }
        assertEquals("Invalid email format: not-an-email", ex.message)
    }

    @Test
    fun `execute throws when name is blank`() = runBlocking<Unit> {
        whenever(captchaService.validateToken(any(), any())).thenReturn(true)
        whenever(openPositionService.getPosition(positionId)).thenReturn(activePosition)

        val ex = assertThrows<IllegalArgumentException> {
            useCase.execute(buildCommand(name = "   "))
        }
        assertEquals("Name cannot be empty", ex.message)
    }

    @Test
    fun `execute throws on invalid phone format`() = runBlocking<Unit> {
        whenever(captchaService.validateToken(any(), any())).thenReturn(true)
        whenever(openPositionService.getPosition(positionId)).thenReturn(activePosition)

        val ex = assertThrows<IllegalArgumentException> {
            useCase.execute(buildCommand(phone = "abc"))
        }
        assertEquals("Invalid phone format: abc", ex.message)
    }

    @Test
    fun `execute throws on invalid linkedin url`() = runBlocking<Unit> {
        whenever(captchaService.validateToken(any(), any())).thenReturn(true)
        whenever(openPositionService.getPosition(positionId)).thenReturn(activePosition)

        val ex = assertThrows<IllegalArgumentException> {
            useCase.execute(buildCommand(linkedinUrl = "not-a-linkedin-url"))
        }
        assertEquals("Invalid LinkedIn profile URL format", ex.message)
    }

    @Test
    fun `execute throws when applicant already applied`() = runBlocking<Unit> {
        whenever(captchaService.validateToken(any(), any())).thenReturn(true)
        whenever(openPositionService.getPosition(positionId)).thenReturn(activePosition)
        whenever(applicantService.findByEmailAndPositionId("jane@example.com", positionId))
            .thenReturn(savedApplicant)

        val ex = assertThrows<IllegalArgumentException> {
            useCase.execute(buildCommand())
        }
        assertEquals("You have already applied to this position", ex.message)
    }

    @Test
    fun `execute throws when candidate was already invited by recruiter`() = runBlocking<Unit> {
        whenever(captchaService.validateToken(any(), any())).thenReturn(true)
        whenever(openPositionService.getPosition(positionId)).thenReturn(activePosition)
        whenever(applicantService.findByEmailAndPositionId(any(), any())).thenReturn(null)
        whenever(invitationService.existsForCandidateAndPosition("jane@example.com", positionId)).thenReturn(true)

        val ex = assertThrows<IllegalArgumentException> {
            useCase.execute(buildCommand())
        }
        assertEquals("You have already been invited to this position by a recruiter", ex.message)
    }

    @Test
    fun `execute happy path with linkedinUrl returns ApplicantItem with positionTitle`() = runBlocking<Unit> {
        whenever(captchaService.validateToken(any(), any())).thenReturn(true)
        whenever(openPositionService.getPosition(positionId)).thenReturn(activePosition)
        whenever(applicantService.findByEmailAndPositionId(any(), any())).thenReturn(null)
        whenever(invitationService.existsForCandidateAndPosition(any(), any())).thenReturn(false)
        whenever(applicantService.createApplicant(any())).thenReturn(savedApplicant)

        val result = useCase.execute(buildCommand(filePart = null, linkedinUrl = "https://linkedin.com/in/jane"))

        assertEquals("app-1", result.id)
        assertEquals("jane@example.com", result.email)
        assertEquals("PENDING", result.status)
        assertEquals("Backend Engineer", result.positionTitle)
        verify(fileService, never()).upload(any(), any(), any())
    }

    @Test
    fun `execute happy path with filePart calls fileService upload`() = runBlocking<Unit> {
        val filePart = mock(FilePart::class.java)
        whenever(captchaService.validateToken(any(), any())).thenReturn(true)
        whenever(openPositionService.getPosition(positionId)).thenReturn(activePosition)
        whenever(applicantService.findByEmailAndPositionId(any(), any())).thenReturn(null)
        whenever(invitationService.existsForCandidateAndPosition(any(), any())).thenReturn(false)
        whenever(fileService.upload(any(), any(), any())).thenReturn("https://s3/applicant/app-1/cv.pdf")
        whenever(applicantService.createApplicant(any())).thenReturn(savedApplicant)

        val result = useCase.execute(buildCommand(filePart = filePart, linkedinUrl = null))

        assertNotNull(result)
        verify(fileService).upload(eq(filePart), eq("applicant"), any())
    }

    @Test
    fun `execute publishes candidate application event on success`() = runBlocking<Unit> {
        whenever(captchaService.validateToken(any(), any())).thenReturn(true)
        whenever(openPositionService.getPosition(positionId)).thenReturn(activePosition)
        whenever(applicantService.findByEmailAndPositionId(any(), any())).thenReturn(null)
        whenever(invitationService.existsForCandidateAndPosition(any(), any())).thenReturn(false)
        whenever(applicantService.createApplicant(any())).thenReturn(savedApplicant)

        useCase.execute(buildCommand(filePart = null, linkedinUrl = "https://linkedin.com/in/jane"))

        verify(eventPublisher).publishEvent(argThat<CandidateApplicationEvent> { event ->
            event is CandidateApplicationEvent
                && event.candidateEmail == "jane@example.com"
                && event.candidateName == "Jane Doe"
                && event.positionTitle == "Backend Engineer"
        })
    }

    @Test
    fun `execute does not throw when event publishing fails`() = runBlocking<Unit> {
        whenever(captchaService.validateToken(any(), any())).thenReturn(true)
        whenever(openPositionService.getPosition(positionId)).thenReturn(activePosition)
        whenever(applicantService.findByEmailAndPositionId(any(), any())).thenReturn(null)
        whenever(invitationService.existsForCandidateAndPosition(any(), any())).thenReturn(false)
        whenever(applicantService.createApplicant(any())).thenReturn(savedApplicant)
        whenever(eventPublisher.publishEvent(any())).thenThrow(RuntimeException("event bus down"))

        val result = useCase.execute(buildCommand())

        assertNotNull(result)
    }

    private fun buildCommand(
        captchaToken: String = "valid-token",
        name: String = "Jane Doe",
        email: String = "jane@example.com",
        phone: String? = "123456789",
        filePart: FilePart? = null,
        linkedinUrl: String? = "https://linkedin.com/in/jane"
    ) = SubmitApplicationCommand(
        name = name,
        email = email,
        phone = phone,
        positionId = positionId,
        filePart = filePart,
        linkedinUrl = linkedinUrl,
        gdprConsent = true,
        captchaToken = captchaToken
    )
}
