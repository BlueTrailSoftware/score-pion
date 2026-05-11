package org.example.notifier.application.useCases.getGlobalRecipientEmails

import org.example.notifier.application.service.core.GlobalRecipientsService
import org.springframework.stereotype.Component

@Component
class GetGlobalRecipientEmailsUseCase(
    private val globalRecipientsService: GlobalRecipientsService
) {
    suspend fun execute(): List<String> =
        globalRecipientsService.getAllEmails()
}