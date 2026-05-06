package org.example.notifier.application.useCases.confirmDataErasure

import org.example.notifier.application.service.core.ApplicantService
import org.example.notifier.application.service.notification.NotificationOrchestrator
import org.example.notifier.application.service.security.PrivacyTokenService
import org.example.notifier.domain.applicant.ApplicantStatus
import org.example.notifier.infrastructure.logging.LoggerPort
import org.springframework.stereotype.Component

@Component
class ConfirmDataErasureUseCase(
    private val privacyTokenService: PrivacyTokenService,
    private val applicantService: ApplicantService,
    private val notificationOrchestrator: NotificationOrchestrator,
    private val logger: LoggerPort
) {

    suspend fun execute(command: ConfirmDataErasureCommand): ConfirmDataErasureResult {
        val email = privacyTokenService.validateDeletionToken(command.token)
            ?: return ConfirmDataErasureResult.InvalidToken

        val applicants = applicantService.getApplicantsByEmail(email)

        if (applicants.isEmpty()) {
            return ConfirmDataErasureResult.NotFound
        }

        if (applicants.all { it.status == ApplicantStatus.ANONYMIZED }) {
            return ConfirmDataErasureResult.AlreadyAnonymized
        }

        val count = applicantService.anonymizeApplicantsByEmail(email)

        if (count > 0) {
            notificationOrchestrator.notifyDataErasureConfirmed(email)
        }

        return ConfirmDataErasureResult.Anonymized(count)
    }
}
