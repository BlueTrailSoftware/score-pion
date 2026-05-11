package org.example.notifier.application.service.core.impl

import java.time.LocalDateTime
import org.example.notifier.domain.port.ApplicantRepository
import org.example.notifier.domain.applicant.Applicant
import org.springframework.stereotype.Service
import org.example.notifier.application.service.core.OpenPositionService
import org.example.notifier.application.service.core.ApplicantService
import org.example.notifier.application.service.core.InvitationService
import org.example.notifier.domain.applicant.AnonymizationResult
import org.example.notifier.application.service.file.FileService
import org.example.notifier.domain.user.User
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.Dispatchers
import org.example.notifier.domain.applicant.ApplicantStatus
import org.example.notifier.domain.applicant.CandidatePositionKey
import org.example.notifier.domain.shared.SystemConstants
import org.example.notifier.infrastructure.logging.LoggerPort

@Service
class ApplicantServiceImpl(
    private val applicantRepository: ApplicantRepository,
    private val openPositionService: OpenPositionService,
    private val fileService: FileService,
    private val logger: LoggerPort
) : ApplicantService {

    companion object {

        private val VALID_STATUSES = setOf(
            ApplicantStatus.PENDING,
            ApplicantStatus.INVITED,
            ApplicantStatus.REJECTED
        )

        private val QUERYABLE_STATUSES = VALID_STATUSES + ApplicantStatus.ANONYMIZED

        private fun parseStatus(status: String): ApplicantStatus? {
            return when(status.lowercase()) {
                "pending" -> ApplicantStatus.PENDING
                "invited" -> ApplicantStatus.INVITED
                "rejected" -> ApplicantStatus.REJECTED
                "anonymized" -> ApplicantStatus.ANONYMIZED
                else -> null
            }
        }
    }

    override suspend fun findByEmailAndPositionId(email: String, positionId: String): Applicant? {
        return applicantRepository.findByEmailAndPositionId(email, positionId)
    }

    override suspend fun createApplicant(applicant: Applicant): Applicant {
        return applicantRepository.save(applicant)
    }

    /** Gets all applicants with optional filters */
    override suspend fun getAllApplicants(
            status: String?,
            positionId: String?,
            search: String?
    ): List<Applicant> {
        logger.info("Fetching applicants - status: $status, positionId: $positionId, search: $search")

        val applicants =
                when {
                    positionId != null -> applicantRepository.findByPositionId(positionId)
                    else -> applicantRepository.findAll()
                }

        return applyCommonFilters(applicants, status, search)
    }

    /** Gets applicants scoped to only those the recruiter personally invited */
    override suspend fun getApplicantsForRecruiter(
            invitedKeys: Set<CandidatePositionKey>,
            status: String?,
            positionId: String?,
            search: String?
    ): List<Applicant> {
        val allowedPositionIds = invitedKeys.map { it.positionId }.toSet()

        if (positionId != null && positionId !in allowedPositionIds) {
            throw org.springframework.security.access.AccessDeniedException(
                "Recruiter does not have access to position $positionId"
            )
        }

        val positionIdsToQuery = if (positionId != null) setOf(positionId) else allowedPositionIds

        val applicants = coroutineScope {
            positionIdsToQuery
                .map { pid -> async { applicantRepository.findByPositionId(pid) } }
                .awaitAll()
                .flatten()
                .filter { CandidatePositionKey(it.email, it.positionId) in invitedKeys }
        }

        return applyCommonFilters(applicants, status, search)
    }

    private suspend fun applyCommonFilters(
            applicants: List<Applicant>,
            status: String?,
            search: String?
    ): List<Applicant> {
        var result = applicants.filter { it.status != ApplicantStatus.ANONYMIZED }

        if (status != null) {
            val statusEnum = parseStatus(status)
            if (statusEnum != null && statusEnum in QUERYABLE_STATUSES) {
                result = result.filter { it.status == statusEnum }
            }
        }

        if (!search.isNullOrBlank()) {
            val searchLower = search.trim().lowercase()

            val uniquePositionIds = result.map { it.positionId }.distinct()
            val positionsMap = uniquePositionIds.associateWith { pid ->
                openPositionService.getPosition(pid)
            }

            result = result.filter { applicant ->
                val position = positionsMap[applicant.positionId]
                applicant.name.lowercase().contains(searchLower) ||
                applicant.email.lowercase().contains(searchLower) ||
                position?.title?.lowercase()?.contains(searchLower) == true
            }
        }

        return result
    }

    /** Gets a single applicant by ID */
    override suspend fun getApplicantById(id: String): Applicant? {
        return applicantRepository.findById(id)
    }

    /** Updates applicant status */
    override suspend fun updateApplicantStatus(
            id: String,
            newStatus: String,
            reviewedBy: User,
            statusNote: String?
    ): Applicant {
        logger.info("Updating applicant $id status to $newStatus by ${reviewedBy.id}")

        val statusEnum = parseStatus(newStatus)
            ?: throw IllegalArgumentException("Invalid status: $newStatus. Must be one of: ${VALID_STATUSES.joinToString()}")

        if (statusEnum !in VALID_STATUSES) {
            throw IllegalArgumentException(
                    "Invalid status: $newStatus. Cannot manually set status to ANONYMIZED"
            )
        }

        val applicant =
                applicantRepository.findById(id)
                        ?: throw IllegalArgumentException("Applicant not found with id: $id")

        val updatedApplicant =
                applicant.copy(
                        status = statusEnum,
                        reviewedBy = reviewedBy.id,
                        reviewedAt = LocalDateTime.now(),
                        updatedAt = LocalDateTime.now(),
                        statusNote = statusNote
                )

        val saved = applicantRepository.save(updatedApplicant)
        logger.info("Applicant $id status updated to $newStatus")
        return saved
    }

    /** Updates applicant details and file */
    override suspend fun updateApplicant(
        id: String,
        name: String?,
        email: String?,
        phone: String?,
        fileUrl: String?,
        isFileDeleted: Boolean
    ): Applicant {
        logger.info("Updating applicant $id")

        val applicant =
            applicantRepository.findById(id)
                ?: throw IllegalArgumentException("Applicant not found with id: $id")

        val updatedApplicant = applicant.copy(
            name = name ?: applicant.name,
            email = email ?: applicant.email,
            phone = phone ?: applicant.phone,
            fileUrl = fileUrl,
            isFileDeleted = isFileDeleted,
            updatedAt = LocalDateTime.now()
        )

        return applicantRepository.save(updatedApplicant)
    }

    override suspend fun getApplicantsByEmail(email: String): List<Applicant> {
        return applicantRepository.findByEmail(email)
    }

    override suspend fun anonymizeSingleApplicant(applicant: Applicant): AnonymizationResult {
        return try {
            var fileDeleted = false

            if (!applicant.fileUrl.isNullOrBlank()) {
                try {
                    fileService.hardDelete(applicant.fileUrl)
                    fileDeleted = true
                    logger.debug("Permanently deleted file for applicant ${applicant.id}")
                } catch (e: Exception) {
                    logger.warn("Failed to delete file for applicant ${applicant.id}: ${e.message}")
                }
            }

            val anonymized = applicant.copy(
                name = "DELETED_USER",
                email = "deleted-${applicant.id}@anonymized.local",
                phone = null,
                fileUrl = null,
                linkedinUrl = null,
                status = ApplicantStatus.ANONYMIZED,
                isFileDeleted = fileDeleted,
                updatedAt = LocalDateTime.now()
            )
            applicantRepository.save(anonymized)

            AnonymizationResult(
                applicantId = applicant.id,
                fileDeleted = fileDeleted,
                success = true
            )
        } catch (e: Exception) {
            logger.error("Failed to anonymize applicant ${applicant.id}: ${e.message}", e)
            AnonymizationResult(
                applicantId = applicant.id,
                fileDeleted = false,
                success = false,
                error = e.message
            )
        }
    }

    override suspend fun anonymizeApplicantsByEmail(email: String): Int = coroutineScope {
        logger.info("Anonymizing applicants for email: $email")
        val applicants = applicantRepository.findByEmail(email)

        if (applicants.isEmpty()) {
            return@coroutineScope 0
        }

        logger.info("Processing ${applicants.size} applicant(s) for anonymization")

        val results = applicants.map { applicant ->
            async(Dispatchers.IO) {
                anonymizeSingleApplicant(applicant)
            }
        }.awaitAll()

        val successCount = results.count { it.success }
        val filesDeletedCount = results.count { it.fileDeleted }
        logger.info("Anonymized $successCount applicant(s) for email: $email, deleted $filesDeletedCount file(s)")

        return@coroutineScope successCount
    }

    override suspend fun exportApplicantData(email: String): ByteArray {
        logger.info("Exporting data for email: $email")
        val applicants = applicantRepository.findByEmail(email)

        val jsonData = org.example.notifier.infrastructure.persistence.mapper.JacksonMapperUtil.objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(applicants)

        return jsonData.toByteArray()
    }

}
