package org.example.notifier.controller.GlobalRecipients

import kotlinx.coroutines.runBlocking
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

class GetEmailsTest {

    private lateinit var getGlobalRecipientsUseCase: GetGlobalRecipientsUseCase
    private lateinit var getGlobalRecipientEmailsUseCase: GetGlobalRecipientEmailsUseCase
    private lateinit var addGlobalRecipientEmailUseCase: AddGlobalRecipientEmailUseCase
    private lateinit var removeGlobalRecipientEmailUseCase: RemoveGlobalRecipientEmailUseCase
    private lateinit var updateGlobalRecipientEmailUseCase: UpdateGlobalRecipientEmailUseCase
    private lateinit var securityUtils: SecurityUtils
    private lateinit var logger: LoggerPort
    private lateinit var controller: GlobalRecipientsController

    private val responseFactory = ResponseEntityFactory()

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
    fun `getAllEmails should return 200 with email list`() = runBlocking {
        whenever(getGlobalRecipientEmailsUseCase.execute()).thenReturn(listOf("a@example.com", "b@example.com"))

        val response = controller.getAllEmails()

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("success", response.body?.status)
        assertEquals("Emails retrieved successfully", response.body?.message)
        assertEquals(listOf("a@example.com", "b@example.com"), response.body?.data?.emails)
    }

    @Test
    fun `getAllEmails should return 200 with empty list when no emails`() = runBlocking {
        whenever(getGlobalRecipientEmailsUseCase.execute()).thenReturn(emptyList())

        val response = controller.getAllEmails()

        assertEquals(HttpStatus.OK, response.statusCode)
        assertTrue(response.body?.data?.emails?.isEmpty() == true)
    }

    @Test
    fun `getAllEmails should return 500 on unexpected exception`() = runBlocking {
        whenever(getGlobalRecipientEmailsUseCase.execute()).thenThrow(RuntimeException("DB error"))

        val response = controller.getAllEmails()

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals("error", response.body?.status)
        assertTrue(response.body?.message?.contains("Failed to fetch recipient emails") == true)
    }
}