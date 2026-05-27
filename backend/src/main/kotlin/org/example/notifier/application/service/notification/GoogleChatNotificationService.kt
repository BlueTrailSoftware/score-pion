package org.example.notifier.application.service

import org.example.notifier.infrastructure.client.GoogleChatClient
import org.example.notifier.domain.shared.AssessmentReport
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

private val logger = LoggerFactory.getLogger(GoogleChatNotificationService::class.java)

@Service
class GoogleChatNotificationService(
    @Value("\${gchat.internal.webhookUrl}")
    private val gchatInternalWebhookUrl: String,

    @Value("\${gchat.external.webhookUrl}")
    private val gchatExternalWebhookUrl: String,

    private val googleChatClient: GoogleChatClient
) {

    /**
     * Sends notification when assessment report is available to appropriate Google Chat channels
     */
    suspend fun sendAssessmentWithReportNotification(
        candidateEmail: String,
        report: AssessmentReport,
        timeExpired: Boolean,
        recruiterName: String?
    ) {
        val message = buildMessageWithReport(candidateEmail, recruiterName, report, timeExpired)

        // Always send to internal channel
        sendMessage(gchatInternalWebhookUrl, message, candidateEmail)

        // Send to external channel if workspace is "External"
        if (report.workspaces?.contains("External") == true) {
            sendMessage(gchatExternalWebhookUrl, message, candidateEmail)
        }
    }

    /**
     * Sends notification when candidate join to an assessment
     * Note: Only sends to internal channel (no workspace information available)
     */
    suspend fun sendAssessmentJoinedNotification(
        candidateEmail: String,
        assessmentId: String,
    ) {
        val message = buildAssessmentStartedMessage(candidateEmail, assessmentId)

        // Always send to internal channel
        sendMessage(gchatInternalWebhookUrl, message, candidateEmail)
    }


    /**
     * Sends notification when report is still being processed
     * Note: Only sends to internal channel (no workspace information available)
     */
    suspend fun sendAssessmentWithoutReportNotification(
        candidateEmail: String,
        assessmentId: String,
        timeExpired: Boolean,
        recruiterName: String?
    ) {
        val message = buildMessageWithoutReport(candidateEmail, recruiterName, assessmentId, timeExpired)

        // Always send to internal channel
        sendMessage(gchatInternalWebhookUrl, message, candidateEmail)
    }

    private suspend fun sendMessage(webhookUrl: String, message: String, email: String) {
        try {
            googleChatClient.sendMessage(webhookUrl, message)
            logger.info("Google Chat notification sent for candidate: {}", email)
        } catch (e: Exception) {
            logger.error("Google Chat notification error: {}", e.message, e)
        }
    }

    /**
     * Sends position creation notification to internal Google Chat
     */
    suspend fun sendPositionCreatedNotification(
        createdBy: String,
        positionTitle: String,
        positionDescription: String,
        positionLink: String,
        isExternal: Boolean,
        assessmentNames: List<String>
    ) {
        val message = buildPositionCreationMessage(
            createdBy,
            positionTitle,
            positionDescription,
            positionLink,
            isExternal,
            assessmentNames
        )

        try {
            googleChatClient.sendMessage(gchatInternalWebhookUrl, message)
            logger.info("Position creation notification sent for position: {}", positionTitle)
        } catch (e: Exception) {
            logger.error("Failed to send position creation notification: {}", e.message, e)
        }
    }

    private fun buildPositionCreationMessage(
        createdBy: String,
        positionTitle: String,
        positionDescription: String,
        positionLink: String,
        isExternal: Boolean,
        assessmentNames: List<String>
    ): String {
        return """
        🆕 *New Position Created*

        📝 *Title*: $positionTitle
        👤 *Created by*: $createdBy
        🔗 *Link*: $positionLink
        ${if (isExternal) "🌍 *External Position*" else "🏢 *Internal Position*"}
        ${if (assessmentNames.isNotEmpty()) "📊 *Assessments*: ${assessmentNames.joinToString(", ")}" else ""}
        
        *Description*:
        ${positionDescription.take(200)}${if (positionDescription.length > 200) "..." else ""}
        """.trimIndent()
    }


    /**
     * Sends candidate invitation notification to internal Google Chat
     */
    suspend fun sendCandidateInvitationNotification(
        candidateEmail: String,
        candidateName: String,
        positionTitle: String,
        recruiterName: String,
        assessmentsCount: Int
    ) {
        val message = buildInvitationMessage(
            candidateEmail,
            candidateName,
            positionTitle,
            recruiterName,
            assessmentsCount
        )

        try {
            googleChatClient.sendMessage(gchatInternalWebhookUrl, message)
            logger.info("Candidate invitation notification sent for {}", candidateEmail)
        } catch (e: Exception) {
            logger.error("Failed to send invitation notification: {}", e.message, e)
        }
    }

    private fun buildInvitationMessage(
        candidateEmail: String,
        candidateName: String,
        positionTitle: String,
        recruiterName: String,
        assessmentsCount: Int
    ): String {
        return """
        📧 *New Candidate Invited*

        👤 *Candidate*: $candidateName
        📬 *Email*: $candidateEmail

        💼 *Position*: $positionTitle
        📝 *Assessments*: $assessmentsCount

        👨‍💼 *Invited by*: $recruiterName
        """.trimIndent()
    }

    /**
     * Builds notification message when report is available
     */
    private fun buildMessageWithReport(
        email: String,
        recruiterName: String?,
        report: AssessmentReport,
        timeExpired: Boolean
    ): String {
        val correctCount = report.mcDetails?.count { it.correct == true }
        val totalCount = report.mcDetails?.size
        val recruiterNameMessage = recruiterName ?: "Unknown Recruiter"
        val statusIcon = if (timeExpired) "⏰" else "✅"
        val statusText = if (timeExpired) "Time Expired" else "Assessment Completed"

        return """
        $statusIcon *${report.displayName} / $statusText*

        👤 *Candidate*: $email
        🎯 *Score*: $correctCount / $totalCount
        👨‍💼 *Recruiter*: $recruiterNameMessage

        🕵️ *Cheating Indicators*:

        • 📝 *Plagiarism*: ${report.cheatingDetails?.plagiarism}
        • 📋 *Pasted Code*: ${report.cheatingDetails?.pastedCode}
        • 👀 *Suspicious Activity*: ${report.cheatingDetails?.suspiciousActivity}
        • 🤖 *AI Usage*: ${report.cheatingDetails?.aiUsage}
        • 🧭 *Tab Switching*: ${report.cheatingDetails?.tabLeaving} times

        📊 *Summary*:

        • 🧩 *Challenge Score*: ${report.codeScore}%
        • ❓ *Questions Score*: ${report.mcScore}%

        • 🧮 *Final Score*: ${report.finalScore}%
        • 🎯 *Qualifying Score*: ${report.qualifyingScore}%
        • 🧾 *Verdict*: ${if (report.isQualified) "✅ Passed" else "❌ Disqualified"}
        """.trimIndent()
    }

    /**
     * Builds notification message when report is still being processed
     */
    private fun buildMessageWithoutReport(
        email: String,
        recruiterName: String?,
        assessmentId: String,
        timeExpired: Boolean
    ): String {
        val recruiterNameMessage = recruiterName ?: "Unknown Recruiter"
        val statusIcon = if (timeExpired) "⏰" else "⏳"
        val statusText = if (timeExpired) {
            "Time Expired - Report Processing"
        } else {
            "Assessment Completed - Report Processing"
        }

        return """
        $statusIcon *$statusText*
  
        👤 *Candidate*: $email
        
        📝 *Assessment ID*: $assessmentId

        👨‍💼 *Recruiter*: $recruiterNameMessage

        ℹ️ *Status*: The assessment report is still being processed by Coderbyte.
        """.trimIndent()
    }

    /**
     * Builds notification message when candidate starts an assessment
     */
    private fun buildAssessmentStartedMessage(email: String, assessmentId: String): String {
        return """
        🚀 *Assessment Started*
    
        👤 *Candidate*: $email
        📝 *Assessment ID*: $assessmentId
    
        ℹ️ *Status*: Candidate has started the assessment. The test is now in progress.
        """.trimIndent()
    }

    /**
     * Sends notification when an application is approved
     */
    suspend fun sendApplicationApprovedNotification(
        candidateEmail: String,
        candidateName: String,
        positionTitle: String,
        reviewedBy: String
    ) {
        val message = buildApplicationApprovedMessage(
            candidateEmail,
            candidateName,
            positionTitle,
            reviewedBy
        )

        try {
            googleChatClient.sendMessage(gchatInternalWebhookUrl, message)
            logger.info("Application approved notification sent for {}", candidateEmail)
        } catch (e: Exception) {
            logger.error("Failed to send application approved notification: {}", e.message, e)
        }
    }

    /**
     * Sends notification when an application is rejected
     */
    suspend fun sendApplicationRejectedNotification(
        candidateEmail: String,
        candidateName: String,
        positionTitle: String,
        reviewedBy: String,
        statusNote: String? = null
    ) {
        val message = buildApplicationRejectedMessage(
            candidateEmail,
            candidateName,
            positionTitle,
            reviewedBy,
            statusNote
        )

        try {
            googleChatClient.sendMessage(gchatInternalWebhookUrl, message)
            logger.info("Application rejected notification sent for {}", candidateEmail)
        } catch (e: Exception) {
            logger.error("Failed to send application rejected notification: {}", e.message, e)
        }
    }

    private fun buildApplicationApprovedMessage(
        candidateEmail: String,
        candidateName: String,
        positionTitle: String,
        reviewedBy: String
    ): String {
        return """
        ✅ *Application Approved*

        👤 *Candidate*: $candidateName
        📬 *Email*: $candidateEmail

        💼 *Position*: $positionTitle

        👨‍💼 *Reviewed by*: $reviewedBy
        🎉 *Status*: Application has been approved
        """.trimIndent()
    }

    private fun buildApplicationRejectedMessage(
        candidateEmail: String,
        candidateName: String,
        positionTitle: String,
        reviewedBy: String,
        statusNote: String? = null
    ): String {
        val noteLine = if (statusNote != null) "\n📝 *Note*: $statusNote" else ""
        return """
        ❌ *Application Rejected*

        👤 *Candidate*: $candidateName
        📬 *Email*: $candidateEmail

        💼 *Position*: $positionTitle

        👨‍💼 *Reviewed by*: $reviewedBy
        📋 *Status*: Application has been rejected$noteLine
        """.trimIndent()
    }
}
