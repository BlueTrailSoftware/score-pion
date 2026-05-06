package org.example.notifier.controller.AdminRecruiters

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.useCases.getRecruiterDetail.GetRecruiterDetailUseCase
import org.example.notifier.application.useCases.getRecruiterPositions.GetRecruiterPositionsUseCase
import org.example.notifier.application.useCases.getRecruiters.GetRecruitersUseCase
import org.example.notifier.application.useCases.inviteUser.InviteUserUseCase
import org.example.notifier.application.useCases.syncRecruiterPositions.SyncRecruiterPositionsCommand
import org.example.notifier.application.useCases.syncRecruiterPositions.SyncRecruiterPositionsUseCase
import org.example.notifier.application.useCases.updateRecruiterActiveStatus.UpdateRecruiterActiveStatusUseCase
import org.example.notifier.domain.user.User
import org.example.notifier.domain.user.UserRole
import org.example.notifier.infrastructure.controller.AdminRecruitersController
import org.example.notifier.infrastructure.dto.request.GrantPositionAccessRequest
import org.example.notifier.infrastructure.logging.LoggerPort
import org.example.notifier.infrastructure.security.SecurityUtils
import org.example.notifier.infrastructure.util.factory.ResponseEntityFactory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.argThat
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import java.time.LocalDateTime
import java.util.UUID

class AdminRecruitersControllerSyncRecruiterPositionsTest {

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
    private val recruiterId = UUID.randomUUID().toString()

    private val currentAdmin = User(
        id = "admin-1",
        email = "admin@example.com",
        name = "Admin Name",
        role = UserRole.ADMIN,
        isActive = true,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )

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
    fun `syncRecruiterPositions should return 200 with success status`() = runBlocking {
        whenever(securityUtils.getCurrentUser()).thenReturn(currentAdmin)

        val response = controller.syncRecruiterPositions(
            id = recruiterId,
            request = GrantPositionAccessRequest(positionIds = listOf("pos-1", "pos-2"))
        )

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertEquals("success", response.body?.status)
        Assertions.assertEquals("Recruiter positions synchronized successfully", response.body?.message)
    }

    @Test
    fun `syncRecruiterPositions should pass correct command to use case`() = runBlocking<Unit> {
        whenever(securityUtils.getCurrentUser()).thenReturn(currentAdmin)

        controller.syncRecruiterPositions(
            id = recruiterId,
            request = GrantPositionAccessRequest(positionIds = listOf("pos-1", "pos-2"))
        )

        verify(syncRecruiterPositionsUseCase).execute(argThat {
            this.recruiterId == recruiterId &&
            positionIds == listOf("pos-1", "pos-2") &&
            grantedBy == currentAdmin.id
        })
    }

    @Test
    fun `syncRecruiterPositions should work with empty positionIds`() = runBlocking {
        whenever(securityUtils.getCurrentUser()).thenReturn(currentAdmin)

        val response = controller.syncRecruiterPositions(
            id = recruiterId,
            request = GrantPositionAccessRequest(positionIds = emptyList())
        )

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertEquals("success", response.body?.status)
    }

    @Test
    fun `syncRecruiterPositions should return 500 on exception`() = runBlocking {
        whenever(securityUtils.getCurrentUser()).thenReturn(currentAdmin)
        whenever(
            syncRecruiterPositionsUseCase.execute(
                SyncRecruiterPositionsCommand(
                    recruiterId = recruiterId,
                    positionIds = listOf("pos-1"),
                    grantedBy = currentAdmin.id
                )
            )
        ).thenThrow(RuntimeException("DynamoDB unavailable"))

        val response = controller.syncRecruiterPositions(
            id = recruiterId,
            request = GrantPositionAccessRequest(positionIds = listOf("pos-1"))
        )

        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        Assertions.assertEquals("error", response.body?.status)
    }
}
