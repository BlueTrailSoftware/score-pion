package org.example.notifier.controller.AdminPositions

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import org.example.notifier.application.model.position.PositionAssessmentItem
import org.example.notifier.application.model.position.PositionResult
import org.example.notifier.application.useCases.createPosition.CreatePositionUseCase
import org.example.notifier.application.useCases.getAllPositions.GetAllPositionsUseCase
import org.example.notifier.application.useCases.getAvailableAssessments.GetAvailableAssessmentsUseCase
import org.example.notifier.application.useCases.getPositionById.GetPositionByIdCommand
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
import java.time.LocalDateTime
import java.util.UUID

class AdminPositionsControllerGetPositionByIdTest {

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
    private val now = LocalDateTime.now()
    private val positionId = UUID.randomUUID().toString()

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

    private fun buildPositionResult(
        id: String = positionId,
        title: String = "Backend Engineer",
        description: String = "Backend role",
        external: Boolean = false,
        assessments: List<PositionAssessmentItem> = emptyList(),
        isActive: Boolean = true
    ) = PositionResult(
        id = id,
        title = title,
        description = description,
        external = external,
        assessments = assessments,
        fileUrl = null,
        createdBy = "admin@example.com",
        isActive = isActive,
        createdAt = now,
        updatedAt = now,
        workMode = "Onsite",
        location = ""
    )

    @Test
    fun `getPositionById should return 200 with success status`() = runBlocking {
        whenever(getPositionByIdUseCase.execute(GetPositionByIdCommand(positionId = positionId)))
            .thenReturn(buildPositionResult())

        val response = controller.getPositionById(id = positionId)

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertEquals("success", response.body?.status)
        Assertions.assertEquals("Position retrieved successfully", response.body?.message)
    }

    @Test
    fun `getPositionById should map PositionResult fields to response`() = runBlocking {
        val assessments = listOf(
            PositionAssessmentItem("a-1", "Java Test", now),
            PositionAssessmentItem("a-2", "Kotlin Test", now)
        )
        whenever(getPositionByIdUseCase.execute(GetPositionByIdCommand(positionId = positionId)))
            .thenReturn(buildPositionResult(assessments = assessments))

        val response = controller.getPositionById(id = positionId)

        val data = response.body?.data
        Assertions.assertNotNull(data)
        Assertions.assertEquals(positionId, data!!.id)
        Assertions.assertEquals("Backend Engineer", data.title)
        Assertions.assertEquals("Backend role", data.description)
        Assertions.assertEquals(false, data.external)
        Assertions.assertEquals(2, data.assessments.size)
    }

    @Test
    fun `getPositionById should return 404 when position not found`() = runBlocking {
        whenever(getPositionByIdUseCase.execute(GetPositionByIdCommand(positionId = positionId)))
            .thenReturn(null)

        val response = controller.getPositionById(id = positionId)

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        Assertions.assertEquals("error", response.body?.status)
        Assertions.assertEquals("Position not found", response.body?.message)
    }

    @Test
    fun `getPositionById should return 500 on exception`() = runBlocking {
        whenever(getPositionByIdUseCase.execute(GetPositionByIdCommand(positionId = positionId)))
            .thenThrow(RuntimeException("DynamoDB unavailable"))

        val response = controller.getPositionById(id = positionId)

        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        Assertions.assertEquals("error", response.body?.status)
    }
}