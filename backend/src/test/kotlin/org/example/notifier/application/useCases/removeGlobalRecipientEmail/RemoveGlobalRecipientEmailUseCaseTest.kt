package org.example.notifier.application.useCases.removeGlobalRecipientEmail

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.service.core.GlobalRecipientsService
import org.example.notifier.domain.globalRecipients.GlobalRecipients
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

class RemoveGlobalRecipientEmailUseCaseTest {

    private lateinit var globalRecipientsService: GlobalRecipientsService
    private lateinit var useCase: RemoveGlobalRecipientEmailUseCase

    private val updatedRecipients = GlobalRecipients(
        emails = mutableListOf("b@example.com"),
        description = "Global recipients",
        updatedAt = "2026-01-01T00:00:00Z",
        updatedBy = "admin-1"
    )

    @BeforeEach
    fun setup() {
        globalRecipientsService = mock(GlobalRecipientsService::class.java)
        useCase = RemoveGlobalRecipientEmailUseCase(globalRecipientsService)
    }

    @Test
    fun `execute should remove email and return mapped result`() = runBlocking<Unit> {
        val command = RemoveGlobalRecipientEmailCommand(email = "a@example.com", updatedBy = "admin-1")
        whenever(globalRecipientsService.removeEmail("a@example.com", "admin-1")).thenReturn(updatedRecipients)

        val result = useCase.execute(command)

        assertFalse(result.emails.contains("a@example.com"))
        assertEquals("admin-1", result.updatedBy)
    }

    @Test
    fun `execute should propagate IllegalArgumentException when email not found`() = runBlocking<Unit> {
        val command = RemoveGlobalRecipientEmailCommand(email = "missing@example.com", updatedBy = "admin-1")
        whenever(globalRecipientsService.removeEmail("missing@example.com", "admin-1"))
            .thenThrow(IllegalArgumentException("Email not found"))

        assertThrows<IllegalArgumentException> {
            useCase.execute(command)
        }
    }
}