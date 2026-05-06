package org.example.notifier.controller.AdminRecruiters

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.model.shared.PagedResult
import org.example.notifier.application.useCases.getRecruiterDetail.GetRecruiterDetailUseCase
import org.example.notifier.application.useCases.getRecruiterPositions.GetRecruiterPositionsCommand
import org.example.notifier.application.useCases.getRecruiterPositions.GetRecruiterPositionsUseCase
import org.example.notifier.application.model.position.RecruiterPositionItem
import org.example.notifier.application.useCases.getRecruiters.GetRecruitersUseCase
import org.example.notifier.application.useCases.inviteUser.InviteUserUseCase
import org.example.notifier.application.useCases.syncRecruiterPositions.SyncRecruiterPositionsUseCase
import org.example.notifier.application.useCases.updateRecruiterActiveStatus.UpdateRecruiterActiveStatusUseCase
import org.example.notifier.infrastructure.controller.AdminRecruitersController
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

class AdminRecruitersControllerGetRecruiterPositionsTest {

    private lateinit var inviteUserUseCase: InviteUserUseCase
    private lateinit var getRecruitersUseCase: GetRecruitersUseCase
    private lateinit var getRecruiterDetailUseCase: GetRecruiterDetailUseCase
    private lateinit var getRecruiterPositionsUseCase: GetRecruiterPositionsUseCase
    private lateinit var updateRecruiterActiveStatusUseCase: UpdateRecruiterActiveStatusUseCase
    private lateinit var syncRecruiterPositionsUseCase: SyncRecruiterPositionsUseCase
    private lateinit var securityUtils: SecurityUtils
    private lateinit var logger: LoggerPort
    private lateinit var controller: AdminRecruitersController

    private val responseFactory = ResponseEntityFactory()
    private val now = LocalDateTime.now()
    private val recruiterId = UUID.randomUUID().toString()

    @BeforeEach
    fun setup() {
        inviteUserUseCase = Mockito.mock(InviteUserUseCase::class.java)
        getRecruitersUseCase = Mockito.mock(GetRecruitersUseCase::class.java)
        getRecruiterDetailUseCase = Mockito.mock(GetRecruiterDetailUseCase::class.java)
        getRecruiterPositionsUseCase = Mockito.mock(GetRecruiterPositionsUseCase::class.java)
        updateRecruiterActiveStatusUseCase = Mockito.mock(UpdateRecruiterActiveStatusUseCase::class.java)
        syncRecruiterPositionsUseCase = Mockito.mock(SyncRecruiterPositionsUseCase::class.java)
        securityUtils = Mockito.mock(SecurityUtils::class.java)
        logger = Mockito.mock(LoggerPort::class.java)

        controller = AdminRecruitersController(
            inviteUserUseCase = inviteUserUseCase,
            getRecruitersUseCase = getRecruitersUseCase,
            getRecruiterDetailUseCase = getRecruiterDetailUseCase,
            getRecruiterPositionsUseCase = getRecruiterPositionsUseCase,
            updateRecruiterActiveStatusUseCase = updateRecruiterActiveStatusUseCase,
            syncRecruiterPositionsUseCase = syncRecruiterPositionsUseCase,
            securityUtils = securityUtils,
            responseFactory = responseFactory,
            logger = logger,
        )
    }

    @Test
    fun `getRecruiterPositions should return 200 with success status`() = runBlocking {
        whenever(
            getRecruiterPositionsUseCase.execute(GetRecruiterPositionsCommand(recruiterId = recruiterId))
        ).thenReturn(PagedResult(emptyList(), 0))

        val response = controller.getRecruiterPositions(id = recruiterId)

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertEquals("success", response.body?.status)
        Assertions.assertEquals("Positions retrieved successfully", response.body?.message)
    }

    @Test
    fun `getRecruiterPositions should map RecruiterPositionItem fields to response`() = runBlocking {
        val item = RecruiterPositionItem(
            id = "p-1",
            title = "Backend Engineer",
            description = "Backend role",
            external = false,
            assessmentsCount = 2,
            isActive = true,
            createdAt = now
        )
        whenever(
            getRecruiterPositionsUseCase.execute(GetRecruiterPositionsCommand(recruiterId = recruiterId))
        ).thenReturn(PagedResult(listOf(item), 1))

        val response = controller.getRecruiterPositions(id = recruiterId)

        val data = response.body?.data
        Assertions.assertNotNull(data)
        Assertions.assertEquals(1, data!!.items.size)
        Assertions.assertEquals(1, data.total)
        with(data.items[0]) {
            Assertions.assertEquals("p-1", id)
            Assertions.assertEquals("Backend Engineer", title)
            Assertions.assertEquals("Backend role", description)
            Assertions.assertEquals(false, external)
            Assertions.assertEquals(2, assessmentsCount)
            Assertions.assertEquals(true, isActive)
            Assertions.assertEquals(now, createdAt)
        }
    }

    @Test
    fun `getRecruiterPositions should return all items from use case`() = runBlocking {
        val items = listOf(
            RecruiterPositionItem("p-1", "Position A", "Desc A", false, 1, true, now),
            RecruiterPositionItem("p-2", "Position B", "Desc B", true, 0, false, now),
            RecruiterPositionItem("p-3", "Position C", "Desc C", false, 3, true, now)
        )
        whenever(
            getRecruiterPositionsUseCase.execute(GetRecruiterPositionsCommand(recruiterId = recruiterId))
        ).thenReturn(PagedResult(items, 3))

        val response = controller.getRecruiterPositions(id = recruiterId)

        Assertions.assertEquals(3, response.body?.data?.items?.size)
        Assertions.assertEquals(3, response.body?.data?.total)
    }

    @Test
    fun `getRecruiterPositions should return empty list when recruiter has no positions`() = runBlocking {
        whenever(
            getRecruiterPositionsUseCase.execute(GetRecruiterPositionsCommand(recruiterId = recruiterId))
        ).thenReturn(PagedResult(emptyList(), 0))

        val response = controller.getRecruiterPositions(id = recruiterId)

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertEquals(0, response.body?.data?.items?.size)
    }

    @Test
    fun `getRecruiterPositions should return 500 on exception`() = runBlocking {
        whenever(
            getRecruiterPositionsUseCase.execute(GetRecruiterPositionsCommand(recruiterId = recruiterId))
        ).thenThrow(RuntimeException("DynamoDB unavailable"))

        val response = controller.getRecruiterPositions(id = recruiterId)

        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        Assertions.assertEquals("error", response.body?.status)
    }
}
