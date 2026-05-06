package org.example.notifier.controller.Applicants

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import org.example.notifier.application.model.applicant.ApplicantItem
import org.example.notifier.application.useCases.getApplicantById.GetApplicantByIdUseCase
import org.example.notifier.application.useCases.getApplicants.GetApplicantsUseCase
import org.example.notifier.application.useCases.getPositionById.GetPositionByIdUseCase
import org.example.notifier.application.useCases.getPublicPositions.GetPublicPositionsUseCase
import org.example.notifier.application.useCases.submitApplication.SubmitApplicationCommand
import org.example.notifier.application.useCases.submitApplication.SubmitApplicationUseCase
import org.example.notifier.application.useCases.updateApplicant.UpdateApplicantUseCase
import org.example.notifier.application.useCases.updateApplicantStatus.UpdateApplicantStatusUseCase
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

class SubmitApplicationTest {

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
    private val objectMapper = ObjectMapper().apply {
        findAndRegisterModules()
    }
    private val now = LocalDateTime.now()

    private val applicantItem = ApplicantItem(
        id = "app-1",
        name = "Jane Doe",
        email = "jane@example.com",
        phone = "123456789",
        positionId = "pos-1",
        positionTitle = null,
        status = "PENDING",
        source = "self_application",
        createdAt = now,
        updatedAt = now,
        reviewedBy = null,
        reviewedAt = null,
        fileUrl = null,
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
    fun `applyToPosition returns 200 on success`() = runBlocking {
        val requestJson = buildRequestJson()
        whenever(submitApplicationUseCase.execute(any<SubmitApplicationCommand>())).thenReturn(applicantItem)

        val response = controller.applyToPosition(requestJson, filePart = null)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("success", response.body?.status)
        assertEquals("Application submitted successfully", response.body?.message)
    }

    @Test
    fun `applyToPosition maps ApplicantItem fields to response`() = runBlocking {
        val requestJson = buildRequestJson()
        whenever(submitApplicationUseCase.execute(any<SubmitApplicationCommand>())).thenReturn(applicantItem)

        val response = controller.applyToPosition(requestJson, filePart = null)

        val data = response.body?.data
        assertNotNull(data)
        assertEquals("app-1", data!!.id)
        assertEquals("jane@example.com", data.email)
        assertEquals("Jane Doe", data.name)
        assertEquals("PENDING", data.status)
    }

    @Test
    fun `applyToPosition returns 400 on IllegalArgumentException`() = runBlocking {
        val requestJson = buildRequestJson()
        whenever(submitApplicationUseCase.execute(any<SubmitApplicationCommand>()))
            .thenThrow(IllegalArgumentException("Security validation failed. Please try again."))

        val response = controller.applyToPosition(requestJson, filePart = null)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("error", response.body?.status)
        assertEquals("Security validation failed. Please try again.", response.body?.message)
    }

    @Test
    fun `applyToPosition returns 400 with message on business rule violation`() = runBlocking {
        val requestJson = buildRequestJson()
        whenever(submitApplicationUseCase.execute(any<SubmitApplicationCommand>()))
            .thenThrow(IllegalArgumentException("Please provide either a CV file or a LinkedIn profile URL"))

        val response = controller.applyToPosition(requestJson, filePart = null)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("Please provide either a CV file or a LinkedIn profile URL", response.body?.message)
    }

    @Test
    fun `applyToPosition returns 500 on unexpected exception`() = runBlocking {
        val requestJson = buildRequestJson()
        whenever(submitApplicationUseCase.execute(any<SubmitApplicationCommand>()))
            .thenThrow(RuntimeException("S3 unavailable"))

        val response = controller.applyToPosition(requestJson, filePart = null)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals("error", response.body?.status)
    }

    private fun buildRequestJson(
        name: String = "Jane Doe",
        email: String = "jane@example.com",
        positionId: String = "pos-1",
        linkedinUrl: String = "https://linkedin.com/in/jane",
        captchaToken: String = "valid-token"
    ) = objectMapper.writeValueAsString(
        mapOf(
            "name" to name,
            "email" to email,
            "phone" to "123456789",
            "positionId" to positionId,
            "linkedinUrl" to linkedinUrl,
            "gdprConsent" to true,
            "captchaToken" to captchaToken
        )
    )
}