package org.example.notifier.controller.AdminUsersController

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.model.shared.PagedResult
import org.example.notifier.application.useCases.getAllAdmins.GetAllAdminsUseCase
import org.example.notifier.application.model.user.AdminListItem
import org.example.notifier.application.useCases.getPendingInvitations.GetPendingInvitationsUseCase
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
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

class AdminUsersControllerGetAllAdminsTest {

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
    fun `getAllAdmins should return 200 with success status`() = runBlocking {
        whenever(getAllAdminsUseCase.execute(any())).thenReturn(PagedResult(emptyList(), 0))

        val response = controller.getAllAdmins()

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertEquals("success", response.body?.status)
        Assertions.assertEquals("Admins data retrieved successfully", response.body?.message)
    }

    @Test
    fun `getAllAdmins should map AdminListItem fields to response`() = runBlocking {
        val item = AdminListItem(
            id = "admin-1",
            email = "admin@example.com",
            name = "Admin One",
            isActive = true,
            status = "ACTIVE",
            createdAt = now
        )
        whenever(getAllAdminsUseCase.execute(any())).thenReturn(PagedResult(listOf(item), 1))

        val response = controller.getAllAdmins()

        val data = response.body?.data
        Assertions.assertNotNull(data)
        Assertions.assertEquals(1, data!!.items.size)
        Assertions.assertEquals(1, data.total)
        with(data.items[0]) {
            Assertions.assertEquals("admin-1", id)
            Assertions.assertEquals("admin@example.com", email)
            Assertions.assertEquals("Admin One", name)
            Assertions.assertEquals(true, isActive)
        }
    }

    @Test
    fun `getAllAdmins should return all items from use case`() = runBlocking {
        val items = listOf(
            AdminListItem("a-1", "a1@example.com", "Admin 1", true, "ACTIVE", now),
            AdminListItem("a-2", "a2@example.com", "Admin 2", false, "INACTIVE", now),
            AdminListItem("a-3", "a3@example.com", "Admin 3", true, null, now)
        )
        whenever(getAllAdminsUseCase.execute(any())).thenReturn(PagedResult(items, 3))

        val response = controller.getAllAdmins()

        Assertions.assertEquals(3, response.body?.data?.items?.size)
        Assertions.assertEquals(3, response.body?.data?.total)
    }

    @Test
    fun `getAllAdmins should return 500 on exception`() = runBlocking {
        whenever(getAllAdminsUseCase.execute(any())).thenThrow(RuntimeException("DynamoDB unavailable"))

        val response = controller.getAllAdmins()

        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        Assertions.assertEquals("error", response.body?.status)
    }
}
