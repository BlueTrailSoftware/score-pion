package org.example.notifier.application.useCases.addGlobalRecipientEmail

import org.example.notifier.application.model.globalRecipients.GlobalRecipientsResult
import org.example.notifier.application.model.globalRecipients.toResult
import org.example.notifier.application.service.core.GlobalRecipientsService
import org.springframework.stereotype.Component

@Component
class AddGlobalRecipientEmailUseCase(
    private val globalRecipientsService: GlobalRecipientsService
) {
    suspend fun execute(command: AddGlobalRecipientEmailCommand): GlobalRecipientsResult =
        globalRecipientsService.addEmail(command.email, command.updatedBy).toResult()
}