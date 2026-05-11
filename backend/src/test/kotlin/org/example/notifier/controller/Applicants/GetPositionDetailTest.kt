package org.example.notifier.controller.Applicants

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import org.example.notifier.application.useCases.getApplicantById.GetApplicantByIdUseCase
import org.example.notifier.application.useCases.getApplicants.GetApplicantsUseCase
import org.example.notifier.application.useCases.getPositionById.GetPositionByIdCommand
import org.example.notifier.application.useCases.getPositionById.GetPositionByIdUseCase
import org.example.notifier.application.useCases.getPublicPositions.GetPublicPositionsUseCase
import org.example.notifier.application.useCases.submitApplication.SubmitApplicationUseCase
import org.example.notifier.application.useCases.updateApplicant.UpdateApplicantUseCase
import org.example.notifier.application.useCases.updateApplicantStatus.UpdateApplicantStatusUseCase
import org.example.notifier.application.model.position.PositionResult
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

class GetPositionDetailTest {

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

    private val activeExternalPosition = PositionResult(
        id = "pos-1",
        title = "Backend Engineer",
        description = "Build cool stuff",
        createdBy = "user-1",
        isActive = true,
        external = true,
        fileUrl = "https://s3.example.com/jd.pdf",
        assessments = emptyList(),
        createdAt = now,
        updatedAt = now,
        workMode = "Onsite",
        location = "Buenos Aires"
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
    fun `getPositionDetail returns 200 with position data on success`() = runBlocking {
        whenever(getPositionByIdUseCase.execute(any<GetPositionByIdCommand>())).thenReturn(activeExternalPosition)

        val response = controller.getPositionDetail("pos-1")

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("success", response.body?.status)
        assertEquals("Position details retrieved successfully", response.body?.message)
    }

    @Test
    fun `getPositionDetail maps position fields to response`() = runBlocking {
        whenever(getPositionByIdUseCase.execute(any<GetPositionByIdCommand>())).thenReturn(activeExternalPosition)

        val response = controller.getPositionDetail("pos-1")

        val data = response.body?.data
        assertNotNull(data)
        assertEquals("pos-1", data!!.id)
        assertEquals("Backend Engineer", data.title)
        assertEquals("Build cool stuff", data.description)
        assertEquals("https://s3.example.com/jd.pdf", data.fileUrl)
        assertEquals(now, data.createdAt)
    }

    @Test
    fun `getPositionDetail returns 404 when position not found`() = runBlocking {
        whenever(getPositionByIdUseCase.execute(any<GetPositionByIdCommand>())).thenReturn(null)

        val response = controller.getPositionDetail("missing-id")

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertEquals("error", response.body?.status)
        assertEquals("Position not available", response.body?.message)
    }

    @Test
    fun `getPositionDetail returns 404 when position is not active`() = runBlocking {
        val inactivePosition = activeExternalPosition.copy(isActive = false)
        whenever(getPositionByIdUseCase.execute(any<GetPositionByIdCommand>())).thenReturn(inactivePosition)

        val response = controller.getPositionDetail("pos-1")

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertEquals("Position not available", response.body?.message)
    }

    @Test
    fun `getPositionDetail returns 404 when position is not external`() = runBlocking {
        val internalPosition = activeExternalPosition.copy(external = false)
        whenever(getPositionByIdUseCase.execute(any<GetPositionByIdCommand>())).thenReturn(internalPosition)

        val response = controller.getPositionDetail("pos-1")

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertEquals("Position not available", response.body?.message)
    }

    @Test
    fun `getPositionDetail returns 500 on unexpected exception`() = runBlocking {
        whenever(getPositionByIdUseCase.execute(any<GetPositionByIdCommand>())).thenThrow(RuntimeException("DB error"))

        val response = controller.getPositionDetail("pos-1")

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals("error", response.body?.status)
    }
}