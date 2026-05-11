package org.example.notifier.controller.AdminUsersController

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.useCases.getAllAdmins.GetAllAdminsUseCase
import org.example.notifier.application.useCases.getPendingInvitations.GetPendingInvitationsUseCase
import org.example.notifier.application.useCases.inviteUser.InviteUserUseCase
import org.example.notifier.application.useCases.updateAdminActiveStatus.UpdateAdminActiveStatusCommand
import org.example.notifier.application.useCases.updateAdminActiveStatus.UpdateAdminActiveStatusResult
import org.example.notifier.application.useCases.updateAdminActiveStatus.UpdateAdminActiveStatusUseCase
import org.example.notifier.infrastructure.controller.AdminUsersController
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

class AdminUsersControllerUpdateAdminActiveStatusTest {

    private lateinit var inviteUserUseCase: InviteUserUseCase
    private lateinit var getAllAdminsUseCase: GetAllAdminsUseCase
    private lateinit var updateAdminActiveStatusUseCase: UpdateAdminActiveStatusUseCase
    private lateinit var getPendingInvitationsUseCase: GetPendingInvitationsUseCase
    private lateinit var securityUtils: SecurityUtils
    private lateinit var logger: LoggerPort
    private lateinit var controller: AdminUsersController

    private val responseFactory = ResponseEntityFactory()
    private val now = LocalDateTime.now()
    private val adminId = UUID.randomUUID().toString()

    @BeforeEach
    fun setup() {
        inviteUserUseCase = Mockito.mock(InviteUserUseCase::class.java)
        getAllAdminsUseCase = Mockito.mock(GetAllAdminsUseCase::class.java)
        updateAdminActiveStatusUseCase = Mockito.mock(UpdateAdminActiveStatusUseCase::class.java)
        getPendingInvitationsUseCase = Mockito.mock(GetPendingInvitationsUseCase::class.java)
        securityUtils = Mockito.mock(SecurityUtils::class.java)
        logger = Mockito.mock(LoggerPort::class.java)

        controller = AdminUsersController(
            inviteUserUseCase = inviteUserUseCase,
            getAllAdminsUseCase = getAllAdminsUseCase,
            updateAdminActiveStatusUseCase = updateAdminActiveStatusUseCase,
            getPendingInvitationsUseCase = getPendingInvitationsUseCase,
            securityUtils = securityUtils,
            responseFactory = responseFactory,
            logger = logger,
        )
    }

    @Test
    fun `updateAdminActiveStatus should return 200 with success status`() = runBlocking {
        val result = UpdateAdminActiveStatusResult(
            id = adminId,
            email = "admin@example.com",
            name = "Admin Name",
            role = "ADMIN",
            isActive = true,
            createdAt = now
        )
        whenever(
            updateAdminActiveStatusUseCase.execute(
                UpdateAdminActiveStatusCommand(adminId = adminId, isActive = true)
            )
        ).thenReturn(result)

        val response = controller.updateAdminActiveStatus(
            id = adminId,
            request = UpdateActiveStatusRequest(isActive = true)
        )

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertEquals("success", response.body?.status)
        Assertions.assertEquals("Admin status updated successfully", response.body?.message)
    }

    @Test
    fun `updateAdminActiveStatus should return 200 with updated data in body`() = runBlocking {
        val result = UpdateAdminActiveStatusResult(
            id = adminId,
            email = "admin@example.com",
            name = "Admin Name",
            role = "ADMIN",
            isActive = false,
            createdAt = now
        )
        whenever(
            updateAdminActiveStatusUseCase.execute(
                UpdateAdminActiveStatusCommand(adminId = adminId, isActive = false)
            )
        ).thenReturn(result)

        val response = controller.updateAdminActiveStatus(
            id = adminId,
            request = UpdateActiveStatusRequest(isActive = false)
        )

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertEquals(adminId, response.body?.data?.id)
        Assertions.assertEquals("admin@example.com", response.body?.data?.email)
        Assertions.assertEquals("ADMIN", response.body?.data?.role)
        Assertions.assertEquals(false, response.body?.data?.isActive)
    }

    @Test
    fun `updateAdminActiveStatus should return 404 when admin is not found`() = runBlocking {
        whenever(
            updateAdminActiveStatusUseCase.execute(
                UpdateAdminActiveStatusCommand(adminId = adminId, isActive = true)
            )
        ).thenThrow(IllegalArgumentException("Admin not found"))

        val response = controller.updateAdminActiveStatus(
            id = adminId,
            request = UpdateActiveStatusRequest(isActive = true)
        )

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        Assertions.assertEquals("error", response.body?.status)
        Assertions.assertEquals("Admin not found", response.body?.message)
    }

    @Test
    fun `updateAdminActiveStatus should return 404 with default message when exception has no message`() = runBlocking {
        whenever(
            updateAdminActiveStatusUseCase.execute(
                UpdateAdminActiveStatusCommand(adminId = adminId, isActive = false)
            )
        ).thenThrow(IllegalArgumentException())

        val response = controller.updateAdminActiveStatus(
            id = adminId,
            request = UpdateActiveStatusRequest(isActive = false)
        )

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        Assertions.assertEquals("error", response.body?.status)
        Assertions.assertEquals("Admin not found", response.body?.message)
    }
}