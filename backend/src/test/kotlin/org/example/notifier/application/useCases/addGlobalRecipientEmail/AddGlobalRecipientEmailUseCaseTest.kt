package org.example.notifier.application.useCases.addGlobalRecipientEmail

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.service.core.GlobalRecipientsService
import org.example.notifier.domain.globalRecipients.GlobalRecipients
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

class AddGlobalRecipientEmailUseCaseTest {

    private lateinit var globalRecipientsService: GlobalRecipientsService
    private lateinit var useCase: AddGlobalRecipientEmailUseCase

    private val updatedRecipients = GlobalRecipients(
        emails = mutableListOf("existing@example.com", "new@example.com"),
        description = "Global recipients",
        updatedAt = "2026-01-01T00:00:00Z",
        updatedBy = "admin-1"
    )

    @BeforeEach
    fun setup() {
        globalRecipientsService = mock(GlobalRecipientsService::class.java)
        useCase = AddGlobalRecipientEmailUseCase(globalRecipientsService)
    }

    @Test
    fun `execute should add email and return mapped result`() = runBlocking<Unit> {
        val command = AddGlobalRecipientEmailCommand(email = "new@example.com", updatedBy = "admin-1")
        whenever(globalRecipientsService.addEmail("new@example.com", "admin-1")).thenReturn(updatedRecipients)

        val result = useCase.execute(command)

        assertTrue(result.emails.contains("new@example.com"))
        assertEquals("admin-1", result.updatedBy)
    }

    @Test
    fun `execute should propagate IllegalArgumentException from service`() = runBlocking<Unit> {
        val command = AddGlobalRecipientEmailCommand(email = "duplicate@example.com", updatedBy = "admin-1")
        whenever(globalRecipientsService.addEmail("duplicate@example.com", "admin-1"))
            .thenThrow(IllegalArgumentException("Email already exists"))

        assertThrows<IllegalArgumentException> {
            useCase.execute(command)
        }
    }
}