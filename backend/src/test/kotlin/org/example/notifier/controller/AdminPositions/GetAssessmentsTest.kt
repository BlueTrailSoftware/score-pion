package org.example.notifier.controller.AdminPositions

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import org.example.notifier.application.useCases.createPosition.CreatePositionUseCase
import org.example.notifier.application.useCases.getAllPositions.GetAllPositionsUseCase
import org.example.notifier.application.model.assessment.AssessmentItem
import org.example.notifier.application.useCases.getAvailableAssessments.GetAvailableAssessmentsUseCase
import org.example.notifier.application.useCases.getPositionById.GetPositionByIdUseCase
import org.example.notifier.application.useCases.updatePosition.UpdatePositionUseCase
import org.example.notifier.application.useCases.updatePositionActiveStatus.UpdatePositionActiveStatusUseCase
import org.example.notifier.infrastructure.controller.AdminPositionsController
import org.example.notifier.infrastructure.logging.LoggerPort
import org.example.notifier.infrastructure.security.SecurityUtils
import org.example.notifier.infrastructure.util.factory.ResponseEntityFactory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus

class AdminPositionsControllerGetAssessmentsTest {

    private lateinit var createPositionUseCase: CreatePositionUseCase
    private lateinit var getAllPositionsUseCase: GetAllPositionsUseCase
    private lateinit var getPositionByIdUseCase: GetPositionByIdUseCase
    private lateinit var updatePositionUseCase: UpdatePositionUseCase
    private lateinit var updatePositionActiveStatusUseCase: UpdatePositionActiveStatusUseCase
    private lateinit var getAvailableAssessmentsUseCase: GetAvailableAssessmentsUseCase
    private lateinit var securityUtils: SecurityUtils
    private lateinit var logger: LoggerPort
    private lateinit var objectMapper: ObjectMapper
    private lateinit var controller: AdminPositionsController

    private val responseFactory = ResponseEntityFactory()

    @BeforeEach
    fun setup() {
        createPositionUseCase = Mockito.mock(CreatePositionUseCase::class.java)
        getAllPositionsUseCase = Mockito.mock(GetAllPositionsUseCase::class.java)
        getPositionByIdUseCase = Mockito.mock(GetPositionByIdUseCase::class.java)
        updatePositionUseCase = Mockito.mock(UpdatePositionUseCase::class.java)
        updatePositionActiveStatusUseCase = Mockito.mock(UpdatePositionActiveStatusUseCase::class.java)
        getAvailableAssessmentsUseCase = Mockito.mock(GetAvailableAssessmentsUseCase::class.java)
        securityUtils = Mockito.mock(SecurityUtils::class.java)
        logger = Mockito.mock(LoggerPort::class.java)
        objectMapper = Mockito.mock(ObjectMapper::class.java)

        controller = AdminPositionsController(
            createPositionUseCase = createPositionUseCase,
            getAllPositionsUseCase = getAllPositionsUseCase,
            getPositionByIdUseCase = getPositionByIdUseCase,
            updatePositionUseCase = updatePositionUseCase,
            updatePositionActiveStatusUseCase = updatePositionActiveStatusUseCase,
            getAvailableAssessmentsUseCase = getAvailableAssessmentsUseCase,
            securityUtils = securityUtils,
            responseFactory = responseFactory,
            logger = logger,
            objectMapper = objectMapper,
        )
    }

    @Test
    fun `getAssessments should return 200 with success status`() = runBlocking {
        whenever(getAvailableAssessmentsUseCase.execute()).thenReturn(emptyList())

        val response = controller.getAssessments()

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertEquals("success", response.body?.status)
        Assertions.assertEquals("Assessments retrieved successfully", response.body?.message)
    }

    @Test
    fun `getAssessments should map AssessmentItem fields to response`() = runBlocking {
        val items = listOf(
            AssessmentItem(displayName = "Java Test", testId = "java-101"),
            AssessmentItem(displayName = "Kotlin Test", testId = "kotlin-202")
        )
        whenever(getAvailableAssessmentsUseCase.execute()).thenReturn(items)

        val response = controller.getAssessments()

        val data = response.body?.data
        Assertions.assertNotNull(data)
        Assertions.assertEquals(2, data!!.size)
        Assertions.assertEquals("Java Test", data[0].displayName)
        Assertions.assertEquals("java-101", data[0].testID)
        Assertions.assertEquals("Kotlin Test", data[1].displayName)
        Assertions.assertEquals("kotlin-202", data[1].testID)
    }

    @Test
    fun `getAssessments should return 500 on IllegalStateException`() = runBlocking {
        whenever(getAvailableAssessmentsUseCase.execute())
            .thenThrow(IllegalStateException("Invalid response from Coderbyte"))

        val response = controller.getAssessments()

        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        Assertions.assertEquals("error", response.body?.status)
        Assertions.assertTrue(response.body?.message?.contains("Invalid response from assessment service") == true)
    }

    @Test
    fun `getAssessments should return 500 on unexpected exception`() = runBlocking {
        whenever(getAvailableAssessmentsUseCase.execute())
            .thenThrow(RuntimeException("Network timeout"))

        val response = controller.getAssessments()

        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        Assertions.assertEquals("error", response.body?.status)
    }
}