package org.example.notifier.controller.GlobalRecipients

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.model.globalRecipients.GlobalRecipientsResult
import org.example.notifier.application.useCases.addGlobalRecipientEmail.AddGlobalRecipientEmailCommand
import org.example.notifier.application.useCases.addGlobalRecipientEmail.AddGlobalRecipientEmailUseCase
import org.example.notifier.application.useCases.getGlobalRecipientEmails.GetGlobalRecipientEmailsUseCase
import org.example.notifier.application.useCases.getGlobalRecipients.GetGlobalRecipientsUseCase
import org.example.notifier.application.useCases.removeGlobalRecipientEmail.RemoveGlobalRecipientEmailUseCase
import org.example.notifier.application.useCases.updateGlobalRecipientEmail.UpdateGlobalRecipientEmailUseCase
import org.example.notifier.infrastructure.controller.GlobalRecipientsController
import org.example.notifier.infrastructure.dto.request.AddEmailRequest
import org.example.notifier.infrastructure.logging.LoggerPort
import org.example.notifier.infrastructure.security.SecurityUtils
import org.example.notifier.infrastructure.util.factory.ResponseEntityFactory
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus

class AddEmailTest {

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
        emails = listOf("existing@example.com", "new@example.com"),
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
    fun `addEmail should return 200 with updated recipients on success`() = runBlocking {
        whenever(securityUtils.getCurrentUserId()).thenReturn(adminId)
        whenever(addGlobalRecipientEmailUseCase.execute(AddGlobalRecipientEmailCommand("new@example.com", adminId)))
            .thenReturn(updatedResult)

        val response = controller.addEmail(AddEmailRequest("new@example.com"))

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("success", response.body?.status)
        assertEquals("Email added successfully", response.body?.message)
        assertTrue(response.body?.data?.emails?.contains("new@example.com") == true)
    }

    @Test
    fun `addEmail should return 400 on IllegalArgumentException`() = runBlocking {
        whenever(securityUtils.getCurrentUserId()).thenReturn(adminId)
        whenever(addGlobalRecipientEmailUseCase.execute(AddGlobalRecipientEmailCommand("duplicate@example.com", adminId)))
            .thenThrow(IllegalArgumentException("Email already exists"))

        val response = controller.addEmail(AddEmailRequest("duplicate@example.com"))

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("error", response.body?.status)
        assertEquals("Email already exists", response.body?.message)
    }

    @Test
    fun `addEmail should return 500 on unexpected exception`() = runBlocking {
        whenever(securityUtils.getCurrentUserId()).thenReturn(adminId)
        whenever(addGlobalRecipientEmailUseCase.execute(AddGlobalRecipientEmailCommand("new@example.com", adminId)))
            .thenThrow(RuntimeException("DB error"))

        val response = controller.addEmail(AddEmailRequest("new@example.com"))

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals("error", response.body?.status)
        assertTrue(response.body?.message?.contains("Failed to add email") == true)
    }
}