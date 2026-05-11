package org.example.notifier.controller.GlobalRecipients

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.model.globalRecipients.GlobalRecipientsResult
import org.example.notifier.application.useCases.addGlobalRecipientEmail.AddGlobalRecipientEmailUseCase
import org.example.notifier.application.useCases.getGlobalRecipientEmails.GetGlobalRecipientEmailsUseCase
import org.example.notifier.application.useCases.getGlobalRecipients.GetGlobalRecipientsUseCase
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

class GetRecipientsTest {

    private lateinit var getGlobalRecipientsUseCase: GetGlobalRecipientsUseCase
    private lateinit var getGlobalRecipientEmailsUseCase: GetGlobalRecipientEmailsUseCase
    private lateinit var addGlobalRecipientEmailUseCase: AddGlobalRecipientEmailUseCase
    private lateinit var removeGlobalRecipientEmailUseCase: RemoveGlobalRecipientEmailUseCase
    private lateinit var updateGlobalRecipientEmailUseCase: UpdateGlobalRecipientEmailUseCase
    private lateinit var securityUtils: SecurityUtils
    private lateinit var logger: LoggerPort
    private lateinit var controller: GlobalRecipientsController

    private val responseFactory = ResponseEntityFactory()

    private val recipientsResult = GlobalRecipientsResult(
        emails = listOf("a@example.com", "b@example.com"),
        description = "Global recipients",
        updatedAt = "2026-01-01T00:00:00Z",
        updatedBy = "admin-1"
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
    fun `getRecipients should return 200 with success status and data`() = runBlocking {
        whenever(getGlobalRecipientsUseCase.execute()).thenReturn(recipientsResult)

        val response = controller.getRecipients()

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("success", response.body?.status)
        assertEquals("Global recipients retrieved successfully", response.body?.message)
        assertEquals(listOf("a@example.com", "b@example.com"), response.body?.data?.emails)
        assertEquals("admin-1", response.body?.data?.updatedBy)
    }

    @Test
    fun `getRecipients should return 500 on unexpected exception`() = runBlocking {
        whenever(getGlobalRecipientsUseCase.execute()).thenThrow(RuntimeException("DB connection lost"))

        val response = controller.getRecipients()

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals("error", response.body?.status)
        assertTrue(response.body?.message?.contains("Failed to fetch global recipients") == true)
    }
}