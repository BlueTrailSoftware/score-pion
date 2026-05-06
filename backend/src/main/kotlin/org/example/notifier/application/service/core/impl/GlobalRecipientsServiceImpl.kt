package org.example.notifier.application.service.core.impl

import org.example.notifier.application.service.core.GlobalRecipientsService
import org.example.notifier.domain.globalRecipients.GlobalRecipients
import org.example.notifier.domain.port.GlobalRecipientsRepository
import org.springframework.stereotype.Service

@Service
class GlobalRecipientsServiceImpl(
    private val globalRecipientsRepository: GlobalRecipientsRepository
) : GlobalRecipientsService {

    override suspend fun getRecipients(): GlobalRecipients? =
        globalRecipientsRepository.getRecipients()

    override suspend fun getAllEmails(): List<String> =
        globalRecipientsRepository.getAllEmails()

    override suspend fun addEmail(email: String, updatedBy: String): GlobalRecipients =
        globalRecipientsRepository.addEmail(email, updatedBy)

    override suspend fun removeEmail(email: String, updatedBy: String): GlobalRecipients =
        globalRecipientsRepository.removeEmail(email, updatedBy)

    override suspend fun updateEmail(oldEmail: String, newEmail: String, updatedBy: String): GlobalRecipients =
        globalRecipientsRepository.updateEmail(oldEmail, newEmail, updatedBy)
}