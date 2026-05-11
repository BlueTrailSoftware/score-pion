package org.example.notifier.application.service.core.impl

import kotlinx.coroutines.runBlocking
import org.example.notifier.domain.globalRecipients.GlobalRecipients
import org.example.notifier.domain.port.GlobalRecipientsRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class GlobalRecipientsServiceImplTest {

    private lateinit var repository: GlobalRecipientsRepository
    private lateinit var service: GlobalRecipientsServiceImpl

    private val recipients = GlobalRecipients(
        emails = mutableListOf("a@example.com", "b@example.com"),
        description = "Global recipients",
        updatedAt = "2026-01-01T00:00:00Z",
        updatedBy = "admin-1"
    )

    @BeforeEach
    fun setup() {
        repository = mock(GlobalRecipientsRepository::class.java)
        service = GlobalRecipientsServiceImpl(repository)
    }

    @Test
    fun `getRecipients should return recipients from repository`() = runBlocking<Unit> {
        whenever(repository.getRecipients()).thenReturn(recipients)

        val result = service.getRecipients()

        assertEquals(recipients, result)
        verify(repository).getRecipients()
    }

    @Test
    fun `getRecipients should return null when repository returns null`() = runBlocking<Unit> {
        whenever(repository.getRecipients()).thenReturn(null)

        assertNull(service.getRecipients())
    }

    @Test
    fun `getAllEmails should return email list from repository`() = runBlocking<Unit> {
        whenever(repository.getAllEmails()).thenReturn(listOf("a@example.com", "b@example.com"))

        val result = service.getAllEmails()

        assertEquals(listOf("a@example.com", "b@example.com"), result)
        verify(repository).getAllEmails()
    }

    @Test
    fun `addEmail should delegate to repository with correct arguments`() = runBlocking<Unit> {
        whenever(repository.addEmail("c@example.com", "admin-1")).thenReturn(recipients)

        val result = service.addEmail("c@example.com", "admin-1")

        assertEquals(recipients, result)
        verify(repository).addEmail("c@example.com", "admin-1")
    }

    @Test
    fun `removeEmail should delegate to repository with correct arguments`() = runBlocking<Unit> {
        whenever(repository.removeEmail("a@example.com", "admin-1")).thenReturn(recipients)

        val result = service.removeEmail("a@example.com", "admin-1")

        assertEquals(recipients, result)
        verify(repository).removeEmail("a@example.com", "admin-1")
    }

    @Test
    fun `updateEmail should delegate to repository with correct arguments`() = runBlocking<Unit> {
        whenever(repository.updateEmail("old@example.com", "new@example.com", "admin-1")).thenReturn(recipients)

        val result = service.updateEmail("old@example.com", "new@example.com", "admin-1")

        assertEquals(recipients, result)
        verify(repository).updateEmail("old@example.com", "new@example.com", "admin-1")
    }
}