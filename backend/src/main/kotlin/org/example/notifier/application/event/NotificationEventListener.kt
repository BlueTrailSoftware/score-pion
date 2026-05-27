package org.example.notifier.application.event

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.example.notifier.application.service.notification.NotificationOrchestrator
import org.example.notifier.domain.applicant.ApplicantStatus
import org.example.notifier.domain.event.ApplicantStatusChangedEvent
import org.example.notifier.domain.event.AssessmentCompletedNotificationEvent
import org.example.notifier.domain.event.AssessmentStartedNotificationEvent
import org.example.notifier.domain.event.CandidateApplicationEvent
import org.example.notifier.domain.event.CandidateInvitedEvent
import org.example.notifier.domain.event.DataErasureConfirmedEvent
import org.example.notifier.domain.event.DataErasureRequestedEvent
import org.example.notifier.domain.event.DataExportRequestedEvent
import org.example.notifier.domain.event.PositionCreatedNotificationEvent
import org.example.notifier.domain.event.RecruiterPositionsAssignedEvent
import org.example.notifier.domain.event.UserInvitedEvent
import org.example.notifier.domain.user.UserRole
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class NotificationEventListener(
    private val notificationOrchestrator: NotificationOrchestrator,
    private val applicationScope: CoroutineScope
) {

    @EventListener
    fun onCandidateInvited(event: CandidateInvitedEvent) {
        applicationScope.launch {
            notificationOrchestrator.notifyCandidateInvitation(
                candidateEmail = event.candidateEmail,
                candidateName = event.candidateName,
                positionTitle = event.positionTitle,
                recruiterName = event.recruiterName,
                assessmentsCount = event.assessmentsCount
            )
            notificationOrchestrator.notifyRecruiterCandidateInvitation(
                recruiterEmail = event.recruiterEmail,
                recruiterName = event.recruiterName,
                candidateName = event.candidateName,
                candidateEmail = event.candidateEmail,
                positionTitle = event.positionTitle,
                assessmentsCount = event.assessmentsCount
            )
        }
    }

    @EventListener
    fun onUserInvited(event: UserInvitedEvent) {
        applicationScope.launch {
            if (event.role == UserRole.ADMIN) {
                notificationOrchestrator.notifyAdminInvitation(
                    recipientEmail = event.recipientEmail,
                    invitedBy = event.adminName
                )
            } else {
                notificationOrchestrator.notifyRecruiterInvitation(
                    recipientEmail = event.recipientEmail,
                    adminName = event.adminName
                )
            }
        }
    }

    @EventListener
    fun onPositionCreated(event: PositionCreatedNotificationEvent) {
        applicationScope.launch {
            notificationOrchestrator.notifyPositionCreated(
                createdBy = event.createdBy,
                position = event.position,
                assessmentNames = event.assessmentNames
            )
        }
    }

    @EventListener
    fun onAssessmentCompleted(event: AssessmentCompletedNotificationEvent) {
        applicationScope.launch {
            if (event.isReportReady && event.report != null) {
                notificationOrchestrator.notifyAssessmentCompletedWithReport(
                    candidateEmail = event.candidateEmail,
                    candidateName = event.candidateName,
                    report = event.report,
                    recruiterEmail = event.recruiterEmail,
                    recruiterName = event.recruiterName,
                    positionTitle = event.positionTitle,
                    timeExpired = event.timeExpired
                )
            } else {
                notificationOrchestrator.notifyAssessmentCompletedWithoutReport(
                    candidateEmail = event.candidateEmail,
                    candidateName = event.candidateName,
                    assessmentId = event.assessmentId,
                    recruiterEmail = event.recruiterEmail,
                    recruiterName = event.recruiterName,
                    positionTitle = event.positionTitle,
                    timeExpired = event.timeExpired
                )
            }
        }
    }

    @EventListener
    fun onAssessmentStarted(event: AssessmentStartedNotificationEvent) {
        applicationScope.launch {
            notificationOrchestrator.notifyAssessmentStarted(
                candidateEmail = event.candidateEmail,
                assessmentId = event.assessmentId
            )
        }
    }

    @EventListener
    fun onCandidateApplication(event: CandidateApplicationEvent) {
        applicationScope.launch {
            notificationOrchestrator.notifyCandidateApplication(
                candidateEmail = event.candidateEmail,
                candidateName = event.candidateName,
                positionTitle = event.positionTitle,
                recruiterName = event.recruiterName
            )
        }
    }

    @EventListener
    fun onApplicantStatusChanged(event: ApplicantStatusChangedEvent) {
        applicationScope.launch {
            when (event.newStatus) {
                ApplicantStatus.INVITED -> notificationOrchestrator.notifyApplicantApproval(
                    applicantEmail = event.applicantEmail,
                    applicantName = event.applicantName,
                    positionTitle = event.positionTitle,
                    reviewedBy = event.reviewedBy
                )
                ApplicantStatus.REJECTED -> notificationOrchestrator.notifyApplicantRejection(
                    applicantEmail = event.applicantEmail,
                    applicantName = event.applicantName,
                    positionTitle = event.positionTitle,
                    reviewedBy = event.reviewedBy,
                    statusNote = event.statusNote
                )
                else -> {}
            }
        }
    }

    @EventListener
    fun onRecruiterPositionsAssigned(event: RecruiterPositionsAssignedEvent) {
        applicationScope.launch {
            notificationOrchestrator.notifyPositionAssignment(
                recruiterEmail = event.recruiterEmail,
                recruiterName = event.recruiterName,
                positions = event.positions
            )
        }
    }

    @EventListener
    fun onDataErasureRequested(event: DataErasureRequestedEvent) {
        applicationScope.launch {
            notificationOrchestrator.notifyDataErasureRequest(event.email, event.token)
        }
    }

    @EventListener
    fun onDataErasureConfirmed(event: DataErasureConfirmedEvent) {
        applicationScope.launch {
            notificationOrchestrator.notifyDataErasureConfirmed(event.email)
        }
    }

    @EventListener
    fun onDataExportRequested(event: DataExportRequestedEvent) {
        applicationScope.launch {
            notificationOrchestrator.notifyDataExportRequest(event.email, event.token)
        }
    }
}
