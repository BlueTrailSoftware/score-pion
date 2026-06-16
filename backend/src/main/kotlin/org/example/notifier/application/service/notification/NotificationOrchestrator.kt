package org.example.notifier.application.service.notification

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.Dispatchers
import org.example.notifier.domain.port.GlobalRecipientsRepository
import org.example.notifier.infrastructure.logging.LoggerPort
import org.example.notifier.domain.shared.AssessmentReport
import org.example.notifier.domain.shared.EmailEvent
import org.example.notifier.domain.position.OpenPosition
import org.example.notifier.infrastructure.external.factory.EmailTemplateFactory
import org.example.notifier.application.service.EmailNotificationService
import org.example.notifier.application.service.GoogleChatNotificationService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

/**
 * Central orchestrator for all notifications in the system.
 * Handles email and Google Chat notifications with global recipients support.
 */
@Service
class NotificationOrchestrator(
    private val emailNotificationService: EmailNotificationService,
    private val googleChatNotificationService: GoogleChatNotificationService,
    private val emailTemplateFactory: EmailTemplateFactory,
    private val globalRecipientsRepository: GlobalRecipientsRepository,
    private val logger: LoggerPort,
    @Value("\${frontend.url}") private val frontendUrl: String
) {

    // ========== GENERIC EMAIL SENDER ==========

    /**
     * Generic method to send an email based on an EmailEvent.
     * This simplifies sending direct emails without needing specific methods.
     *
     * @param recipientEmail The recipient's email address
     * @param event The event that triggered this email
     */
    suspend fun sendEmail(recipientEmail: String, event: EmailEvent) {
        try {
            val emailTemplate = emailTemplateFactory.createEmail(recipientEmail, event)
            emailNotificationService.sendEmail(emailTemplate)
            logger.info("Email sent to: {} for event: {}", recipientEmail, event::class.simpleName)
        } catch (e: Exception) {
            logger.error("Failed to send email to {}: {}", recipientEmail, e.message, e)
        }
    }

    // ========== SPECIFIC NOTIFICATION METHODS ==========

    /**
     * Notifies about recruiter invitation.
     * Uses the unified EmailEvent pattern.
     */
    suspend fun notifyRecruiterInvitation(
        recipientEmail: String,
        adminName: String? = null
    ) {
        val inviteLink = "$frontendUrl/login"

        val event = EmailEvent.RecruiterInvited(
            recruiterEmail = recipientEmail,
            adminName = adminName,
            inviteLink = inviteLink
        )

        coroutineScope {
            val jobs = listOf(
                async { sendEmail(recipientEmail, event) },
                async { notifyGlobalRecipients(event) }
            )
            jobs.awaitAll()
        }
    }

    /**
     * Notifies about admin invitation.
     */
    suspend fun notifyAdminInvitation(
        recipientEmail: String,
        invitedBy: String? = null
    ) {
        val inviteLink = "$frontendUrl/login"

        val event = EmailEvent.AdminInvited(
            adminEmail = recipientEmail,
            invitedBy = invitedBy,
            inviteLink = inviteLink
        )

        coroutineScope {
            val jobs = listOf(
                async { sendEmail(recipientEmail, event) },
                async { notifyGlobalRecipients(event) }
            )
            jobs.awaitAll()
        }
    }

    /**
     * Notifies candidate when they apply to a position
     */
    suspend fun notifyCandidateApplication(
        candidateEmail: String,
        candidateName: String,
        positionTitle: String,
        recruiterName: String? = null
    ) {
        try {
            logger.info("Sending application confirmation email to: {}", candidateEmail)

            coroutineScope {
                val jobs = listOf(
                    async {
                        val emailTemplate = emailTemplateFactory.createCandidateApplicationConfirmationEmail(
                            to = candidateEmail,
                            candidateName = candidateName,
                            positionTitle = positionTitle
                        )
                        emailNotificationService.sendEmail(emailTemplate)
                        logger.info("Application confirmation email sent to: {}", candidateEmail)
                    },
                    async {
                        notifyGlobalRecipients(
                            EmailEvent.CandidateApplication(
                                candidateEmail = candidateEmail,
                                candidateName = candidateName,
                                positionTitle = positionTitle,
                                recruiterName = recruiterName
                            )
                        )
                    }
                )
                jobs.awaitAll()
            }

        } catch (e: Exception) {
            logger.error("Failed to send application confirmation to {}: {}", candidateEmail, e.message, e)
        }
    }

    /**
     * Notifies about candidate invitation (Google Chat + Global Recipients)
     */
    suspend fun notifyCandidateInvitation(
        candidateEmail: String,
        candidateName: String,
        positionTitle: String,
        recruiterName: String,
        assessmentsCount: Int
    ) = coroutineScope {
        val jobs = listOf(
            async { notifyGoogleChatCandidateInvitation(candidateEmail, candidateName, positionTitle, recruiterName, assessmentsCount) },
            async { notifyGlobalRecipientsCandidateInvitation(candidateEmail, candidateName, positionTitle, recruiterName, assessmentsCount) }
        )

        jobs.awaitAll()
    }

    /**
     * Notifies recruiter when they invite a candidate
     */
    suspend fun notifyRecruiterCandidateInvitation(
        recruiterEmail: String,
        recruiterName: String,
        candidateName: String,
        candidateEmail: String,
        positionTitle: String,
        assessmentsCount: Int
    ) {
        try {
            logger.info("Sending invitation confirmation email to recruiter: {}", recruiterEmail)

            val emailTemplate = emailTemplateFactory.createRecruiterCandidateInvitationConfirmationEmail(
                to = recruiterEmail,
                recruiterName = recruiterName,
                candidateName = candidateName,
                candidateEmail = candidateEmail,
                positionTitle = positionTitle,
                assessmentsCount = assessmentsCount
            )

            emailNotificationService.sendEmail(emailTemplate)
            logger.info("Invitation confirmation email sent to recruiter: {}", recruiterEmail)

        } catch (e: Exception) {
            logger.error("Failed to send invitation confirmation to recruiter {}: {}", recruiterEmail, e.message, e)
        }
    }

    /**
     * Notifies when assessment is started
     */
    suspend fun notifyAssessmentStarted(
        candidateEmail: String,
        assessmentId: String,
        recruiterName: String? = null
    ) {
        try {
            logger.info("Sending assessment started notification - candidate: {}", candidateEmail)

            coroutineScope {
                val jobs = listOf(
                    async {
                        googleChatNotificationService.sendAssessmentJoinedNotification(
                            candidateEmail = candidateEmail,
                            assessmentId = assessmentId
                        )
                    },
                    async {
                        notifyGlobalRecipients(
                            EmailEvent.AssessmentStarted(
                                candidateEmail = candidateEmail,
                                assessmentId = assessmentId,
                                recruiterName = recruiterName
                            )
                        )
                    }
                )
                jobs.awaitAll()
            }
        } catch (e: Exception) {
            logger.error("Failed to send assessment started notification: {}", e.message, e)
        }
    }

    suspend fun notifyPositionAssignment(
        recruiterEmail: String,
        recruiterName: String,
        positions: List<OpenPosition>
    ) {
        try {
            val inviteLink = "$frontendUrl/login"

            if (recruiterEmail.isBlank()) {
                logger.error("Cannot send position assignment: Recruiter email is empty")
                return
            }

            coroutineScope {
                val jobs = mutableListOf<Deferred<Unit>>()

                jobs.add(async {
                    // Recruiter email
                    val event = EmailEvent.PositionAssigned(
                        recruiterName = recruiterName,
                        positions = positions,
                        inviteLink = inviteLink
                    )
                    sendEmail(recruiterEmail, event)
                })

                jobs.add(async {
                    // Global recipients
                    notifyGlobalRecipients(
                        EmailEvent.PositionAssigned(
                            recruiterName = recruiterName,
                            positions = positions,
                            inviteLink = inviteLink
                        )
                    )
                })

                jobs.awaitAll()
                logger.info("Position assignment email sent to global recipients: {}", recruiterEmail)
            }
        } catch (e: Exception) {
            logger.error("Failed to send position assignment notification",  e)
        }
    }

    suspend fun notifyPositionCreated(
        createdBy: String,
        position: OpenPosition,
        assessmentNames: List<String> = emptyList()
    ) {
        try {
            val positionLink = "$frontendUrl/positions/${position.id}"

            coroutineScope {
                val jobs = mutableListOf<Deferred<Unit>>()

                jobs.add(async {
                    notifyGoogleChatPositionCreated(
                        createdBy = createdBy,
                        position = position,
                        positionLink = positionLink,
                        assessmentNames = assessmentNames
                    )
                })

                jobs.add(async {
                    notifyGlobalRecipients(
                        EmailEvent.PositionCreated(
                            positionTitle = position.title,
                            positionDescription = position.description,
                            createdBy = createdBy,
                            positionLink = positionLink,
                            isExternal = position.external,
                            assessmentNames = assessmentNames
                        )
                    )
                })

                jobs.awaitAll()
            }

        } catch (e: Exception) {
            logger.error("Failed to send position creation notifications", e)
        }
    }

    private suspend fun notifyGoogleChatPositionCreated(
        createdBy: String,
        position: OpenPosition,
        positionLink: String,
        assessmentNames: List<String>
    ) {
        try {
            googleChatNotificationService.sendPositionCreatedNotification(
                createdBy = createdBy,
                positionTitle = position.title,
                positionDescription = position.description,
                positionLink = positionLink,
                isExternal = position.external,
                assessmentNames = assessmentNames
            )
        } catch (e: Exception) {
            logger.error("Failed to send Google Chat position creation notification: {}", e.message, e)
        }
    }

    /**
     * Notifies about assessment completion with full report
     */
    suspend fun notifyAssessmentCompletedWithReport(
        candidateEmail: String,
        candidateName: String?,
        report: AssessmentReport,
        recruiterEmail: String?,
        recruiterName: String?,
        positionTitle: String?,
        timeExpired: Boolean
    ) = coroutineScope {
        val jobs = mutableListOf(
            async { notifyGoogleChatAssessmentReport(candidateEmail, recruiterName, report, timeExpired) },
            async { notifyGlobalRecipientsAssessmentCompleted(candidateEmail, candidateName, report, positionTitle, timeExpired, recruiterName) }
        )

        if (recruiterEmail != null) {
            jobs.add(async {
                notifyRecruiterAssessmentReport(
                    candidateEmail,
                    report,
                    recruiterEmail,
                    recruiterName,
                    candidateName
                )
            })
        }

        jobs.awaitAll()
    }

    /**
     * Notifies about assessment completion without report
     */
    suspend fun notifyAssessmentCompletedWithoutReport(
        candidateEmail: String,
        candidateName: String?,
        assessmentId: String,
        recruiterEmail: String?,
        recruiterName: String?,
        positionTitle: String?,
        timeExpired: Boolean
    ) = coroutineScope {
        logger.info("Sending notification without report - candidate: {}, timeExpired: {}", candidateEmail, timeExpired)

        val jobs = mutableListOf(
            async { notifyGoogleChatAssessmentPending(candidateEmail, recruiterName, assessmentId, timeExpired) },
            async { notifyGlobalRecipientsAssessmentPending(candidateEmail, candidateName, assessmentId, positionTitle, timeExpired, recruiterName) }
        )

        if (recruiterEmail != null) {
            jobs.add(async {
                notifyRecruiterAssessmentPending(
                    candidateEmail,
                    assessmentId,
                    recruiterEmail,
                    recruiterName,
                    timeExpired
                )
            })
        }

        jobs.awaitAll()
    }

    private suspend fun notifyRecruiterAssessmentReport(
        candidateEmail: String,
        report: AssessmentReport,
        recruiterEmail: String,
        recruiterName: String?,
        candidateName: String?
    ) {
        try {
            if (recruiterEmail.isBlank()) {
                logger.error("Cannot send assessment report: Recruiter email is empty")
                return
            }

            val emailTemplate = emailTemplateFactory.createCandidateReportEmail(
                to = recruiterEmail,
                recruiterName = recruiterName,
                candidateEmail = candidateEmail,
                candidateName = candidateName,
                report = report
            )

            emailNotificationService.sendEmail(emailTemplate)
            logger.info("Assessment report email sent to recruiter: {}", recruiterEmail)

        } catch (e: Exception) {
            logger.error("Failed to send assessment report to recruiter email: {}", recruiterEmail, e)
        }

        val event = EmailEvent.CandidateReport(
            recruiterEmail = recruiterEmail,
            recruiterName = recruiterName,
            candidateEmail = candidateEmail,
            candidateName = candidateName,
            report = report
        )

        sendEmail(recruiterEmail, event)
    }

    private suspend fun notifyRecruiterAssessmentPending(
        candidateEmail: String,
        assessmentId: String,
        recruiterEmail: String,
        recruiterName: String?,
        timeExpired: Boolean
    ) {
        if (recruiterEmail.isBlank()) {
            logger.error("Cannot send assessment pending: Recruiter email is empty")
            return
        }

        val event = EmailEvent.CandidateAssessmentPending(
            recruiterEmail = recruiterEmail,
            recruiterName = recruiterName,
            candidateEmail = candidateEmail,
            candidateName = null,
            assessmentId = assessmentId,
            timeExpired = timeExpired
        )

        sendEmail(recruiterEmail, event)
    }


    private suspend fun notifyGoogleChatCandidateInvitation(
        candidateEmail: String,
        candidateName: String,
        positionTitle: String,
        recruiterName: String,
        assessmentsCount: Int
    ) {
        try {
            googleChatNotificationService.sendCandidateInvitationNotification(
                candidateEmail = candidateEmail,
                candidateName = candidateName,
                positionTitle = positionTitle,
                recruiterName = recruiterName,
                assessmentsCount = assessmentsCount
            )
        } catch (e: Exception) {
            logger.error("Failed to send Google Chat candidate invitation: {}", e.message, e)
        }
    }

    private suspend fun notifyGlobalRecipientsCandidateInvitation(
        candidateEmail: String,
        candidateName: String,
        positionTitle: String,
        recruiterName: String,
        assessmentsCount: Int
    ) {
        notifyGlobalRecipients(
            EmailEvent.CandidateInvited(
                candidateEmail = candidateEmail,
                candidateName = candidateName,
                positionTitle = positionTitle,
                recruiterName = recruiterName,
                assessmentsCount = assessmentsCount
            )
        )
    }

    private suspend fun notifyGoogleChatAssessmentReport(
        candidateEmail: String,
        recruiterName: String?,
        report: AssessmentReport,
        timeExpired: Boolean
    ) {
        try {
            googleChatNotificationService.sendAssessmentWithReportNotification(
                candidateEmail = candidateEmail,
                recruiterName = recruiterName,
                report = report,
                timeExpired = timeExpired
            )
        } catch (e: Exception) {
            logger.error("Failed to send Google Chat assessment report: {}", e.message, e)
        }
    }


    private suspend fun notifyGoogleChatAssessmentPending(
        candidateEmail: String,
        recruiterName: String?,
        assessmentId: String,
        timeExpired: Boolean
    ) {
        try {
            googleChatNotificationService.sendAssessmentWithoutReportNotification(
                candidateEmail = candidateEmail,
                recruiterName = recruiterName,
                assessmentId = assessmentId,
                timeExpired = timeExpired
            )
        } catch (e: Exception) {
            logger.error("Failed to send Google Chat assessment pending notification: {}", e.message, e)
        }
    }

    private suspend fun notifyGlobalRecipientsAssessmentCompleted(
        candidateEmail: String,
        candidateName: String?,
        report: AssessmentReport,
        positionTitle: String?,
        timeExpired: Boolean,
        recruiterName: String?
    ) {
        notifyGlobalRecipients(
            EmailEvent.AssessmentCompleted(
                candidateEmail = candidateEmail,
                candidateName = candidateName,
                report = report,
                positionTitle = positionTitle,
                timeExpired = timeExpired,
                recruiterName = recruiterName
            )
        )
    }

    private suspend fun notifyGlobalRecipientsAssessmentPending(
        candidateEmail: String,
        candidateName: String?,
        assessmentId: String,
        positionTitle: String?,
        timeExpired: Boolean,
        recruiterName: String?
    ) {
        notifyGlobalRecipients(
            EmailEvent.AssessmentPending(
                candidateEmail = candidateEmail,
                candidateName = candidateName,
                assessmentId = assessmentId,
                positionTitle = positionTitle,
                timeExpired = timeExpired,
                recruiterName = recruiterName
            )
        )
    }

    suspend fun notifyDataErasureRequest(email: String, token: String) {
        try {
            val verificationLink = "$frontendUrl/privacy/erasures/$token"
            val emailTemplate = emailTemplateFactory.createDataDeletionVerificationEmail(
                to = email,
                verificationLink = verificationLink
            )
            emailNotificationService.sendEmail(emailTemplate)
            logger.info("Data erasure verification email sent to: {}", email)
        } catch (e: Exception) {
            logger.error("Failed to send data erasure verification email to {}: {}", email, e.message, e)
        }
    }

    suspend fun notifyDataErasureConfirmed(email: String) {
        try {
            val emailTemplate = emailTemplateFactory.createDataDeletionConfirmationEmail(email)
            emailNotificationService.sendEmail(emailTemplate)
            logger.info("Data erasure confirmation email sent to: {}", email)
        } catch (e: Exception) {
            logger.error("Failed to send data erasure confirmation email to {}: {}", email, e.message, e)
        }
    }

    suspend fun notifyDataExportRequest(email: String, token: String) {
        try {
            val downloadLink = "$frontendUrl/privacy/exports/$token"
            val emailTemplate = emailTemplateFactory.createDataDownloadVerificationEmail(
                to = email,
                downloadLink = downloadLink
            )
            emailNotificationService.sendEmail(emailTemplate)
            logger.info("Data export verification email sent to: {}", email)
        } catch (e: Exception) {
            logger.error("Failed to send data export verification email to {}: {}", email, e.message, e)
        }
    }

    /**
     * Overloaded version that accepts EmailEvent.
     * This is the new preferred way to notify global recipients.
     */
    private suspend fun notifyGlobalRecipients(event: EmailEvent) {
        try {
            val emails = globalRecipientsRepository.getAllEmails()

            if (emails.isEmpty()) {
                logger.debug("No global recipients configured for event: {}", event::class.simpleName)
                return
            }

            logger.info("Notifying {} global recipient(s) about {}", emails.size, event::class.simpleName)

            coroutineScope {
                emails.map { recipientEmail ->
                    async(Dispatchers.IO) {
                        sendEmailToGlobalRecipient(recipientEmail, event)
                    }
                }.awaitAll()
            }

        } catch (e: Exception) {
            logger.error("Failed to notify global recipients about {}: {}", event::class.simpleName, e.message, e)
        }
    }

    private suspend fun sendEmailToGlobalRecipient(recipientEmail: String, event: EmailEvent) {
        try {
            val emailTemplate = emailTemplateFactory.createEmailForGlobalRecipient(recipientEmail, event)
            emailNotificationService.sendEmail(emailTemplate)
            logger.info("Global recipient email sent to: {} for event: {}", recipientEmail, event::class.simpleName)
        } catch (e: Exception) {
            logger.error("Failed to send global recipient email to {}: {}", recipientEmail, e.message, e)
        }
    }

    /**
     * Notifies about applicant approval.
     * Uses the new EmailEvent pattern for cleaner code.
     */
    suspend fun notifyApplicantApproval(
        applicantEmail: String,
        applicantName: String? = null,
        positionTitle: String,
        reviewedBy: String? = null
    ) {
        try {
            val event = EmailEvent.ApplicantApproved(
                applicantEmail = applicantEmail,
                applicantName = applicantName,
                positionTitle = positionTitle,
                recruiterName = reviewedBy
            )

            coroutineScope {
                val jobs = listOf(
                    async { sendEmail(applicantEmail, event) },
                    async {
                        googleChatNotificationService.sendApplicationApprovedNotification(
                            candidateEmail = applicantEmail,
                            candidateName = applicantName ?: applicantEmail,
                            positionTitle = positionTitle,
                            reviewedBy = reviewedBy ?: "System"
                        )
                    },
                    async { notifyGlobalRecipients(event) }
                )
                jobs.awaitAll()
            }
        } catch (e: Exception) {
            logger.error("Failed to send applicant approval notifications: {}", e.message, e)
        }
    }

    /**
     * Notifies about applicant rejection.
     */
    suspend fun notifyApplicantRejection(
        applicantEmail: String,
        applicantName: String? = null,
        positionTitle: String,
        reviewedBy: String? = null,
        statusNote: String? = null
    ) {
        try {
            val event = EmailEvent.ApplicantRejected(
                applicantEmail = applicantEmail,
                applicantName = applicantName,
                positionTitle = positionTitle,
                recruiterName = reviewedBy
            )

            coroutineScope {
                val jobs = listOf(
                    async { sendEmail(applicantEmail, event) },
                    async {
                        googleChatNotificationService.sendApplicationRejectedNotification(
                            candidateEmail = applicantEmail,
                            candidateName = applicantName ?: applicantEmail,
                            positionTitle = positionTitle,
                            reviewedBy = reviewedBy ?: "System",
                            statusNote = statusNote
                        )
                    },
                    async { notifyGlobalRecipients(event) }
                )
                jobs.awaitAll()
            }
        } catch (e: Exception) {
            logger.error("Failed to send applicant rejection notifications: {}", e.message, e)
        }
    }

}