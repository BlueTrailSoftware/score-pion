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
import org.example.notifier.domain.user.User
import org.example.notifier.domain.user.UserRole
import org.example.notifier.infrastructure.controller.AdminPositionsController
import org.example.notifier.infrastructure.dto.request.CreatePositionRequest
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

class AdminPositionsControllerCreatePositionTest {

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

    private val currentAdmin = User(
        id = "admin-1",
        email = "admin@example.com",
        name = "Admin Name",
        role = UserRole.ADMIN,
        isActive = true,
        createdAt = now,
        updatedAt = now
    )

    private val createRequest = CreatePositionRequest(
        title = "Backend Engineer",
        description = "Backend role",
        external = false,
        assessmentIds = listOf("a-1", "a-2"),
        workMode = "Onsite",
        location = "Berlin"
    )

    private val positionResult = PositionResult(
        id = "pos-1",
        title = "Backend Engineer",
        description = "Backend role",
        external = false,
        assessments = emptyList(),
        fileUrl = null,
        createdBy = "admin@example.com",
        isActive = true,
        createdAt = now,
        updatedAt = now,
        workMode = "Onsite",
        location = "Berlin",
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
    fun `createPosition should return 200 with success status`() = runBlocking {
        whenever(objectMapper.readValue(any<String>(), eq(CreatePositionRequest::class.java)))
            .thenReturn(createRequest)
        whenever(securityUtils.getCurrentUser()).thenReturn(currentAdmin)
        whenever(createPositionUseCase.execute(any())).thenReturn(positionResult)

        val response = controller.createPosition(requestJson = "{}", filePart = null)

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertEquals("success", response.body?.status)
        Assertions.assertEquals("Position created successfully", response.body?.message)
    }

    @Test
    fun `createPosition should pass correct command fields to use case`() = runBlocking<Unit> {
        whenever(objectMapper.readValue(any<String>(), eq(CreatePositionRequest::class.java)))
            .thenReturn(createRequest)
        whenever(securityUtils.getCurrentUser()).thenReturn(currentAdmin)
        whenever(createPositionUseCase.execute(any())).thenReturn(positionResult)

        controller.createPosition(requestJson = "{}", filePart = null)

        verify(createPositionUseCase).execute(argThat {
            title == "Backend Engineer" &&
            description == "Backend role" &&
            external == false &&
            assessmentIds == listOf("a-1", "a-2") &&
            createdByEmail == currentAdmin.email &&
            filePart == null &&
            workMode == "Onsite" &&
            location == "Berlin"
        })
    }

    @Test
    fun `createPosition should return 400 on IllegalArgumentException`() = runBlocking {
        whenever(objectMapper.readValue(any<String>(), eq(CreatePositionRequest::class.java)))
            .thenReturn(createRequest)
        whenever(securityUtils.getCurrentUser()).thenReturn(currentAdmin)
        whenever(createPositionUseCase.execute(any()))
            .thenThrow(IllegalArgumentException("Title already exists"))

        val response = controller.createPosition(requestJson = "{}", filePart = null)

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        Assertions.assertEquals("error", response.body?.status)
        Assertions.assertEquals("Title already exists", response.body?.message)
    }

    @Test
    fun `createPosition should return 400 for invalid workMode`() = runBlocking {
        val invalidRequest = createRequest.copy(workMode = "InOffice")
        whenever(objectMapper.readValue(any<String>(), eq(CreatePositionRequest::class.java)))
            .thenReturn(invalidRequest)
        whenever(securityUtils.getCurrentUser()).thenReturn(currentAdmin)
        whenever(createPositionUseCase.execute(any()))
            .thenThrow(IllegalArgumentException("Invalid workMode: 'InOffice'. Must be one of: Onsite, Remote, Hybrid"))

        val response = controller.createPosition(requestJson = "{}", filePart = null)

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        Assertions.assertEquals("error", response.body?.status)
    }

    @Test
    fun `createPosition should return 500 on unexpected exception`() = runBlocking {
        whenever(objectMapper.readValue(any<String>(), eq(CreatePositionRequest::class.java)))
            .thenReturn(createRequest)
        whenever(securityUtils.getCurrentUser()).thenReturn(currentAdmin)
        whenever(createPositionUseCase.execute(any()))
            .thenThrow(RuntimeException("DynamoDB unavailable"))

        val response = controller.createPosition(requestJson = "{}", filePart = null)

        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        Assertions.assertEquals("error", response.body?.status)
    }
}