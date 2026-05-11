package org.example.notifier.controller.AdminUsersController

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.useCases.getAllAdmins.GetAllAdminsUseCase
import org.example.notifier.application.useCases.getPendingInvitations.GetPendingInvitationsUseCase
import org.example.notifier.application.useCases.getRecruiterDetail.GetRecruiterDetailUseCase
import org.example.notifier.application.useCases.getRecruiterPositions.GetRecruiterPositionsUseCase
import org.example.notifier.application.useCases.getRecruiters.GetRecruitersUseCase
import org.example.notifier.application.useCases.inviteUser.InviteUserResult
import org.example.notifier.application.useCases.inviteUser.InviteUserUseCase
import org.example.notifier.application.useCases.syncRecruiterPositions.SyncRecruiterPositionsUseCase
import org.example.notifier.application.useCases.updateAdminActiveStatus.UpdateAdminActiveStatusUseCase
import org.example.notifier.application.useCases.updateRecruiterActiveStatus.UpdateRecruiterActiveStatusUseCase
import org.example.notifier.domain.user.User
import org.example.notifier.domain.user.UserRole
import org.example.notifier.infrastructure.controller.AdminRecruitersController
import org.example.notifier.infrastructure.controller.AdminUsersController
import org.example.notifier.infrastructure.dto.request.InviteAdminRequest
import org.example.notifier.infrastructure.dto.request.InviteRecruiterRequest
import org.example.notifier.infrastructure.logging.LoggerPort
import org.example.notifier.infrastructure.security.SecurityUtils
import org.example.notifier.infrastructure.util.factory.ResponseEntityFactory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

class AdminRecruitersControllerInviteTest {

    private lateinit var inviteUserUseCase: InviteUserUseCase
    private lateinit var getRecruitersUseCase: GetRecruitersUseCase
    private lateinit var getRecruiterDetailUseCase: GetRecruiterDetailUseCase
    private lateinit var getRecruiterPositionsUseCase: GetRecruiterPositionsUseCase
    private lateinit var updateRecruiterActiveStatusUseCase: UpdateRecruiterActiveStatusUseCase
    private lateinit var syncRecruiterPositionsUseCase: SyncRecruiterPositionsUseCase
    private lateinit var getAllAdminsUseCase: GetAllAdminsUseCase
    private lateinit var updateAdminActiveStatusUseCase: UpdateAdminActiveStatusUseCase
    private lateinit var getPendingInvitationsUseCase: GetPendingInvitationsUseCase
    private lateinit var securityUtils: SecurityUtils
    private lateinit var logger: LoggerPort
    private lateinit var recruitersController: AdminRecruitersController
    private lateinit var usersController: AdminUsersController

    private val responseFactory = ResponseEntityFactory()

    private val currentAdmin = User(
        id = "admin-1",
        email = "admin@example.com",
        name = "Admin Name",
        role = UserRole.ADMIN,
        isActive = true,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )

    private val inviteResult = InviteUserResult(
        id = "inv-1",
        email = "invited@example.com",
        invitedBy = "admin-1",
        assignedPositions = emptyList(),
        status = "PENDING",
        createdAt = LocalDateTime.now(),
        expiresAt = LocalDateTime.now().plusDays(3)
    )

