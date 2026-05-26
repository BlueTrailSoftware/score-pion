package org.example.notifier.application.useCases.requestDataExport

import org.example.notifier.application.service.core.ApplicantService
import org.example.notifier.application.service.integration.CaptchaService
import org.example.notifier.application.service.security.PrivacyTokenService
import org.example.notifier.domain.event.DataExportRequestedEvent
import org.example.notifier.infrastructure.logging.LoggerPort
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class RequestDataExportUseCase(
    private val captchaService: CaptchaService,
    private val applicantService: ApplicantService,
    private val privacyTokenService: PrivacyTokenService,
    private val eventPublisher: ApplicationEventPublisher,
    private val logger: LoggerPort
) {

    suspend fun execute(command: RequestDataExportCommand): RequestDataExportResult {
        logger.info("Export request received for email: {}", command.email)

        if (!captchaService.validateToken(command.captchaToken, "download_data")) {
            logger.warn("reCAPTCHA validation failed for data download request from {}", command.email)
            throw IllegalArgumentException("Security validation failed. Please try again.")
        }

        val applicants = applicantService.getApplicantsByEmail(command.email)

        if (applicants.isEmpty()) {
            return RequestDataExportResult.NoApplicantFound
        }

        val token = privacyTokenService.generateDownloadToken(command.email)
        eventPublisher.publishEvent(DataExportRequestedEvent(email = command.email, token = token))

        return RequestDataExportResult.DownloadEmailSent
    }
}
