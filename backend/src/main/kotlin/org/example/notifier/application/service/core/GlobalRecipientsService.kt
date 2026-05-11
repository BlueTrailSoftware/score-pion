package org.example.notifier.application.service.core

import org.example.notifier.domain.globalRecipients.GlobalRecipients

interface GlobalRecipientsService {
    suspend fun getRecipients(): GlobalRecipients?
    suspend fun getAllEmails(): List<String>
    suspend fun addEmail(email: String, updatedBy: String): GlobalRecipients
    suspend fun removeEmail(email: String, updatedBy: String): GlobalRecipients
    suspend fun updateEmail(oldEmail: String, newEmail: String, updatedBy: String): GlobalRecipients
}