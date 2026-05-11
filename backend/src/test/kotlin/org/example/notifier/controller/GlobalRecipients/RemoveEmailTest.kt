package org.example.notifier.controller.GlobalRecipients

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.model.globalRecipients.GlobalRecipientsResult
import org.example.notifier.application.useCases.addGlobalRecipientEmail.AddGlobalRecipientEmailUseCase
import org.example.notifier.application.useCases.getGlobalRecipientEmails.GetGlobalRecipientEmailsUseCase
import org.example.notifier.application.useCases.getGlobalRecipients.GetGlobalRecipientsUseCase
import org.example.notifier.application.useCases.removeGlobalRecipientEmail.RemoveGlobalRecipientEmailCommand
import org.example.notifier.application.useCases.removeGlobalRecipientEmail.RemoveGlobalRecipientEmailUseCase
import org.example.notifier.application.useCases.updateGlobalRecipientEmail.UpdateGlobalRecipientEmailUseCase
import org.example.notifier.infrastructure.controller.GlobalRecipientsController
import org.example.notifier.infrastructure.logging.LoggerPort
import org.example.notifier.infrastructure.security.SecurityUtils
import org.example.notifier.infrastructure.util.factory.ResponseEntityFactory
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus

class RemoveEmailTest {

    private lateinit var getGlobalRecipientsUseCase: GetGlobalRecipientsUseCase
    private lateinit var getGlobalRecipientEmailsUseCase: GetGlobalRecipientEmailsUseCase
    private lateinit var addGlobalRecipientEmailUseCase: AddGlobalRecipientEmailUseCase
    private lateinit var removeGlobalRecipientEmailUseCase: RemoveGlobalRecipientEmailUseCase
    private lateinit var updateGlobalRecipientEmailUseCase: UpdateGlobalRecipientEmailUseCase
    private lateinit var securityUtils: SecurityUtils
    private lateinit var logger: LoggerPort
    private lateinit var controller: GlobalRecipientsController

    private val responseFactory = ResponseEntityFactory()
    private val adminId = "admin-1"

    private val updatedResult = GlobalRecipientsResult(
        emails = listOf("b@example.com"),
        description = "Global recipients",
        updatedAt = "2026-01-01T00:00:00Z",
        updatedBy = adminId
    )

    @BeforeEach
    fun setup() {
        getGlobalRecipientsUseCase = mock(GetGlobalRecipientsUseCase::class.java)
        getGlobalRecipientEmailsUseCase = mock(GetGlobalRecipientEmailsUseCase::class.java)
        addGlobalRecipientEmailUseCase = mock(AddGlobalRecipientEmailUseCase::class.java)
        removeGlobalRecipientEmailUseCase = mock(RemoveGlobalRecipientEmailUseCase::class.java)
        updateGlobalRecipientEmailUseCase = mock(UpdateGlobalRecipientEmailUseCase::class.java)
        securityUtils = mock(SecurityUtils::class.java)
        logger = mock(LoggerPort::class.java)

        controller = GlobalRecipientsController(
            getGlobalRecipientsUseCase = getGlobalRecipientsUseCase,
            getGlobalRecipientEmailsUseCase = getGlobalRecipientEmailsUseCase,
            addGlobalRecipientEmailUseCase = addGlobalRecipientEmailUseCase,
            removeGlobalRecipientEmailUseCase = removeGlobalRecipientEmailUseCase,
            updateGlobalRecipientEmailUseCase = updateGlobalRecipientEmailUseCase,
            securityUtils = securityUtils,
            responseFactory = responseFactory,
            logger = logger
        )
    }

    @Test
    fun `removeEmail should return 200 with updated recipients on success`() = runBlocking {
        whenever(securityUtils.getCurrentUserId()).thenReturn(adminId)
        whenever(removeGlobalRecipientEmailUseCase.execute(RemoveGlobalRecipientEmailCommand("a@example.com", adminId)))
            .thenReturn(updatedResult)

        val response = controller.removeEmail("a@example.com")

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("success", response.body?.status)
        assertEquals("Email removed successfully", response.body?.message)
        assertFalse(response.body?.data?.emails?.contains("a@example.com") == true)
    }

    @Test
    fun `removeEmail should return 404 on IllegalArgumentException`() = runBlocking {
        whenever(securityUtils.getCurrentUserId()).thenReturn(adminId)
        whenever(removeGlobalRecipientEmailUseCase.execute(RemoveGlobalRecipientEmailCommand("missing@example.com", adminId)))
            .thenThrow(IllegalArgumentException("Email not found"))

        val response = controller.removeEmail("missing@example.com")

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertEquals("error", response.body?.status)
        assertEquals("Email not found", response.body?.message)
    }

    @Test
    fun `removeEmail should return 500 on unexpected exception`() = runBlocking {
        whenever(securityUtils.getCurrentUserId()).thenReturn(adminId)
        whenever(removeGlobalRecipientEmailUseCase.execute(RemoveGlobalRecipientEmailCommand("a@example.com", adminId)))
            .thenThrow(RuntimeException("DB error"))

        val response = controller.removeEmail("a@example.com")

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals("error", response.body?.status)
        assertTrue(response.body?.message?.contains("Failed to remove email") == true)
    }
}