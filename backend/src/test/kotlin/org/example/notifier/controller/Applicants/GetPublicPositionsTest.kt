package org.example.notifier.controller.Applicants

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import org.example.notifier.application.model.position.PublicPositionItem
import org.example.notifier.application.useCases.getApplicantById.GetApplicantByIdUseCase
import org.example.notifier.application.useCases.getApplicants.GetApplicantsUseCase
import org.example.notifier.application.useCases.getPositionById.GetPositionByIdUseCase
import org.example.notifier.application.useCases.getPublicPositions.GetPublicPositionsUseCase
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
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

class GetPublicPositionsTest {

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
    fun `getAvailablePositions returns 200 with empty list`() = runBlocking {
        whenever(getPublicPositionsUseCase.execute()).thenReturn(emptyList())

        val response = controller.getAvailablePositions()

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("success", response.body?.status)
        assertEquals("Available positions retrieved successfully", response.body?.message)
        assertEquals(emptyList<Any>(), response.body?.data)
    }

    @Test
    fun `getAvailablePositions maps PublicPositionItem fields to response`() = runBlocking {
        val item = PublicPositionItem(
            id = "pos-1",
            title = "Backend Engineer",
            description = "Kotlin job",
            fileUrl = "https://s3.example.com/jd.pdf",
            createdAt = now
        )
        whenever(getPublicPositionsUseCase.execute()).thenReturn(listOf(item))

        val response = controller.getAvailablePositions()

        val data = response.body?.data
        assertNotNull(data)
        assertEquals(1, data!!.size)
        with(data[0]) {
            assertEquals("pos-1", id)
            assertEquals("Backend Engineer", title)
            assertEquals("Kotlin job", description)
            assertEquals("https://s3.example.com/jd.pdf", fileUrl)
            assertEquals(now, createdAt)
        }
    }

    @Test
    fun `getAvailablePositions returns all items from use case`() = runBlocking {
        val items = listOf(
            PublicPositionItem("p-1", "Position 1", "Desc 1", null, now),
            PublicPositionItem("p-2", "Position 2", "Desc 2", null, now.minusDays(1))
        )
        whenever(getPublicPositionsUseCase.execute()).thenReturn(items)

        val response = controller.getAvailablePositions()

        assertEquals(2, response.body?.data?.size)
    }

    @Test
    fun `getAvailablePositions returns 500 on exception`() = runBlocking {
        whenever(getPublicPositionsUseCase.execute()).thenThrow(RuntimeException("DB error"))

        val response = controller.getAvailablePositions()

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals("error", response.body?.status)
    }
}