package org.example.notifier.application.useCases.getGlobalRecipients

import org.example.notifier.application.model.globalRecipients.GlobalRecipientsResult
import org.example.notifier.application.model.globalRecipients.toResult
import org.example.notifier.application.service.core.GlobalRecipientsService
import org.example.notifier.domain.globalRecipients.GlobalRecipients
import org.springframework.stereotype.Component

@Component
class GetGlobalRecipientsUseCase(
    private val globalRecipientsService: GlobalRecipientsService
) {
    suspend fun execute(): GlobalRecipientsResult =
        (globalRecipientsService.getRecipients() ?: GlobalRecipients()).toResult()
}