package org.example.notifier.application.useCases.removeGlobalRecipientEmail

import org.example.notifier.application.model.globalRecipients.GlobalRecipientsResult
import org.example.notifier.application.model.globalRecipients.toResult
import org.example.notifier.application.service.core.GlobalRecipientsService
import org.springframework.stereotype.Component

@Component
class RemoveGlobalRecipientEmailUseCase(
    private val globalRecipientsService: GlobalRecipientsService
) {
    suspend fun execute(command: RemoveGlobalRecipientEmailCommand): GlobalRecipientsResult =
        globalRecipientsService.removeEmail(command.email, command.updatedBy).toResult()
}