    @BeforeEach
    fun setup() {
        inviteUserUseCase = Mockito.mock(InviteUserUseCase::class.java)
        getRecruitersUseCase = Mockito.mock(GetRecruitersUseCase::class.java)
        getRecruiterDetailUseCase = Mockito.mock(GetRecruiterDetailUseCase::class.java)
        getRecruiterPositionsUseCase = Mockito.mock(GetRecruiterPositionsUseCase::class.java)
        updateRecruiterActiveStatusUseCase = Mockito.mock(UpdateRecruiterActiveStatusUseCase::class.java)
        syncRecruiterPositionsUseCase = Mockito.mock(SyncRecruiterPositionsUseCase::class.java)
        getAllAdminsUseCase = Mockito.mock(GetAllAdminsUseCase::class.java)
        updateAdminActiveStatusUseCase = Mockito.mock(UpdateAdminActiveStatusUseCase::class.java)
        getPendingInvitationsUseCase = Mockito.mock(GetPendingInvitationsUseCase::class.java)
        securityUtils = Mockito.mock(SecurityUtils::class.java)
        logger = Mockito.mock(LoggerPort::class.java)

        recruitersController = AdminRecruitersController(
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

        usersController = AdminUsersController(
            inviteUserUseCase = inviteUserUseCase,
            getAllAdminsUseCase = getAllAdminsUseCase,
            updateAdminActiveStatusUseCase = updateAdminActiveStatusUseCase,
            getPendingInvitationsUseCase = getPendingInvitationsUseCase,
            securityUtils = securityUtils,
            responseFactory = responseFactory,
            logger = logger,
        )
    }

    // --- inviteAdmin ---

    @Test
    fun `inviteAdmin should return success when use case succeeds`() = runBlocking {
        whenever(securityUtils.getCurrentUser()).thenReturn(currentAdmin)
        whenever(inviteUserUseCase.execute(any())).thenReturn(inviteResult)

        val response = usersController.inviteAdmin(InviteAdminRequest(email = "invited@example.com"))

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertEquals("success", response.body?.status)
        Assertions.assertEquals("Admin invitation sent successfully", response.body?.message)
        Assertions.assertEquals("inv-1", response.body?.data?.id)
    }

    @Test
    fun `inviteAdmin should pass ADMIN role to use case`() = runBlocking<Unit> {
        whenever(securityUtils.getCurrentUser()).thenReturn(currentAdmin)
        whenever(inviteUserUseCase.execute(any())).thenReturn(inviteResult)

        usersController.inviteAdmin(InviteAdminRequest(email = "invited@example.com"))

        verify(inviteUserUseCase).execute(argThat {
            role == UserRole.ADMIN && positionIds == null
                    && invitedBy == currentAdmin.id && adminName == currentAdmin.name
        })
    }

    @Test
    fun `inviteAdmin should log audit entry on success`() = runBlocking {
        whenever(securityUtils.getCurrentUser()).thenReturn(currentAdmin)
        whenever(inviteUserUseCase.execute(any())).thenReturn(inviteResult)

        usersController.inviteAdmin(InviteAdminRequest(email = "invited@example.com"))

        verify(logger).info(
            "ADMIN_INVITATION: Admin ${currentAdmin.email} invited invited@example.com as new ADMIN"
        )
    }

    @Test
    fun `inviteAdmin should return badRequest on IllegalArgumentException`() = runBlocking {
        whenever(securityUtils.getCurrentUser()).thenReturn(currentAdmin)
        whenever(inviteUserUseCase.execute(any()))
            .thenThrow(IllegalArgumentException("An invitation for this email already exists"))

        val response = usersController.inviteAdmin(InviteAdminRequest(email = "existing@example.com"))

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        Assertions.assertEquals("An invitation for this email already exists", response.body?.message)
    }

    @Test
    fun `inviteAdmin should return error on unexpected exception`() = runBlocking {
        whenever(securityUtils.getCurrentUser()).thenReturn(currentAdmin)
        whenever(inviteUserUseCase.execute(any())).thenThrow(RuntimeException("DB unavailable"))

        val response = usersController.inviteAdmin(InviteAdminRequest(email = "invited@example.com"))

        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        Assertions.assertEquals("error", response.body?.status)
    }

    // --- inviteRecruiter ---

    @Test
    fun `inviteRecruiter should return success when use case succeeds`() = runBlocking {
        val resultWithPositions = inviteResult.copy(assignedPositions = listOf("pos-1"))
        whenever(securityUtils.getCurrentUser()).thenReturn(currentAdmin)
        whenever(inviteUserUseCase.execute(any())).thenReturn(resultWithPositions)

        val response = recruitersController.inviteRecruiter(
            InviteRecruiterRequest(email = "recruiter@example.com", positionIds = listOf("pos-1"))
        )

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertEquals("success", response.body?.status)
        Assertions.assertEquals("Invitation sent successfully", response.body?.message)
    }

    @Test
    fun `inviteRecruiter should pass RECRUITER role and positionIds to use case`() = runBlocking<Unit> {
        whenever(securityUtils.getCurrentUser()).thenReturn(currentAdmin)
        whenever(inviteUserUseCase.execute(any())).thenReturn(inviteResult)

        recruitersController.inviteRecruiter(
            InviteRecruiterRequest(email = "recruiter@example.com", positionIds = listOf("pos-1", "pos-2"))
        )

        verify(inviteUserUseCase).execute(argThat {
            role == UserRole.RECRUITER && positionIds == listOf("pos-1", "pos-2")
                    && invitedBy == currentAdmin.id && adminName == currentAdmin.name
        })
    }

    @Test
    fun `inviteRecruiter should return badRequest on IllegalArgumentException`() = runBlocking {
        whenever(securityUtils.getCurrentUser()).thenReturn(currentAdmin)
        whenever(inviteUserUseCase.execute(any()))
            .thenThrow(IllegalArgumentException("An invitation for this email already exists"))

        val response = recruitersController.inviteRecruiter(
            InviteRecruiterRequest(email = "existing@example.com")
        )

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun `inviteRecruiter should return error on unexpected exception`() = runBlocking {
        whenever(securityUtils.getCurrentUser()).thenReturn(currentAdmin)
        whenever(inviteUserUseCase.execute(any())).thenThrow(RuntimeException("DB unavailable"))

        val response = recruitersController.inviteRecruiter(
            InviteRecruiterRequest(email = "recruiter@example.com")
        )

        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
    }
}