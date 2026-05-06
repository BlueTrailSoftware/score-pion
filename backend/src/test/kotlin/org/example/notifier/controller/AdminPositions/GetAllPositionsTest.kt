package org.example.notifier.controller.AdminPositions

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import org.example.notifier.application.model.position.PositionSummaryItem
import org.example.notifier.application.model.shared.PagedResult
import org.example.notifier.application.useCases.createPosition.CreatePositionUseCase
import org.example.notifier.application.useCases.getAllPositions.GetAllPositionsCommand
import org.example.notifier.application.useCases.getAllPositions.GetAllPositionsUseCase
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
import java.time.LocalDateTime

class AdminPositionsControllerGetAllPositionsTest {

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
    fun `getAllPositions should return 200 with success status`() = runBlocking {
        whenever(getAllPositionsUseCase.execute(GetAllPositionsCommand(activeOnly = false)))
            .thenReturn(PagedResult(emptyList(), 0))

        val response = controller.getAllPositions(activeOnly = false)

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertEquals("success", response.body?.status)
        Assertions.assertEquals("Positions retrieved successfully", response.body?.message)
    }

    @Test
    fun `getAllPositions should map PositionSummaryItem fields to response`() = runBlocking {
        val item = PositionSummaryItem(
            id = "pos-1",
            title = "Backend Engineer",
            description = "Backend role",
            external = false,
            assessmentsCount = 3,
            isActive = true,
            createdAt = now
        )
        whenever(getAllPositionsUseCase.execute(GetAllPositionsCommand(activeOnly = false)))
            .thenReturn(PagedResult(listOf(item), 1))

        val response = controller.getAllPositions(activeOnly = false)

        val data = response.body?.data
        Assertions.assertNotNull(data)
        Assertions.assertEquals(1, data!!.items.size)
        Assertions.assertEquals(1, data.total)
        with(data.items[0]) {
            Assertions.assertEquals("pos-1", id)
            Assertions.assertEquals("Backend Engineer", title)
            Assertions.assertEquals("Backend role", description)
            Assertions.assertEquals(false, external)
            Assertions.assertEquals(3, assessmentsCount)
            Assertions.assertEquals(true, isActive)
        }
    }

    @Test
    fun `getAllPositions should forward activeOnly flag to use case`() = runBlocking {
        whenever(getAllPositionsUseCase.execute(GetAllPositionsCommand(activeOnly = true)))
            .thenReturn(PagedResult(emptyList(), 0))

        val response = controller.getAllPositions(activeOnly = true)

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertEquals(0, response.body?.data?.items?.size)
    }

    @Test
    fun `getAllPositions should return all items from use case`() = runBlocking {
        val items = listOf(
            PositionSummaryItem("p-1", "Position A", "Desc A", false, 1, true, now),
            PositionSummaryItem("p-2", "Position B", "Desc B", true, 0, false, now),
            PositionSummaryItem("p-3", "Position C", "Desc C", false, 2, true, now)
        )
        whenever(getAllPositionsUseCase.execute(GetAllPositionsCommand(activeOnly = false)))
            .thenReturn(PagedResult(items, 3))

        val response = controller.getAllPositions(activeOnly = false)

        Assertions.assertEquals(3, response.body?.data?.items?.size)
        Assertions.assertEquals(3, response.body?.data?.total)
    }

    @Test
    fun `getAllPositions should return 500 on exception`() = runBlocking {
        whenever(getAllPositionsUseCase.execute(GetAllPositionsCommand(activeOnly = false)))
            .thenThrow(RuntimeException("DynamoDB unavailable"))

        val response = controller.getAllPositions(activeOnly = false)

        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        Assertions.assertEquals("error", response.body?.status)
    }
}
