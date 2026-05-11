package org.example.notifier.application.useCases.updateGlobalRecipientEmail

import org.example.notifier.application.model.globalRecipients.GlobalRecipientsResult
import org.example.notifier.application.model.globalRecipients.toResult
import org.example.notifier.application.service.core.GlobalRecipientsService
import org.springframework.stereotype.Component

@Component
class UpdateGlobalRecipientEmailUseCase(
    private val globalRecipientsService: GlobalRecipientsService
) {
    suspend fun execute(command: UpdateGlobalRecipientEmailCommand): GlobalRecipientsResult =
        globalRecipientsService.updateEmail(
            oldEmail = command.oldEmail,
            newEmail = command.newEmail,
            updatedBy = command.updatedBy
        ).toResult()
}