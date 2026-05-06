package org.example.notifier.application.useCases.requestDataErasure

import org.example.notifier.application.service.core.ApplicantService
import org.example.notifier.application.service.integration.CaptchaService
import org.example.notifier.application.service.notification.NotificationOrchestrator
import org.example.notifier.application.service.security.PrivacyTokenService
import org.example.notifier.infrastructure.logging.LoggerPort
import org.springframework.stereotype.Component

@Component
class RequestDataErasureUseCase(
    private val captchaService: CaptchaService,
    private val applicantService: ApplicantService,
    private val privacyTokenService: PrivacyTokenService,
    private val notificationOrchestrator: NotificationOrchestrator,
    private val logger: LoggerPort
) {

    suspend fun execute(command: RequestDataErasureCommand): RequestDataErasureResult {
        logger.info("Erasure request received for email: {}", command.email)

        if (!captchaService.validateToken(command.captchaToken, "delete_data")) {
            logger.warn("reCAPTCHA validation failed for data deletion request from {}", command.email)
            throw IllegalArgumentException("Security validation failed. Please try again.")
        }

        val applicants = applicantService.getApplicantsByEmail(command.email)

        if (applicants.isEmpty()) {
            return RequestDataErasureResult.NoApplicantFound
        }

        val token = privacyTokenService.generateDeletionToken(command.email)
        notificationOrchestrator.notifyDataErasureRequest(command.email, token)

        return RequestDataErasureResult.VerificationEmailSent
    }
}
