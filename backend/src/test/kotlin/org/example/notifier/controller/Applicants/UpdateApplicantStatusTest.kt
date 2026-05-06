package org.example.notifier.controller.Applicants

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import org.example.notifier.application.model.applicant.ApplicantItem
import org.example.notifier.application.useCases.getApplicantById.GetApplicantByIdUseCase
import org.example.notifier.application.useCases.getApplicants.GetApplicantsUseCase
import org.example.notifier.application.useCases.getPositionById.GetPositionByIdUseCase
import org.example.notifier.application.useCases.getPublicPositions.GetPublicPositionsUseCase
import org.example.notifier.application.useCases.submitApplication.SubmitApplicationUseCase
import org.example.notifier.application.useCases.updateApplicant.UpdateApplicantUseCase
import org.example.notifier.application.useCases.updateApplicantStatus.UpdateApplicantStatusCommand
import org.example.notifier.application.useCases.updateApplicantStatus.UpdateApplicantStatusUseCase
import org.example.notifier.domain.user.User
import org.example.notifier.domain.user.UserRole
import org.example.notifier.infrastructure.controller.ApplicantsController
import org.example.notifier.infrastructure.dto.request.UpdateApplicantStatusRequest
import org.example.notifier.infrastructure.logging.LoggerPort
import org.example.notifier.infrastructure.security.SecurityUtils
import org.example.notifier.infrastructure.util.factory.ResponseEntityFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

class UpdateApplicantStatusTest {

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
        phone = null,
        positionId = "pos-1",
        positionTitle = "Backend Engineer",
        status = "APPROVED",
        source = "self_application",
        createdAt = now,
        updatedAt = now,
        reviewedBy = "admin-1",
        reviewedAt = now,
        fileUrl = null,
        linkedinUrl = null,
        isFileDeleted = false,
        statusNote = "Great candidate",
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
    fun `updateApplicantStatus returns 200 with updated applicant data on success`() = runBlocking {
        whenever(securityUtils.getCurrentUser()).thenReturn(adminUser)
        whenever(updateApplicantStatusUseCase.execute(any<UpdateApplicantStatusCommand>())).thenReturn(applicantItem)

        val response = controller.updateApplicantStatus("app-1", UpdateApplicantStatusRequest("APPROVED"))

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("success", response.body?.status)
        assertEquals("Applicant status updated successfully", response.body?.message)
    }

    @Test
    fun `updateApplicantStatus maps updated applicant fields to response`() = runBlocking {
        whenever(securityUtils.getCurrentUser()).thenReturn(adminUser)
        whenever(updateApplicantStatusUseCase.execute(any<UpdateApplicantStatusCommand>())).thenReturn(applicantItem)

        val response = controller.updateApplicantStatus("app-1", UpdateApplicantStatusRequest("APPROVED", "Great candidate"))

        val data = response.body?.data
        assertNotNull(data)
        assertEquals("app-1", data!!.id)
        assertEquals("APPROVED", data.status)
        assertEquals("admin-1", data.reviewedBy)
    }

    @Test
    fun `updateApplicantStatus passes correct command to use case`() = runBlocking<Unit> {
        whenever(securityUtils.getCurrentUser()).thenReturn(adminUser)
        whenever(updateApplicantStatusUseCase.execute(any<UpdateApplicantStatusCommand>())).thenReturn(applicantItem)

        controller.updateApplicantStatus("app-1", UpdateApplicantStatusRequest("REJECTED", "Not a fit"))

        verify(updateApplicantStatusUseCase).execute(argThat { cmd ->
            cmd.id == "app-1" &&
            cmd.newStatus == "REJECTED" &&
            cmd.reviewedBy.id == "admin-1" &&
            cmd.statusNote == "Not a fit"
        })
    }

    @Test
    fun `updateApplicantStatus passes null statusNote when not provided`() = runBlocking<Unit> {
        whenever(securityUtils.getCurrentUser()).thenReturn(adminUser)
        whenever(updateApplicantStatusUseCase.execute(any<UpdateApplicantStatusCommand>())).thenReturn(applicantItem)

        controller.updateApplicantStatus("app-1", UpdateApplicantStatusRequest("APPROVED"))

        verify(updateApplicantStatusUseCase).execute(argThat { cmd -> cmd.statusNote == null })
    }

    @Test
    fun `updateApplicantStatus returns 400 on IllegalArgumentException`() = runBlocking {
        whenever(securityUtils.getCurrentUser()).thenReturn(adminUser)
        whenever(updateApplicantStatusUseCase.execute(any<UpdateApplicantStatusCommand>()))
            .thenThrow(IllegalArgumentException("Invalid status value: UNKNOWN"))

        val response = controller.updateApplicantStatus("app-1", UpdateApplicantStatusRequest("UNKNOWN"))

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("error", response.body?.status)
        assertEquals("Invalid status value: UNKNOWN", response.body?.message)
    }

    @Test
    fun `updateApplicantStatus returns 500 on unexpected exception`() = runBlocking {
        whenever(securityUtils.getCurrentUser()).thenReturn(adminUser)
        whenever(updateApplicantStatusUseCase.execute(any<UpdateApplicantStatusCommand>()))
            .thenThrow(RuntimeException("DB error"))

        val response = controller.updateApplicantStatus("app-1", UpdateApplicantStatusRequest("APPROVED"))

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals("error", response.body?.status)
    }
}