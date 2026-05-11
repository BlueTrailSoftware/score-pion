package org.example.notifier.controller.AdminPositions

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import org.example.notifier.application.model.position.PositionResult
import org.example.notifier.application.useCases.createPosition.CreatePositionUseCase
import org.example.notifier.application.useCases.getAllPositions.GetAllPositionsUseCase
import org.example.notifier.application.useCases.getAvailableAssessments.GetAvailableAssessmentsUseCase
import org.example.notifier.application.useCases.getPositionById.GetPositionByIdUseCase
import org.example.notifier.application.useCases.updatePosition.UpdatePositionUseCase
import org.example.notifier.application.useCases.updatePositionActiveStatus.UpdatePositionActiveStatusCommand
import org.example.notifier.application.useCases.updatePositionActiveStatus.UpdatePositionActiveStatusUseCase
import org.example.notifier.infrastructure.controller.AdminPositionsController
import org.example.notifier.infrastructure.dto.request.UpdateActiveStatusRequest
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

class AdminPositionsControllerUpdatePositionActiveStatusTest {

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

    private fun buildPositionResult(isActive: Boolean) = PositionResult(
        id = positionId,
        title = "Backend Engineer",
        description = "Backend role",
        external = false,
        assessments = emptyList(),
        fileUrl = null,
        createdBy = "admin@example.com",
        isActive = isActive,
        createdAt = now,
        updatedAt = now,
        workMode = "Onsite",
        location = ""
    )

    @Test
    fun `updatePositionActiveStatus should return 200 with activated message when isActive is true`() = runBlocking {
        whenever(
            updatePositionActiveStatusUseCase.execute(
                UpdatePositionActiveStatusCommand(positionId = positionId, isActive = true)
            )
        ).thenReturn(buildPositionResult(isActive = true))

        val response = controller.updatePositionActiveStatus(
            id = positionId,
            request = UpdateActiveStatusRequest(isActive = true)
        )

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertEquals("success", response.body?.status)
        Assertions.assertEquals("Position activated successfully", response.body?.message)
    }

    @Test
    fun `updatePositionActiveStatus should return 200 with deactivated message when isActive is false`() = runBlocking {
        whenever(
            updatePositionActiveStatusUseCase.execute(
                UpdatePositionActiveStatusCommand(positionId = positionId, isActive = false)
            )
        ).thenReturn(buildPositionResult(isActive = false))

        val response = controller.updatePositionActiveStatus(
            id = positionId,
            request = UpdateActiveStatusRequest(isActive = false)
        )

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertEquals("success", response.body?.status)
        Assertions.assertEquals("Position deactivated successfully", response.body?.message)
    }

    @Test
    fun `updatePositionActiveStatus should return updated data in body`() = runBlocking {
        whenever(
            updatePositionActiveStatusUseCase.execute(
                UpdatePositionActiveStatusCommand(positionId = positionId, isActive = true)
            )
        ).thenReturn(buildPositionResult(isActive = true))

        val response = controller.updatePositionActiveStatus(
            id = positionId,
            request = UpdateActiveStatusRequest(isActive = true)
        )

        Assertions.assertEquals(positionId, response.body?.data?.id)
        Assertions.assertEquals("Backend Engineer", response.body?.data?.title)
        Assertions.assertEquals(true, response.body?.data?.isActive)
    }

    @Test
    fun `updatePositionActiveStatus should return 404 when use case returns null`() = runBlocking {
        whenever(
            updatePositionActiveStatusUseCase.execute(
                UpdatePositionActiveStatusCommand(positionId = positionId, isActive = true)
            )
        ).thenReturn(null)

        val response = controller.updatePositionActiveStatus(
            id = positionId,
            request = UpdateActiveStatusRequest(isActive = true)
        )

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        Assertions.assertEquals("error", response.body?.status)
        Assertions.assertEquals("Position not found or already in requested state", response.body?.message)
    }

    @Test
    fun `updatePositionActiveStatus should return 500 on exception`() = runBlocking {
        whenever(
            updatePositionActiveStatusUseCase.execute(
                UpdatePositionActiveStatusCommand(positionId = positionId, isActive = true)
            )
        ).thenThrow(RuntimeException("DynamoDB unavailable"))

        val response = controller.updatePositionActiveStatus(
            id = positionId,
            request = UpdateActiveStatusRequest(isActive = true)
        )

        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        Assertions.assertEquals("error", response.body?.status)
    }
}