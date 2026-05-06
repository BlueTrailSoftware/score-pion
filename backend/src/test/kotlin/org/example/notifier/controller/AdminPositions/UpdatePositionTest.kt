package org.example.notifier.controller.AdminPositions

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import org.example.notifier.application.model.position.PositionResult
import org.example.notifier.application.useCases.createPosition.CreatePositionUseCase
import org.example.notifier.application.useCases.getAllPositions.GetAllPositionsUseCase
import org.example.notifier.application.useCases.getAvailableAssessments.GetAvailableAssessmentsUseCase
import org.example.notifier.application.useCases.getPositionById.GetPositionByIdUseCase
import org.example.notifier.application.useCases.updatePosition.UpdatePositionUseCase
import org.example.notifier.application.useCases.updatePositionActiveStatus.UpdatePositionActiveStatusUseCase
import org.example.notifier.infrastructure.controller.AdminPositionsController
import org.example.notifier.infrastructure.dto.request.UpdatePositionRequest
import org.example.notifier.infrastructure.logging.LoggerPort
import org.example.notifier.infrastructure.security.SecurityUtils
import org.example.notifier.infrastructure.util.factory.ResponseEntityFactory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import java.time.LocalDateTime
import java.util.UUID

class AdminPositionsControllerUpdatePositionTest {

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

    private val updateRequest = UpdatePositionRequest(
        title = "Updated Title",
        description = "Updated description",
        external = true,
        assessmentIds = listOf("a-3"),
        deleteFile = false,
        workMode = "Onsite",
        location = "New York"
    )

    private val positionResult = PositionResult(
        id = positionId,
        title = "Updated Title",
        description = "Updated description",
        external = true,
        assessments = emptyList(),
        fileUrl = null,
        createdBy = "admin@example.com",
        isActive = true,
        createdAt = now,
        updatedAt = now,
        workMode = "Onsite",
        location = "New York",
        jobType = null,
        experienceMin = null,
        experienceMax = null,
        skills = emptyList()
    )

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
    fun `updatePosition should return 200 with success status`() = runBlocking {
        whenever(objectMapper.readValue(any<String>(), eq(UpdatePositionRequest::class.java)))
            .thenReturn(updateRequest)
        whenever(updatePositionUseCase.execute(any())).thenReturn(positionResult)

        val response = controller.updatePosition(id = positionId, requestJson = "{}", filePart = null)

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertEquals("success", response.body?.status)
        Assertions.assertEquals("Position updated successfully", response.body?.message)
    }

    @Test
    fun `updatePosition should pass correct command fields to use case`() = runBlocking<Unit> {
        whenever(objectMapper.readValue(any<String>(), eq(UpdatePositionRequest::class.java)))
            .thenReturn(updateRequest)
        whenever(updatePositionUseCase.execute(any())).thenReturn(positionResult)

        controller.updatePosition(id = positionId, requestJson = "{}", filePart = null)

        verify(updatePositionUseCase).execute(argThat {
            this.positionId == positionId &&
            title == "Updated Title" &&
            description == "Updated description" &&
            external == true &&
            assessmentIds == listOf("a-3") &&
            filePart == null &&
            deleteFile == false &&
            workMode == "Onsite" &&
            location == "New York"
        })
    }

    @Test
    fun `updatePosition should return 404 on IllegalArgumentException`() = runBlocking {
        whenever(objectMapper.readValue(any<String>(), eq(UpdatePositionRequest::class.java)))
            .thenReturn(updateRequest)
        whenever(updatePositionUseCase.execute(any()))
            .thenThrow(IllegalArgumentException("Position not found"))

        val response = controller.updatePosition(id = positionId, requestJson = "{}", filePart = null)

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        Assertions.assertEquals("error", response.body?.status)
        Assertions.assertEquals("Position not found", response.body?.message)
    }

    @Test
    fun `updatePosition should return error for invalid workMode`() = runBlocking {
        val invalidRequest = updateRequest.copy(workMode = "InOffice")
        whenever(objectMapper.readValue(any<String>(), eq(UpdatePositionRequest::class.java)))
            .thenReturn(invalidRequest)
        whenever(updatePositionUseCase.execute(any()))
            .thenThrow(IllegalArgumentException("Invalid workMode: 'InOffice'. Must be one of: Onsite, Remote, Hybrid"))

        val response = controller.updatePosition(id = positionId, requestJson = "{}", filePart = null)

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        Assertions.assertEquals("error", response.body?.status)
    }

    @Test
    fun `updatePosition should return 500 on unexpected exception`() = runBlocking {
        whenever(objectMapper.readValue(any<String>(), eq(UpdatePositionRequest::class.java)))
            .thenReturn(updateRequest)
        whenever(updatePositionUseCase.execute(any()))
            .thenThrow(RuntimeException("DynamoDB unavailable"))

        val response = controller.updatePosition(id = positionId, requestJson = "{}", filePart = null)

        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        Assertions.assertEquals("error", response.body?.status)
    }
}