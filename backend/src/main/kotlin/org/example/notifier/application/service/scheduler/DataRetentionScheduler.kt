package org.example.notifier.application.service.scheduler

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.Dispatchers
import org.example.notifier.application.service.core.ApplicantService
import org.example.notifier.domain.applicant.ApplicantStatus
import org.example.notifier.domain.port.ApplicantRepository
import org.example.notifier.infrastructure.logging.LoggerPort
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/**
 * Automatic data retention scheduler for GDPR compliance.
 * Anonymizes applicant data and deletes associated files that have passed retention period.
 *
 * Can be disabled by setting: data.retention.scheduler.enabled=false
 */
@Service
@ConditionalOnProperty(
    prefix = "data.retention.scheduler",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = true
)
class DataRetentionScheduler(
    private val applicantRepository: ApplicantRepository,
    private val applicantService: ApplicantService,
    private val logger: LoggerPort
) {

    /**
     * Runs daily at 2 AM to clean up expired applicant data.
     * Cron expression can be configured via: data.retention.scheduler.cron
     */
    @Scheduled(cron = "\${data.retention.scheduler.cron:0 0 2 * * *}")
    fun cleanupExpiredData() = runBlocking {
        logger.info("Starting automatic data retention cleanup job")

        try {
            val now = LocalDateTime.now()
            val allApplicants = applicantRepository.findAll()

            val expiredApplicants = allApplicants.filter { applicant ->
                applicant.deleteAfter.isBefore(now) &&
                applicant.status != ApplicantStatus.ANONYMIZED
            }

            if (expiredApplicants.isEmpty()) {
                logger.info("No expired applicants found")
                return@runBlocking
            }

            logger.info("Found ${expiredApplicants.size} expired applicant(s) to anonymize")

            coroutineScope {
                val results = expiredApplicants.map { applicant ->
                    async(Dispatchers.IO) {
                        applicantService.anonymizeSingleApplicant(applicant)
                    }
                }.awaitAll()

                val successCount = results.count { it.success }
                val errorCount = results.count { !it.success }
                val filesDeletedCount = results.count { it.fileDeleted }

                logger.info("Data retention cleanup completed: $successCount anonymized, $filesDeletedCount files deleted, $errorCount errors")
            }

        } catch (e: Exception) {
            logger.error("Data retention cleanup job failed: ${e.message}", e)
        }
    }
}
