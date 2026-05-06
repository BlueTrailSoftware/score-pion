package org.example.notifier.domain.port

import org.example.notifier.domain.globalRecipients.GlobalRecipients

interface GlobalRecipientsRepository {
    suspend fun getRecipients(): GlobalRecipients?
    suspend fun addEmail(email: String, updatedBy: String): GlobalRecipients
    suspend fun removeEmail(email: String, updatedBy: String): GlobalRecipients
    suspend fun updateEmail(oldEmail: String, newEmail: String, updatedBy: String): GlobalRecipients
    suspend fun getAllEmails(): List<String>
    suspend fun hasRecipients(): Boolean
}
