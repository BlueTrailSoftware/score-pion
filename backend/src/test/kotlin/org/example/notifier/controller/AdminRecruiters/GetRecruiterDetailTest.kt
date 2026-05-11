package org.example.notifier.controller.AdminRecruiters

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.useCases.getRecruiterDetail.GetRecruiterDetailCommand
import org.example.notifier.application.model.user.GetRecruiterDetailResult
import org.example.notifier.application.useCases.getRecruiterDetail.GetRecruiterDetailUseCase
import org.example.notifier.application.model.position.RecruiterPositionItem
import org.example.notifier.application.useCases.getRecruiterPositions.GetRecruiterPositionsUseCase
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
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

class AdminRecruitersControllerGetRecruiterDetailTest {

    private lateinit var getRecruitersUseCase: GetRecruitersUseCase
    private lateinit var inviteUserUseCase: InviteUserUseCase
    private lateinit var getRecruiterDetailUseCase: GetRecruiterDetailUseCase
    private lateinit var getRecruiterPositionsUseCase: GetRecruiterPositionsUseCase
    private lateinit var updateRecruiterActiveStatusUseCase: UpdateRecruiterActiveStatusUseCase
    private lateinit var syncRecruiterPositionsUseCase: SyncRecruiterPositionsUseCase
    private lateinit var securityUtils: SecurityUtils
    private lateinit var logger: LoggerPort
    private lateinit var controller: AdminRecruitersController

    private val responseFactory = ResponseEntityFactory()
    private val fixedNow = LocalDateTime.of(2026, 3, 20, 10, 0)

    @BeforeEach
    fun setup() {
        getRecruitersUseCase = Mockito.mock(GetRecruitersUseCase::class.java)
        inviteUserUseCase = Mockito.mock(InviteUserUseCase::class.java)
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
    fun `getRecruiterById should return 200 with recruiter detail when found`() = runBlocking<Unit> {
        val result = aRecruiterDetailResult(
            id = "r-1",
            positions = listOf(
                aPositionItem(id = "pos-1", title = "Backend Engineer", assessmentsCount = 3),
                aPositionItem(id = "pos-2", title = "Frontend Engineer", assessmentsCount = 1)
            )
        )
        whenever(getRecruiterDetailUseCase.execute(GetRecruiterDetailCommand("r-1"))).thenReturn(result)

        val response = controller.getRecruiterById("r-1")

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertEquals("success", response.body?.status)
        Assertions.assertEquals("Recruiter retrieved successfully", response.body?.message)

        val data = response.body?.data!!
        Assertions.assertEquals("r-1", data.id)
        Assertions.assertEquals("r-1@company.com", data.email)
        Assertions.assertEquals("Recruiter r-1", data.name)
        Assertions.assertEquals("RECRUITER", data.role)
        Assertions.assertEquals(true, data.isActive)
        Assertions.assertEquals(2, data.positionsCount)
        Assertions.assertEquals(2, data.positions.size)

        val firstPos = data.positions.first { it.id == "pos-1" }
        Assertions.assertEquals("Backend Engineer", firstPos.title)
        Assertions.assertEquals(3, firstPos.assessmentsCount)
    }

    @Test
    fun `getRecruiterById should return 404 when recruiter not found`() = runBlocking<Unit> {
        whenever(getRecruiterDetailUseCase.execute(GetRecruiterDetailCommand("missing"))).thenReturn(null)

        val response = controller.getRecruiterById("missing")

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        Assertions.assertEquals("error", response.body?.status)
        Assertions.assertEquals("Recruiter not found", response.body?.message)
        Assertions.assertNull(response.body?.data)
    }

    @Test
    fun `getRecruiterById should return 500 when use case throws an exception`() = runBlocking<Unit> {
        whenever(getRecruiterDetailUseCase.execute(GetRecruiterDetailCommand("r-1")))
            .thenThrow(RuntimeException("DynamoDB timeout"))

        val response = controller.getRecruiterById("r-1")

        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        Assertions.assertEquals("error", response.body?.status)
        verify(logger).error(eq("Error fetching recruiter: DynamoDB timeout"), any<Throwable>())
    }

    @Test
    fun `getRecruiterById should return recruiter with no positions`() = runBlocking<Unit> {
        val result = aRecruiterDetailResult(id = "r-2", positions = emptyList())
        whenever(getRecruiterDetailUseCase.execute(GetRecruiterDetailCommand("r-2"))).thenReturn(result)

        val response = controller.getRecruiterById("r-2")

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertEquals(0, response.body?.data?.positionsCount)
        Assertions.assertEquals(emptyList<Any>(), response.body?.data?.positions)
    }

    @Test
    fun `getRecruiterById should map position fields correctly`() = runBlocking<Unit> {
        val position = RecruiterPositionItem(
            id = "pos-99",
            title = "QA Lead",
            description = "Quality role",
            external = true,
            assessmentsCount = 0,
            isActive = false,
            createdAt = fixedNow
        )
        val result = aRecruiterDetailResult(id = "r-1", positions = listOf(position))
        whenever(getRecruiterDetailUseCase.execute(GetRecruiterDetailCommand("r-1"))).thenReturn(result)

        val response = controller.getRecruiterById("r-1")

        val dto = response.body?.data?.positions?.single()!!
        Assertions.assertEquals("pos-99", dto.id)
        Assertions.assertEquals("QA Lead", dto.title)
        Assertions.assertEquals("Quality role", dto.description)
        Assertions.assertEquals(true, dto.external)
        Assertions.assertEquals(false, dto.isActive)
        Assertions.assertEquals(0, dto.assessmentsCount)
        Assertions.assertEquals(fixedNow, dto.createdAt)
    }

    // --- helpers ---

    private fun aRecruiterDetailResult(
        id: String,
        positions: List<RecruiterPositionItem>
    ) = GetRecruiterDetailResult(
        id = id,
        email = "$id@company.com",
        name = "Recruiter $id",
        role = "RECRUITER",
        isActive = true,
        positions = positions,
        positionsCount = positions.size,
        createdAt = fixedNow
    )

    private fun aPositionItem(
        id: String,
        title: String,
        assessmentsCount: Int = 0
    ) = RecruiterPositionItem(
        id = id,
        title = title,
        description = "Description for $id",
        external = false,
        assessmentsCount = assessmentsCount,
        isActive = true,
        createdAt = fixedNow
    )
}
