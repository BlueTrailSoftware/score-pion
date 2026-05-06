package org.example.notifier.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import kotlinx.coroutines.runBlocking
import org.example.notifier.application.model.applicant.ApplicantItem
import org.example.notifier.application.useCases.getApplicantById.GetApplicantByIdUseCase
import org.example.notifier.application.useCases.getApplicants.GetApplicantsUseCase
import org.example.notifier.application.useCases.getPositionById.GetPositionByIdUseCase
import org.example.notifier.application.useCases.getPublicPositions.GetPublicPositionsUseCase
import org.example.notifier.application.useCases.submitApplication.SubmitApplicationUseCase
import org.example.notifier.application.useCases.updateApplicant.UpdateApplicantCommand
import org.example.notifier.application.useCases.updateApplicant.UpdateApplicantUseCase
import org.example.notifier.application.useCases.updateApplicantStatus.UpdateApplicantStatusUseCase
import org.example.notifier.domain.applicant.ApplicantStatus
import org.example.notifier.domain.user.User
import org.example.notifier.domain.user.UserRole
import org.example.notifier.infrastructure.controller.ApplicantsController
import org.example.notifier.infrastructure.dto.request.UpdateApplicantRequest
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

class ApplicantsControllerUpdateApplicantTest {

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
    private val objectMapper = ObjectMapper().registerModule(JavaTimeModule())

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
        positionTitle = "Designer",
        status = ApplicantStatus.PENDING.name,
        source = "self_application",
        createdAt = now,
        updatedAt = now,
        reviewedBy = null,
        reviewedAt = null,
        fileUrl = null,
        linkedinUrl = null,
        isFileDeleted = false,
        statusNote = null,
        assessments = null
    )

    @BeforeEach
    fun setUp() {
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
    fun `updateApplicant returns 200 with applicant data on success`() = runBlocking {
        val requestJson = objectMapper.writeValueAsString(
            UpdateApplicantRequest(name = "Jane Doe", email = "jane@example.com")
        )
        whenever(securityUtils.getCurrentUser()).thenReturn(adminUser)
        whenever(updateApplicantUseCase.execute(any())).thenReturn(applicantItem)

        val response = controller.updateApplicant("app-1", requestJson, null)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("success", response.body?.status)
        assertEquals("Applicant updated successfully", response.body?.message)
        assertNotNull(response.body?.data)
        assertEquals("app-1", response.body?.data?.id)
        assertEquals("Jane Doe", response.body?.data?.name)
    }

    @Test
    fun `updateApplicant passes correct command to use case`() = runBlocking<Unit> {
        val requestJson = objectMapper.writeValueAsString(
            UpdateApplicantRequest(name = "New Name", email = "new@example.com", phone = "+1", deleteFile = true)
        )
        whenever(securityUtils.getCurrentUser()).thenReturn(adminUser)
        whenever(updateApplicantUseCase.execute(any())).thenReturn(applicantItem)

        controller.updateApplicant("app-1", requestJson, null)

        verify(updateApplicantUseCase).execute(argThat { command ->
            command.id == "app-1" &&
            command.name == "New Name" &&
            command.email == "new@example.com" &&
            command.phone == "+1" &&
            command.deleteFile &&
            command.filePart == null
        })
    }

    @Test
    fun `updateApplicant returns 500 when use case throws`() = runBlocking {
        val requestJson = objectMapper.writeValueAsString(UpdateApplicantRequest())
        whenever(securityUtils.getCurrentUser()).thenReturn(adminUser)
        whenever(updateApplicantUseCase.execute(any()))
            .thenThrow(IllegalArgumentException("Applicant not found with id: app-1"))

        val response = controller.updateApplicant("app-1", requestJson, null)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals("error", response.body?.status)
        assertEquals("Failed to update applicant", response.body?.message)
    }

    @Test
    fun `updateApplicant returns 500 on unexpected exception`() = runBlocking {
        val requestJson = objectMapper.writeValueAsString(UpdateApplicantRequest())
        whenever(securityUtils.getCurrentUser()).thenReturn(adminUser)
        whenever(updateApplicantUseCase.execute(any())).thenThrow(RuntimeException("DB down"))

        val response = controller.updateApplicant("app-1", requestJson, null)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals("error", response.body?.status)
    }
}