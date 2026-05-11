package org.example.notifier.application.useCases.submitApplication

import java.time.LocalDateTime
import java.util.UUID
import org.example.notifier.application.model.applicant.ApplicantItem
import org.example.notifier.application.model.applicant.toApplicantItem
import org.example.notifier.application.service.core.ApplicantService
import org.example.notifier.application.service.core.InvitationService
import org.example.notifier.application.service.core.OpenPositionService
import org.example.notifier.application.service.file.FileService
import org.example.notifier.application.service.integration.CaptchaService
import org.example.notifier.application.service.notification.NotificationOrchestrator
import org.example.notifier.domain.applicant.Applicant
import org.example.notifier.domain.applicant.ApplicantStatus
import org.example.notifier.infrastructure.logging.LoggerPort
import org.example.notifier.application.util.isValidEmail
import org.example.notifier.application.util.isValidLinkedInUrl
import org.example.notifier.application.util.isValidPhone
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class SubmitApplicationUseCase(
    private val captchaService: CaptchaService,
    private val applicantService: ApplicantService,
    private val invitationService: InvitationService,
    private val openPositionService: OpenPositionService,
    private val fileService: FileService,
    private val notificationOrchestrator: NotificationOrchestrator,
    private val logger: LoggerPort,
    @Value("\${applicant.data.retention.months}") private val dataRetentionMonths: Long
) {

    private data class SanitizedApplication(
        val name: String,
        val email: String,
        val phone: String?,
        val linkedinUrl: String?
    )

    suspend fun execute(command: SubmitApplicationCommand): ApplicantItem {
        val sanitized = validateAndSanitize(command)

        val position = openPositionService.getPosition(command.positionId)
            ?: throw IllegalArgumentException("Position not found with id: ${command.positionId}")

        if (!position.isActive) {
            throw IllegalArgumentException("Position ${command.positionId} is not currently accepting applications")
        }

        if (applicantService.findByEmailAndPositionId(sanitized.email, command.positionId) != null) {
            throw IllegalArgumentException("You have already applied to this position")
        }

        if (invitationService.existsForCandidateAndPosition(sanitized.email, command.positionId)) {
            throw IllegalArgumentException("You have already been invited to this position by a recruiter")
        }

        val applicantId = UUID.randomUUID().toString()
        logger.info("Processing application for position ${command.positionId} from ${sanitized.email}")

        val fileUrl = command.filePart?.let {
            val url = fileService.upload(it, "applicant", applicantId)
            logger.info("Applicant file saved in: $url")
            url
        }

        val applicant = Applicant(
            id = applicantId,
            name = sanitized.name,
            email = sanitized.email,
            phone = sanitized.phone,
            positionId = command.positionId,
            fileUrl = fileUrl,
            linkedinUrl = sanitized.linkedinUrl,
            status = ApplicantStatus.PENDING,
            gdprConsent = command.gdprConsent,
            gdprConsentDate = LocalDateTime.now().takeIf { command.gdprConsent },
            deleteAfter = LocalDateTime.now().plusMonths(dataRetentionMonths)
        )

        val savedApplicant = applicantService.createApplicant(applicant)
        logger.info("Application created with ID: ${savedApplicant.id}")

        try {
            notificationOrchestrator.notifyCandidateApplication(
                candidateEmail = savedApplicant.email,
                candidateName = savedApplicant.name,
                positionTitle = position.title
            )
        } catch (e: Exception) {
            logger.error("Failed to send application confirmation email: ${e.message}", e)
        }

        return savedApplicant.toApplicantItem(position.title)
    }

    private suspend fun validateAndSanitize(command: SubmitApplicationCommand): SanitizedApplication {
        if (!captchaService.validateToken(command.captchaToken, "apply")) {
            throw IllegalArgumentException("Security validation failed. Please try again.")
        }

        if (command.filePart == null && command.linkedinUrl.isNullOrBlank()) {
            throw IllegalArgumentException("Please provide either a CV file or a LinkedIn profile URL")
        }

        if (command.name.isBlank()) {
            throw IllegalArgumentException("Name cannot be empty")
        }

        if (!command.email.isValidEmail()) {
            throw IllegalArgumentException("Invalid email format: ${command.email}")
        }

        val normalizedPhone = command.phone?.trim()?.takeIf { it.isNotBlank() }
        if (normalizedPhone != null && !normalizedPhone.isValidPhone()) {
            throw IllegalArgumentException("Invalid phone format: $normalizedPhone")
        }

        if (!command.linkedinUrl.isNullOrBlank() && !command.linkedinUrl.isValidLinkedInUrl()) {
            throw IllegalArgumentException("Invalid LinkedIn profile URL format")
        }

        return SanitizedApplication(
            name = command.name.trim(),
            email = command.email.trim().lowercase(),
            phone = normalizedPhone,
            linkedinUrl = command.linkedinUrl?.trim()
        )
    }
}