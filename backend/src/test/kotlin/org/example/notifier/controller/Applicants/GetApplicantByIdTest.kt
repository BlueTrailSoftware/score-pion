package org.example.notifier.controller.Applicants

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import org.example.notifier.application.model.applicant.ApplicantItem
import org.example.notifier.application.useCases.getApplicantById.GetApplicantByIdCommand
import org.example.notifier.application.useCases.getApplicantById.GetApplicantByIdUseCase
import org.example.notifier.application.useCases.getApplicants.GetApplicantsUseCase
import org.example.notifier.application.useCases.getPositionById.GetPositionByIdUseCase
import org.example.notifier.application.useCases.getPublicPositions.GetPublicPositionsUseCase
import org.example.notifier.application.useCases.submitApplication.SubmitApplicationUseCase
import org.example.notifier.application.useCases.updateApplicant.UpdateApplicantUseCase
import org.example.notifier.application.useCases.updateApplicantStatus.UpdateApplicantStatusUseCase
import org.example.notifier.domain.user.User
import org.example.notifier.domain.user.UserRole
import org.example.notifier.infrastructure.controller.ApplicantsController
import org.example.notifier.infrastructure.logging.LoggerPort
import org.example.notifier.infrastructure.security.SecurityUtils
import org.example.notifier.infrastructure.util.factory.ResponseEntityFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

class GetApplicantByIdTest {

    private lateinit var submitApplicationUseCase: SubmitApplicationUseCase
    private lateinit var getPublicPositionsUseCase: GetPublicPositionsUseCase
    private lateinit var getPositionByIdUseCase: GetPositionByIdUseCase
    private lateinit var getApplicantsUseCase: GetApplicantsUseCase
    private lateinit var getApplicantByIdUseCase: GetApplicantByIdUseCase
    private lateinit var updateApplicantStatusUseCase: UpdateApplicantStatusUseCase
    private lateinit var updateApplicantUseCase: UpdateApplicantUseCase
    private lateinit var securityUtils: SecurityUtils
    private lateinit var logger: LoggerPort
    private lateinit var controller: ApplicantsController

    private val responseFactory = ResponseEntityFactory()
    private val objectMapper = ObjectMapper().apply { findAndRegisterModules() }
    private val now = LocalDateTime.now()

    private val adminUser = User(
        id = "admin-1",
        email = "admin@example.com",
        name = "Admin",
        role = UserRole.ADMIN,
        isActive = true,
        createdAt = now,
        updatedAt = now
    )

    private val applicantItem = ApplicantItem(
        id = "app-1",
        name = "Jane Doe",
        email = "jane@example.com",
        phone = "123456789",
        positionId = "pos-1",
        positionTitle = "Backend Engineer",
        status = "PENDING",
        source = "self_application",
        createdAt = now,
        updatedAt = now,
        reviewedBy = null,
        reviewedAt = null,
        fileUrl = "https://s3.example.com/cv.pdf",
        linkedinUrl = "https://linkedin.com/in/jane",
        isFileDeleted = false,
        statusNote = null,
        assessments = null
    )

    @BeforeEach
    fun setup() {
        submitApplicationUseCase = mock(SubmitApplicationUseCase::class.java)
        getPublicPositionsUseCase = mock(GetPublicPositionsUseCase::class.java)
        getPositionByIdUseCase = mock(GetPositionByIdUseCase::class.java)
        getApplicantsUseCase = mock(GetApplicantsUseCase::class.java)
        getApplicantByIdUseCase = mock(GetApplicantByIdUseCase::class.java)
        updateApplicantStatusUseCase = mock(UpdateApplicantStatusUseCase::class.java)
        updateApplicantUseCase = mock(UpdateApplicantUseCase::class.java)
        securityUtils = mock(SecurityUtils::class.java)
        logger = mock(LoggerPort::class.java)

        controller = ApplicantsController(
            submitApplicationUseCase = submitApplicationUseCase,
            getPublicPositionsUseCase = getPublicPositionsUseCase,
            getPositionByIdUseCase = getPositionByIdUseCase,
            getApplicantsUseCase = getApplicantsUseCase,
            getApplicantByIdUseCase = getApplicantByIdUseCase,
            updateApplicantStatusUseCase = updateApplicantStatusUseCase,
            updateApplicantUseCase = updateApplicantUseCase,
            securityUtils = securityUtils,
            responseFactory = responseFactory,
            logger = logger,
            objectMapper = objectMapper
        )
    }

    @Test
    fun `getApplicantById returns 200 with applicant data on success`() = runBlocking {
        whenever(securityUtils.getCurrentUser()).thenReturn(adminUser)
        whenever(getApplicantByIdUseCase.execute(any<GetApplicantByIdCommand>())).thenReturn(applicantItem)

        val response = controller.getApplicantById("app-1")

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("success", response.body?.status)
        assertEquals("Applicant retrieved successfully", response.body?.message)
    }

    @Test
    fun `getApplicantById maps applicant fields to response`() = runBlocking {
        whenever(securityUtils.getCurrentUser()).thenReturn(adminUser)
        whenever(getApplicantByIdUseCase.execute(any<GetApplicantByIdCommand>())).thenReturn(applicantItem)

        val response = controller.getApplicantById("app-1")

        val data = response.body?.data
        assertNotNull(data)
        assertEquals("app-1", data!!.id)
        assertEquals("Jane Doe", data.name)
        assertEquals("jane@example.com", data.email)
        assertEquals("PENDING", data.status)
        assertEquals("https://s3.example.com/cv.pdf", data.fileUrl)
    }

    @Test
    fun `getApplicantById returns 404 when applicant not found`() = runBlocking {
        whenever(securityUtils.getCurrentUser()).thenReturn(adminUser)
        whenever(getApplicantByIdUseCase.execute(any<GetApplicantByIdCommand>())).thenReturn(null)

        val response = controller.getApplicantById("missing-id")

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertEquals("error", response.body?.status)
        assertEquals("Applicant not found", response.body?.message)
    }

    @Test
    fun `getApplicantById returns 500 on unexpected exception`() = runBlocking {
        whenever(securityUtils.getCurrentUser()).thenReturn(adminUser)
        whenever(getApplicantByIdUseCase.execute(any<GetApplicantByIdCommand>())).thenThrow(RuntimeException("DB error"))

        val response = controller.getApplicantById("app-1")

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals("error", response.body?.status)
    }
}