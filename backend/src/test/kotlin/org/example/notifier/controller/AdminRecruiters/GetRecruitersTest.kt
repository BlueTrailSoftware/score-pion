package org.example.notifier.controller.AdminRecruiters

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.model.shared.PagedResult
import org.example.notifier.application.useCases.getRecruiterDetail.GetRecruiterDetailUseCase
import org.example.notifier.application.useCases.getRecruiterPositions.GetRecruiterPositionsUseCase
import org.example.notifier.application.useCases.getRecruiters.GetRecruitersUseCase
import org.example.notifier.application.model.user.RecruiterListItem
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
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

class AdminRecruitersControllerGetRecruitersTest {

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
    fun `getAllRecruiters should return 200 with success status`() = runBlocking {
        whenever(getRecruitersUseCase.execute(any())).thenReturn(PagedResult(emptyList(), 0))

        val response = controller.getAllRecruiters()

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertEquals("success", response.body?.status)
        Assertions.assertEquals("Recruiters data retrieved successfully", response.body?.message)
    }

    @Test
    fun `getAllRecruiters should map RecruiterListItem fields to response`() = runBlocking {
        val item = RecruiterListItem(
            id = "r-1",
            email = "r@example.com",
            name = "Alice",
            isActive = true,
            status = "Active",
            positionsCount = 3,
            createdAt = now
        )
        whenever(getRecruitersUseCase.execute(any())).thenReturn(PagedResult(listOf(item), 1))

        val response = controller.getAllRecruiters()

        val data = response.body?.data
        Assertions.assertNotNull(data)
        Assertions.assertEquals(1, data!!.items.size)
        Assertions.assertEquals(1, data.total)
        with(data.items[0]) {
            Assertions.assertEquals("r-1", id)
            Assertions.assertEquals("r@example.com", email)
            Assertions.assertEquals("Alice", name)
            Assertions.assertEquals(true, isActive)
            Assertions.assertEquals("Active", status)
            Assertions.assertEquals(3, positionsCount)
            Assertions.assertEquals(now, createdAt)
        }
    }

    @Test
    fun `getAllRecruiters should return all items from use case`() = runBlocking {
        val items = listOf(
            RecruiterListItem("r-1", "a@example.com", "Alice", true, "Active", 2, now),
            RecruiterListItem("r-2", "b@example.com", "Bob", false, "Inactive", 0, now),
            RecruiterListItem("", "pending@example.com", "", false, "Pending", 1, now)
        )
        whenever(getRecruitersUseCase.execute(any())).thenReturn(PagedResult(items, 3))

        val response = controller.getAllRecruiters()

        Assertions.assertEquals(3, response.body?.data?.items?.size)
        Assertions.assertEquals(3, response.body?.data?.total)
    }

    @Test
    fun `getAllRecruiters should return 500 on exception`() = runBlocking {
        whenever(getRecruitersUseCase.execute(any())).thenThrow(RuntimeException("DynamoDB unavailable"))

        val response = controller.getAllRecruiters()

        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        Assertions.assertEquals("error", response.body?.status)
    }
}
