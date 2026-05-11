package org.example.notifier.application.useCases.getGlobalRecipients

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.service.core.GlobalRecipientsService
import org.example.notifier.domain.globalRecipients.GlobalRecipients
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

class GetGlobalRecipientsUseCaseTest {

    private lateinit var globalRecipientsService: GlobalRecipientsService
    private lateinit var useCase: GetGlobalRecipientsUseCase

    @BeforeEach
    fun setup() {
        globalRecipientsService = mock(GlobalRecipientsService::class.java)
        useCase = GetGlobalRecipientsUseCase(globalRecipientsService)
    }

    @Test
    fun `execute should return mapped result when service returns recipients`() = runBlocking<Unit> {
        val recipients = GlobalRecipients(
            emails = mutableListOf("a@example.com"),
            description = "Global recipients",
            updatedAt = "2026-01-01T00:00:00Z",
            updatedBy = "admin-1"
        )
        whenever(globalRecipientsService.getRecipients()).thenReturn(recipients)

        val result = useCase.execute()

        assertEquals(listOf("a@example.com"), result.emails)
        assertEquals("Global recipients", result.description)
        assertEquals("2026-01-01T00:00:00Z", result.updatedAt)
        assertEquals("admin-1", result.updatedBy)
    }

    @Test
    fun `execute should return default empty result when service returns null`() = runBlocking<Unit> {
        whenever(globalRecipientsService.getRecipients()).thenReturn(null)

        val result = useCase.execute()

        assertTrue(result.emails.isEmpty())
        assertNull(result.updatedBy)
    }
}