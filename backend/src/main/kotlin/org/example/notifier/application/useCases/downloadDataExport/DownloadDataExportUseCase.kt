package org.example.notifier.application.useCases.downloadDataExport

import java.time.LocalDate
import org.example.notifier.application.service.core.ApplicantService
import org.example.notifier.application.service.security.PrivacyTokenService
import org.example.notifier.domain.applicant.ApplicantStatus
import org.example.notifier.infrastructure.logging.LoggerPort
import org.springframework.stereotype.Component

@Component
class DownloadDataExportUseCase(
    private val privacyTokenService: PrivacyTokenService,
    private val applicantService: ApplicantService,
    private val logger: LoggerPort
) {

    suspend fun execute(command: DownloadDataExportCommand): DownloadDataExportResult {
        val email = privacyTokenService.validateDownloadToken(command.token)
            ?: return DownloadDataExportResult.InvalidToken

        val applicants = applicantService.getApplicantsByEmail(email)

        if (applicants.isEmpty() || applicants.all { it.status == ApplicantStatus.ANONYMIZED }) {
            logger.warn("Download attempt for deleted/anonymized data: {}", email)
            return DownloadDataExportResult.DataNotAvailable
        }

        val data = applicantService.exportApplicantData(email)
        val filename = "my_data_${LocalDate.now()}.json"

        return DownloadDataExportResult.Success(data, filename)
    }
}
