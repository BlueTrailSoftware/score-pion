package org.example.notifier.application.event

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.example.notifier.application.service.notification.NotificationOrchestrator
import org.example.notifier.domain.applicant.ApplicantStatus
import org.example.notifier.domain.event.*
import org.example.notifier.domain.position.OpenPosition
import org.example.notifier.domain.user.UserRole
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class NotificationEventListenerTest {

    private lateinit var notificationOrchestrator: NotificationOrchestrator
    private lateinit var listener: NotificationEventListener

    @BeforeEach
    fun setup() {
        notificationOrchestrator = mock<NotificationOrchestrator>()
        // Unconfined dispatcher executes launch{} bodies synchronously in the calling thread,
        // so verify() calls below don't need Thread.sleep.
        listener = NotificationEventListener(notificationOrchestrator, CoroutineScope(Dispatchers.Unconfined))
    }

    @Test
    fun `onUserInvited calls notifyRecruiterInvitation for RECRUITER role`() {
        val event = UserInvitedEvent(
            recipientEmail = "recruiter@test.com",
            role = UserRole.RECRUITER,
            adminName = "Admin"
        )

        listener.onUserInvited(event)

        runBlocking {
            verify(notificationOrchestrator).notifyRecruiterInvitation(
                recipientEmail = "recruiter@test.com",
                adminName = "Admin"
            )
        }
    }

    @Test
    fun `onUserInvited calls notifyAdminInvitation for ADMIN role`() {
        val event = UserInvitedEvent(
            recipientEmail = "admin@test.com",
            role = UserRole.ADMIN,
            adminName = "Super Admin"
        )

        listener.onUserInvited(event)

        runBlocking {
            verify(notificationOrchestrator).notifyAdminInvitation(
                recipientEmail = "admin@test.com",
                invitedBy = "Super Admin"
            )
        }
    }

    @Test
    fun `onCandidateInvited calls both candidate and recruiter notification methods`() {
        val event = CandidateInvitedEvent(
            candidateEmail = "candidate@test.com",
            candidateName = "Jane",
            positionTitle = "Engineer",
            recruiterEmail = "recruiter@test.com",
            recruiterName = "Bob",
            assessmentsCount = 2
        )

        listener.onCandidateInvited(event)

        runBlocking {
            verify(notificationOrchestrator).notifyCandidateInvitation(
                candidateEmail = "candidate@test.com",
                candidateName = "Jane",
                positionTitle = "Engineer",
                recruiterName = "Bob",
                assessmentsCount = 2
            )
            verify(notificationOrchestrator).notifyRecruiterCandidateInvitation(
                recruiterEmail = "recruiter@test.com",
                recruiterName = "Bob",
                candidateName = "Jane",
                candidateEmail = "candidate@test.com",
                positionTitle = "Engineer",
                assessmentsCount = 2
            )
        }
    }

    @Test
    fun `onAssessmentStarted calls notifyAssessmentStarted`() {
        val event = AssessmentStartedNotificationEvent(
            candidateEmail = "candidate@test.com",
            assessmentId = "a-1"
        )

        listener.onAssessmentStarted(event)

        runBlocking {
            verify(notificationOrchestrator).notifyAssessmentStarted(
                candidateEmail = "candidate@test.com",
                assessmentId = "a-1"
            )
        }
    }

    @Test
    fun `onAssessmentCompleted calls notifyAssessmentCompletedWithReport when report is ready`() {
        val report = mock<org.example.notifier.domain.shared.AssessmentReport>()
        val event = AssessmentCompletedNotificationEvent(
            candidateEmail = "candidate@test.com",
            candidateName = "Jane",
            report = report,
            isReportReady = true,
            assessmentId = "a-1",
            recruiterEmail = "recruiter@test.com",
            recruiterName = "Bob",
            positionTitle = "Engineer",
            timeExpired = false
        )

        listener.onAssessmentCompleted(event)

        runBlocking {
            verify(notificationOrchestrator).notifyAssessmentCompletedWithReport(
                candidateEmail = "candidate@test.com",
                candidateName = "Jane",
                report = report,
                recruiterEmail = "recruiter@test.com",
                recruiterName = "Bob",
                positionTitle = "Engineer",
                timeExpired = false
            )
        }
    }

    @Test
    fun `onAssessmentCompleted calls notifyAssessmentCompletedWithoutReport when report is not ready`() {
        val event = AssessmentCompletedNotificationEvent(
            candidateEmail = "candidate@test.com",
            candidateName = null,
            report = null,
            isReportReady = false,
            assessmentId = "a-1",
            recruiterEmail = null,
            recruiterName = null,
            positionTitle = null,
            timeExpired = true
        )

        listener.onAssessmentCompleted(event)

        runBlocking {
            verify(notificationOrchestrator).notifyAssessmentCompletedWithoutReport(
                candidateEmail = "candidate@test.com",
                candidateName = null,
                assessmentId = "a-1",
                recruiterEmail = null,
                recruiterName = null,
                positionTitle = null,
                timeExpired = true
            )
        }
    }

    @Test
    fun `onApplicantStatusChanged calls notifyApplicantApproval for INVITED status`() {
        val event = ApplicantStatusChangedEvent(
            applicantEmail = "applicant@test.com",
            applicantName = "Jane",
            positionTitle = "Engineer",
            newStatus = ApplicantStatus.INVITED,
            reviewedBy = "admin@test.com"
        )

        listener.onApplicantStatusChanged(event)

        runBlocking {
            verify(notificationOrchestrator).notifyApplicantApproval(
                applicantEmail = "applicant@test.com",
                applicantName = "Jane",
                positionTitle = "Engineer",
                reviewedBy = "admin@test.com"
            )
        }
    }

    @Test
    fun `onApplicantStatusChanged calls notifyApplicantRejection for REJECTED status`() {
        val event = ApplicantStatusChangedEvent(
            applicantEmail = "applicant@test.com",
            applicantName = "Jane",
            positionTitle = "Engineer",
            newStatus = ApplicantStatus.REJECTED,
            reviewedBy = "admin@test.com",
            statusNote = "Not a fit"
        )

        listener.onApplicantStatusChanged(event)

        runBlocking {
            verify(notificationOrchestrator).notifyApplicantRejection(
                applicantEmail = "applicant@test.com",
                applicantName = "Jane",
                positionTitle = "Engineer",
                reviewedBy = "admin@test.com",
                statusNote = "Not a fit"
            )
        }
    }

    @Test
    fun `onCandidateApplication calls notifyCandidateApplication`() {
        val event = CandidateApplicationEvent(
            candidateEmail = "candidate@test.com",
            candidateName = "Jane",
            positionTitle = "Engineer"
        )

        listener.onCandidateApplication(event)

        runBlocking {
            verify(notificationOrchestrator).notifyCandidateApplication(
                candidateEmail = "candidate@test.com",
                candidateName = "Jane",
                positionTitle = "Engineer",
                recruiterName = null
            )
        }
    }

    @Test
    fun `onPositionCreated calls notifyPositionCreated`() {
        val position = OpenPosition(id = "pos-1", title = "Engineer", description = "", createdBy = "admin")
        val event = PositionCreatedNotificationEvent(
            createdBy = "admin@test.com",
            position = position,
            assessmentNames = listOf("Java Test")
        )

        listener.onPositionCreated(event)

        runBlocking {
            verify(notificationOrchestrator).notifyPositionCreated(
                createdBy = "admin@test.com",
                position = position,
                assessmentNames = listOf("Java Test")
            )
        }
    }

    @Test
    fun `onRecruiterPositionsAssigned calls notifyPositionAssignment`() {
        val positions = listOf(OpenPosition(id = "pos-1", title = "Dev", description = "", createdBy = "admin"))
        val event = RecruiterPositionsAssignedEvent(
            recruiterEmail = "recruiter@test.com",
            recruiterName = "Bob",
            positions = positions
        )

        listener.onRecruiterPositionsAssigned(event)

        runBlocking {
            verify(notificationOrchestrator).notifyPositionAssignment(
                recruiterEmail = "recruiter@test.com",
                recruiterName = "Bob",
                positions = positions
            )
        }
    }

    @Test
    fun `onDataErasureRequested calls notifyDataErasureRequest`() {
        val event = DataErasureRequestedEvent(email = "user@test.com", token = "tok-123")

        listener.onDataErasureRequested(event)

        runBlocking { verify(notificationOrchestrator).notifyDataErasureRequest("user@test.com", "tok-123") }
    }

    @Test
    fun `onDataErasureConfirmed calls notifyDataErasureConfirmed`() {
        val event = DataErasureConfirmedEvent(email = "user@test.com")

        listener.onDataErasureConfirmed(event)

        runBlocking { verify(notificationOrchestrator).notifyDataErasureConfirmed("user@test.com") }
    }

    @Test
    fun `onDataExportRequested calls notifyDataExportRequest`() {
        val event = DataExportRequestedEvent(email = "user@test.com", token = "tok-456")

        listener.onDataExportRequested(event)

        runBlocking { verify(notificationOrchestrator).notifyDataExportRequest("user@test.com", "tok-456") }
    }
}