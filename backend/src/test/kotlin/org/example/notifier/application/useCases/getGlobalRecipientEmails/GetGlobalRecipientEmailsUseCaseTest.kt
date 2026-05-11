package org.example.notifier.application.useCases.getGlobalRecipientEmails

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.service.core.GlobalRecipientsService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

class GetGlobalRecipientEmailsUseCaseTest {

    private lateinit var globalRecipientsService: GlobalRecipientsService
    private lateinit var useCase: GetGlobalRecipientEmailsUseCase

    @BeforeEach
    fun setup() {
        globalRecipientsService = mock(GlobalRecipientsService::class.java)
        useCase = GetGlobalRecipientEmailsUseCase(globalRecipientsService)
    }

    @Test
    fun `execute should return emails from service`() = runBlocking<Unit> {
        whenever(globalRecipientsService.getAllEmails()).thenReturn(listOf("a@example.com", "b@example.com"))

        val result = useCase.execute()

        assertEquals(listOf("a@example.com", "b@example.com"), result)
    }

    @Test
    fun `execute should return empty list when no emails exist`() = runBlocking<Unit> {
        whenever(globalRecipientsService.getAllEmails()).thenReturn(emptyList())

        assertTrue(useCase.execute().isEmpty())
    }
}