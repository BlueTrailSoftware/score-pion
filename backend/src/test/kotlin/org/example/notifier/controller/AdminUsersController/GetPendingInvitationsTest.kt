package org.example.notifier.controller.AdminUsersController

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.useCases.getAllAdmins.GetAllAdminsUseCase
import org.example.notifier.application.useCases.getPendingInvitations.GetPendingInvitationsUseCase
import org.example.notifier.application.model.invitation.InvitationItem
import org.example.notifier.application.useCases.inviteUser.InviteUserUseCase
import org.example.notifier.application.useCases.updateAdminActiveStatus.UpdateAdminActiveStatusUseCase
import org.example.notifier.infrastructure.controller.AdminUsersController
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

class AdminUsersControllerGetPendingInvitationsTest {

    private lateinit var inviteUserUseCase: InviteUserUseCase
    private lateinit var getAllAdminsUseCase: GetAllAdminsUseCase
    private lateinit var updateAdminActiveStatusUseCase: UpdateAdminActiveStatusUseCase
    private lateinit var getPendingInvitationsUseCase: GetPendingInvitationsUseCase
    private lateinit var securityUtils: SecurityUtils
    private lateinit var logger: LoggerPort
    private lateinit var controller: AdminUsersController

    private val responseFactory = ResponseEntityFactory()
    private val now = LocalDateTime.now()

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
    fun `getPendingInvitations should return 200 with success status`() = runBlocking {
        whenever(getPendingInvitationsUseCase.execute()).thenReturn(emptyList())

        val response = controller.getPendingInvitations()

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertEquals("success", response.body?.status)
        Assertions.assertEquals("Invitations retrieved successfully", response.body?.message)
    }

    @Test
    fun `getPendingInvitations should map InvitationItem fields to response`() = runBlocking {
        val item = InvitationItem(
            id = "inv-1",
            email = "invited@example.com",
            invitedBy = "admin-1",
            assignedPositions = listOf("pos-1", "pos-2"),
            status = "PENDING",
            createdAt = now,
            expiresAt = now.plusDays(3),
            acceptedAt = null
        )
        whenever(getPendingInvitationsUseCase.execute()).thenReturn(listOf(item))

        val response = controller.getPendingInvitations()

        val data = response.body?.data
        Assertions.assertNotNull(data)
        Assertions.assertEquals(1, data!!.size)
        with(data[0]) {
            Assertions.assertEquals("inv-1", id)
            Assertions.assertEquals("invited@example.com", email)
            Assertions.assertEquals("admin-1", invitedBy)
            Assertions.assertEquals("PENDING", status)
        }
    }

    @Test
    fun `getPendingInvitations should return all items from use case`() = runBlocking {
        val items = listOf(
            InvitationItem("inv-1", "a@example.com", "admin-1", emptyList(), "PENDING", now, now.plusDays(3), null),
            InvitationItem("inv-2", "b@example.com", "admin-1", listOf("pos-1"), "PENDING", now, now.plusDays(3), null),
            InvitationItem("inv-3", "c@example.com", "admin-2", emptyList(), "PENDING", now, now.plusDays(3), null)
        )
        whenever(getPendingInvitationsUseCase.execute()).thenReturn(items)

        val response = controller.getPendingInvitations()

        Assertions.assertEquals(3, response.body?.data?.size)
    }

    @Test
    fun `getPendingInvitations should return empty list when there are no pending invitations`() = runBlocking {
        whenever(getPendingInvitationsUseCase.execute()).thenReturn(emptyList())

        val response = controller.getPendingInvitations()

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertEquals(0, response.body?.data?.size)
    }

    @Test
    fun `getPendingInvitations should return 500 on exception`() = runBlocking {
        whenever(getPendingInvitationsUseCase.execute()).thenThrow(RuntimeException("DynamoDB unavailable"))

        val response = controller.getPendingInvitations()

        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        Assertions.assertEquals("error", response.body?.status)
    }
}