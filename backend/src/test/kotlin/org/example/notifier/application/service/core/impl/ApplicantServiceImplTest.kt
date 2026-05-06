package org.example.notifier.application.service.core.impl

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.service.core.OpenPositionService
import org.example.notifier.application.service.file.FileService
import org.example.notifier.domain.applicant.Applicant
import org.example.notifier.domain.applicant.ApplicantStatus
import org.example.notifier.domain.applicant.CandidatePositionKey
import org.example.notifier.domain.position.OpenPosition
import org.example.notifier.domain.port.ApplicantRepository
import org.example.notifier.infrastructure.logging.LoggerPort
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.access.AccessDeniedException
import java.time.LocalDateTime

class ApplicantServiceImplTest {

    private lateinit var applicantRepository: ApplicantRepository
    private lateinit var openPositionService: OpenPositionService
    private lateinit var fileService: FileService
    private lateinit var logger: LoggerPort
    private lateinit var service: ApplicantServiceImpl

    private val now = LocalDateTime.now()
    private val positionId = "pos-1"

    @BeforeEach
    fun setup() {
        applicantRepository = mock(ApplicantRepository::class.java)
        openPositionService = mock(OpenPositionService::class.java)
        fileService = mock(FileService::class.java)
        logger = mock(LoggerPort::class.java)
        service = ApplicantServiceImpl(
            applicantRepository = applicantRepository,
            openPositionService = openPositionService,
            fileService = fileService,
            logger = logger
        )
    }

    // --- findByEmailAndPositionId ---

    @Test
    fun `findByEmailAndPositionId returns applicant when found`() = runBlocking<Unit> {
        val applicant = buildApplicant()
        whenever(applicantRepository.findByEmailAndPositionId("jane@example.com", positionId))
            .thenReturn(applicant)

        assertEquals(applicant, service.findByEmailAndPositionId("jane@example.com", positionId))
    }

    @Test
    fun `findByEmailAndPositionId returns null when not found`() = runBlocking<Unit> {
        whenever(applicantRepository.findByEmailAndPositionId(any(), any())).thenReturn(null)

        assertNull(service.findByEmailAndPositionId("jane@example.com", positionId))
    }

    // --- createApplicant ---

    @Test
    fun `createApplicant delegates to repository and returns saved applicant`() = runBlocking<Unit> {
        val applicant = buildApplicant()
        whenever(applicantRepository.save(applicant)).thenReturn(applicant)

        assertEquals(applicant, service.createApplicant(applicant))
    }

    // --- getAllApplicants ---

    @Test
    fun `getAllApplicants queries all when no positionId`() = runBlocking<Unit> {
        whenever(applicantRepository.findAll()).thenReturn(emptyList())

        service.getAllApplicants(null, null, null)

        verify(applicantRepository).findAll()
        verify(applicantRepository, never()).findByPositionId(any())
    }

    @Test
    fun `getAllApplicants queries by positionId when provided`() = runBlocking<Unit> {
        whenever(applicantRepository.findByPositionId("pos-1")).thenReturn(emptyList())

        service.getAllApplicants(null, "pos-1", null)

        verify(applicantRepository).findByPositionId("pos-1")
        verify(applicantRepository, never()).findAll()
    }

    @Test
    fun `getAllApplicants excludes ANONYMIZED applicants`() = runBlocking<Unit> {
        val anonymized = buildApplicant().copy(status = ApplicantStatus.ANONYMIZED)
        whenever(applicantRepository.findAll()).thenReturn(listOf(anonymized))

        assertTrue(service.getAllApplicants(null, null, null).isEmpty())
    }

    @Test
    fun `getAllApplicants filters by status`() = runBlocking<Unit> {
        val pending = buildApplicant(id = "app-1").copy(status = ApplicantStatus.PENDING)
        val rejected = buildApplicant(id = "app-2").copy(status = ApplicantStatus.REJECTED)
        whenever(applicantRepository.findAll()).thenReturn(listOf(pending, rejected))

        val result = service.getAllApplicants("PENDING", null, null)

        assertEquals(1, result.size)
        assertEquals("app-1", result[0].id)
    }

    @Test
    fun `getAllApplicants search matches by name`() = runBlocking<Unit> {
        val match = buildApplicant(id = "app-1", email = "jane@example.com").copy(name = "Jane Doe")
        val noMatch = buildApplicant(id = "app-2", email = "john@example.com").copy(name = "John Smith")
        whenever(applicantRepository.findAll()).thenReturn(listOf(match, noMatch))
        whenever(openPositionService.getPosition(any())).thenReturn(null)

        val result = service.getAllApplicants(null, null, "jane")

        assertEquals(1, result.size)
        assertEquals("app-1", result[0].id)
    }

    @Test
    fun `getAllApplicants search matches by position title`() = runBlocking<Unit> {
        val applicant = buildApplicant(positionId = "pos-1")
        val position = OpenPosition(id = "pos-1", title = "Backend Engineer", description = "", createdBy = "admin-1", createdAt = now, updatedAt = now)
        whenever(applicantRepository.findAll()).thenReturn(listOf(applicant))
        whenever(openPositionService.getPosition("pos-1")).thenReturn(position)

        val result = service.getAllApplicants(null, null, "backend")

        assertEquals(1, result.size)
    }

    // --- getApplicantsForRecruiter ---

    @Test
    fun `getApplicantsForRecruiter returns only applicants matching invitedKeys`() = runBlocking<Unit> {
        val invited = buildApplicant(id = "app-1", email = "jane@example.com")
        val notInvited = buildApplicant(id = "app-2", email = "other@example.com")
        val invitedKeys = setOf(CandidatePositionKey("jane@example.com", positionId))
        whenever(applicantRepository.findByPositionId(positionId)).thenReturn(listOf(invited, notInvited))

        val result = service.getApplicantsForRecruiter(invitedKeys, null, null, null)

        assertEquals(1, result.size)
        assertEquals("app-1", result[0].id)
    }

    @Test
    fun `getApplicantsForRecruiter throws AccessDeniedException for position not in invitedKeys`() {
        val invitedKeys = setOf(CandidatePositionKey("jane@example.com", "pos-1"))

        assertThrows(AccessDeniedException::class.java) {
            runBlocking { service.getApplicantsForRecruiter(invitedKeys, null, "pos-forbidden", null) }
        }
    }

    @Test
    fun `getApplicantsForRecruiter queries only the specified positionId when provided`() = runBlocking<Unit> {
        val invitedKeys = setOf(CandidatePositionKey("jane@example.com", "pos-1"), CandidatePositionKey("other@example.com", "pos-2"))
        whenever(applicantRepository.findByPositionId("pos-1")).thenReturn(emptyList())

        service.getApplicantsForRecruiter(invitedKeys, null, "pos-1", null)

        verify(applicantRepository).findByPositionId("pos-1")
        verify(applicantRepository, never()).findByPositionId("pos-2")
    }

    @Test
    fun `getApplicantsForRecruiter queries all allowed positions when no positionId filter`() = runBlocking<Unit> {
        val invitedKeys = setOf(CandidatePositionKey("a@example.com", "pos-1"), CandidatePositionKey("b@example.com", "pos-2"))
        whenever(applicantRepository.findByPositionId(any())).thenReturn(emptyList())

        service.getApplicantsForRecruiter(invitedKeys, null, null, null)

        verify(applicantRepository).findByPositionId("pos-1")
        verify(applicantRepository).findByPositionId("pos-2")
    }

    @Test
    fun `getApplicantsForRecruiter excludes ANONYMIZED applicants`() = runBlocking<Unit> {
        val anonymized = buildApplicant(email = "jane@example.com").copy(status = ApplicantStatus.ANONYMIZED)
        val invitedKeys = setOf(CandidatePositionKey("jane@example.com", positionId))
        whenever(applicantRepository.findByPositionId(positionId)).thenReturn(listOf(anonymized))

        assertTrue(service.getApplicantsForRecruiter(invitedKeys, null, null, null).isEmpty())
    }

    @Test
    fun `getApplicantsForRecruiter filters by status`() = runBlocking<Unit> {
        val pending = buildApplicant(id = "app-1", email = "jane@example.com").copy(status = ApplicantStatus.PENDING)
        val rejected = buildApplicant(id = "app-2", email = "jane@example.com").copy(status = ApplicantStatus.REJECTED)
        val invitedKeys = setOf(CandidatePositionKey("jane@example.com", positionId))
        whenever(applicantRepository.findByPositionId(positionId)).thenReturn(listOf(pending, rejected))

        val result = service.getApplicantsForRecruiter(invitedKeys, "REJECTED", null, null)

        assertEquals(1, result.size)
        assertEquals("app-2", result[0].id)
    }

    // --- updateApplicant ---

    @Test
    fun `updateApplicant updates all provided fields`() = runBlocking<Unit> {
        val existing = buildApplicant()
        whenever(applicantRepository.findById("app-1")).thenReturn(existing)
        whenever(applicantRepository.save(any())).thenAnswer { it.arguments[0] as Applicant }

        val result = service.updateApplicant(
            id = "app-1", name = "New Name", email = "new@example.com",
            phone = "+1234567890", fileUrl = "https://s3/new.pdf", isFileDeleted = false
        )

        assertEquals("New Name", result.name)
        assertEquals("new@example.com", result.email)
        assertEquals("+1234567890", result.phone)
        assertEquals("https://s3/new.pdf", result.fileUrl)
    }

    @Test
    fun `updateApplicant keeps existing values when null is passed`() = runBlocking<Unit> {
        val existing = buildApplicant()
        whenever(applicantRepository.findById("app-1")).thenReturn(existing)
        whenever(applicantRepository.save(any())).thenAnswer { it.arguments[0] as Applicant }

        val result = service.updateApplicant("app-1", null, null, null, null, false)

        assertEquals("Jane Doe", result.name)
        assertEquals("jane@example.com", result.email)
        assertNull(result.phone)
    }

    @Test
    fun `updateApplicant sets isFileDeleted flag`() = runBlocking<Unit> {
        val existing = buildApplicant().copy(fileUrl = "https://s3/old.pdf")
        whenever(applicantRepository.findById("app-1")).thenReturn(existing)
        whenever(applicantRepository.save(any())).thenAnswer { it.arguments[0] as Applicant }

        val result = service.updateApplicant("app-1", null, null, null, null, isFileDeleted = true)

        assertNull(result.fileUrl)
        assertTrue(result.isFileDeleted)
    }

    @Test
    fun `updateApplicant throws when applicant not found`() = runBlocking<Unit> {
        whenever(applicantRepository.findById("missing")).thenReturn(null)

        val ex = assertThrows(IllegalArgumentException::class.java) {
            runBlocking { service.updateApplicant("missing", null, null, null, null, false) }
        }

        assertEquals("Applicant not found with id: missing", ex.message)
    }

    // --- helpers ---

    private fun buildApplicant(
        id: String = "app-1",
        email: String = "jane@example.com",
        positionId: String = this.positionId
    ) = Applicant(
        id = id,
        name = "Jane Doe",
        email = email,
        phone = null,
        positionId = positionId,
        status = ApplicantStatus.PENDING,
        deleteAfter = now.plusMonths(12),
        createdAt = now,
        updatedAt = now
    )
}