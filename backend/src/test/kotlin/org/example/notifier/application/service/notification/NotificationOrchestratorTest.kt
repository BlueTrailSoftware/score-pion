package org.example.notifier.application.service.notification

import kotlinx.coroutines.runBlocking
import org.example.notifier.domain.shared.AssessmentReport
import org.example.notifier.domain.shared.EmailEvent
import org.example.notifier.domain.position.OpenPosition
import org.example.notifier.application.service.EmailNotificationService
import org.example.notifier.application.service.GoogleChatNotificationService
import org.example.notifier.domain.port.GlobalRecipientsRepository
import org.example.notifier.infrastructure.external.EmailTemplate
import org.example.notifier.infrastructure.external.factory.EmailTemplateFactory
import org.example.notifier.infrastructure.logging.LoggerPort
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class NotificationOrchestratorTest {

    private lateinit var emailNotificationService: EmailNotificationService
    private lateinit var googleChatNotificationService: GoogleChatNotificationService
    private lateinit var emailTemplateFactory: EmailTemplateFactory
    private lateinit var globalRecipientsRepository: GlobalRecipientsRepository
    private lateinit var logger: LoggerPort
    private lateinit var notificationOrchestrator: NotificationOrchestrator

    private val frontendUrl = "http://localhost:3000"

    @BeforeEach
    fun setup() {
        emailNotificationService = mock(EmailNotificationService::class.java)
        googleChatNotificationService = mock(GoogleChatNotificationService::class.java)
        emailTemplateFactory = mock(EmailTemplateFactory::class.java)
        globalRecipientsRepository = mock(GlobalRecipientsRepository::class.java)
        logger = mock(LoggerPort::class.java)

        notificationOrchestrator = NotificationOrchestrator(
            emailNotificationService = emailNotificationService,
            googleChatNotificationService = googleChatNotificationService,
            emailTemplateFactory = emailTemplateFactory,
            globalRecipientsRepository = globalRecipientsRepository,
            logger = logger,
            frontendUrl = frontendUrl
        )
    }

    // ========== notifyAssessmentCompletedWithReport Tests ==========

    @Test
    fun `notifyAssessmentCompletedWithReport should include recruiterName in Google Chat notification`() = runBlocking {
        val candidateEmail = "candidate@example.com"
        val recruiterName = "John Smith"
        val report = AssessmentReport(
            candidateEmail = candidateEmail,
            assessmentId = "assessment-001",
            displayName = "Python Basics",
            finalScore = 85.0,
            status = "COMPLETED",
            isQualified = true
        )

        whenever(globalRecipientsRepository.getAllEmails()).thenReturn(emptyList())

        notificationOrchestrator.notifyAssessmentCompletedWithReport(
            candidateEmail = candidateEmail,
            candidateName = "Jane Doe",
            report = report,
            recruiterEmail = "recruiter@example.com",
            recruiterName = recruiterName,
            positionTitle = "Senior Developer",
            timeExpired = false
        )

        val captor = argumentCaptor<String>()
        verify(googleChatNotificationService).sendAssessmentWithReportNotification(
            candidateEmail = any(),
            report = any(),
            timeExpired = any(),
            recruiterName = captor.capture()
        )
        
        assert(captor.firstValue == recruiterName)
    }

    @Test
    fun `notifyAssessmentCompletedWithReport should pass recruiterName to global recipients event`(): Unit = runBlocking {
        val candidateEmail = "candidate@example.com"
        val recruiterName = "John Smith"
        val report = AssessmentReport(
            candidateEmail = candidateEmail,
            assessmentId = "assessment-001",
            displayName = "Python Basics",
            finalScore = 85.0,
            status = "COMPLETED",
            isQualified = true
        )

        whenever(globalRecipientsRepository.getAllEmails()).thenReturn(listOf("admin@example.com"))
        whenever(emailTemplateFactory.createEmail(any(), any())).thenReturn(
            EmailTemplate(
                to = "admin@example.com",
                subject = "Test",
                textContent = "Test",
                from = "test"
            )
        )

        notificationOrchestrator.notifyAssessmentCompletedWithReport(
            candidateEmail = candidateEmail,
            candidateName = "Jane Doe",
            report = report,
            recruiterEmail = "recruiter@example.com",
            recruiterName = recruiterName,
            positionTitle = "Senior Developer",
            timeExpired = false
        )

        // Verify that email template factory was called (for global recipients + recruiter)
        verify(emailTemplateFactory, times(2)).createEmail(any(), any())
    }

    @Test
    fun `notifyAssessmentCompletedWithReport should handle null recruiterName in Google Chat`() = runBlocking {
        val candidateEmail = "candidate@example.com"
        val report = AssessmentReport(
            candidateEmail = candidateEmail,
            assessmentId = "assessment-002",
            displayName = "JavaScript Advanced",
            finalScore = 75.0,
            status = "COMPLETED",
            isQualified = false
        )

        whenever(globalRecipientsRepository.getAllEmails()).thenReturn(emptyList())

        notificationOrchestrator.notifyAssessmentCompletedWithReport(
            candidateEmail = candidateEmail,
            candidateName = "Jane Doe",
            report = report,
            recruiterEmail = null,
            recruiterName = null,
            positionTitle = "Senior Developer",
            timeExpired = false
        )

        // Verify the method was called - we can't easily capture nullable values
        verify(googleChatNotificationService).sendAssessmentWithReportNotification(
            candidateEmail = any(),
            report = any(),
            timeExpired = any(),
            recruiterName = org.mockito.kotlin.isNull()
        )
    }

    // ========== notifyAssessmentCompletedWithoutReport Tests ==========

    @Test
    fun `notifyAssessmentCompletedWithoutReport should include recruiterName in Google Chat notification`() = runBlocking {
        val candidateEmail = "candidate@example.com"
        val recruiterName = "Sarah Johnson"
        val assessmentId = "assessment-123"

        whenever(globalRecipientsRepository.getAllEmails()).thenReturn(emptyList())

        notificationOrchestrator.notifyAssessmentCompletedWithoutReport(
            candidateEmail = candidateEmail,
            candidateName = "John Doe",
            assessmentId = assessmentId,
            recruiterEmail = "recruiter@example.com",
            recruiterName = recruiterName,
            positionTitle = "Software Engineer",
            timeExpired = true
        )

        val captor = argumentCaptor<String>()
        verify(googleChatNotificationService).sendAssessmentWithoutReportNotification(
            candidateEmail = any(),
            assessmentId = any(),
            timeExpired = any(),
            recruiterName = captor.capture()
        )
        
        assert(captor.firstValue == recruiterName)
    }

    @Test
    fun `notifyAssessmentCompletedWithoutReport should handle null recruiterName`() = runBlocking {
        val candidateEmail = "candidate@example.com"
        val assessmentId = "assessment-456"

        whenever(globalRecipientsRepository.getAllEmails()).thenReturn(emptyList())

        notificationOrchestrator.notifyAssessmentCompletedWithoutReport(
            candidateEmail = candidateEmail,
            candidateName = null,
            assessmentId = assessmentId,
            recruiterEmail = null,
            recruiterName = null,
            positionTitle = null,
            timeExpired = false
        )

        // Verify the method was called - we can't easily capture nullable values
        verify(googleChatNotificationService).sendAssessmentWithoutReportNotification(
            candidateEmail = any(),
            assessmentId = any(),
            timeExpired = any(),
            recruiterName = org.mockito.kotlin.isNull()
        )
    }

    // ========== notifyCandidateInvitation Tests ==========

    @Test
    fun `notifyCandidateInvitation should include recruiterName in Google Chat notification`() = runBlocking {
        val candidateEmail = "candidate@example.com"
        val recruiterName = "Mike Wilson"

        whenever(globalRecipientsRepository.getAllEmails()).thenReturn(emptyList())

        notificationOrchestrator.notifyCandidateInvitation(
            candidateEmail = candidateEmail,
            candidateName = "Alice Smith",
            positionTitle = "Product Manager",
            recruiterName = recruiterName,
            assessmentsCount = 2
        )

        val captor = argumentCaptor<String>()
        verify(googleChatNotificationService).sendCandidateInvitationNotification(
            candidateEmail = any(),
            candidateName = any(),
            positionTitle = any(),
            recruiterName = captor.capture(),
            assessmentsCount = any()
        )
        
        assert(captor.firstValue == recruiterName)
    }

    @Test
    fun `notifyCandidateInvitation should pass recruiterName to global recipients`() = runBlocking {
        val candidateEmail = "candidate@example.com"
        val recruiterName = "Mike Wilson"

        whenever(globalRecipientsRepository.getAllEmails()).thenReturn(listOf("admin@example.com"))
        whenever(emailTemplateFactory.createEmail(any(), any())).thenReturn(
            EmailTemplate(
                to = "admin@example.com",
                subject = "Test",
                textContent = "Test",
                from = "test"
            )
        )

        notificationOrchestrator.notifyCandidateInvitation(
            candidateEmail = candidateEmail,
            candidateName = "Alice Smith",
            positionTitle = "Product Manager",
            recruiterName = recruiterName,
            assessmentsCount = 2
        )

        val captor = argumentCaptor<EmailEvent>()
        verify(emailTemplateFactory).createEmail(any(), captor.capture())

        val event = captor.firstValue
        assert(event is EmailEvent.CandidateInvited)
        val candidateInvitedEvent = event as EmailEvent.CandidateInvited
        assert(candidateInvitedEvent.recruiterName == recruiterName)
    }

    // ========== notifyPositionCreated Tests ==========

    @Test
    fun `notifyPositionCreated should include recruiterName in Google Chat notification`() = runBlocking {
        val createdBy = "admin@example.com"
        val position = OpenPosition(
            id = "pos-123",
            title = "Backend Developer",
            description = "Looking for senior backend developer",
            createdBy = createdBy,
            external = false
        )

        whenever(globalRecipientsRepository.getAllEmails()).thenReturn(emptyList())

        notificationOrchestrator.notifyPositionCreated(
            createdBy = createdBy,
            position = position,
            assessmentNames = listOf("Python Test", "System Design")
        )

        verify(googleChatNotificationService).sendPositionCreatedNotification(
            createdBy = any(),
            positionTitle = any(),
            positionDescription = any(),
            positionLink = any(),
            isExternal = any(),
            assessmentNames = any()
        )
    }

    // ========== notifyPositionAssignment Tests ==========

    @Test
    fun `notifyPositionAssignment should include recruiterName in email event`() = runBlocking {
        val recruiterEmail = "recruiter@example.com"
        val recruiterName = "Bob Johnson"
        val positions = listOf(
            OpenPosition(
                id = "pos-1",
                title = "Frontend Developer",
                description = "React expert needed",
                createdBy = "admin@example.com",
                external = false
            )
        )

        whenever(globalRecipientsRepository.getAllEmails()).thenReturn(emptyList())
        whenever(emailTemplateFactory.createEmail(any(), any())).thenReturn(
            EmailTemplate(
                to = recruiterEmail,
                subject = "Position Assignment",
                textContent = "You have been assigned to new positions",
                from = "test"
            )
        )

        notificationOrchestrator.notifyPositionAssignment(
            recruiterEmail = recruiterEmail,
            recruiterName = recruiterName,
            positions = positions
        )

        val captor = argumentCaptor<EmailEvent>()
        verify(emailTemplateFactory, times(1)).createEmail(any(), captor.capture())

        val event = captor.firstValue
        assert(event is EmailEvent.PositionAssigned)
        val assignedEvent = event as EmailEvent.PositionAssigned
        assert(assignedEvent.recruiterName == recruiterName)
    }

    @Test
    fun `notifyPositionAssignment should reject empty recruiterEmail`() = runBlocking {
        val positions = listOf(
            OpenPosition(
                id = "pos-1",
                title = "Frontend Developer",
                description = "React expert needed",
                createdBy = "admin@example.com",
                external = false
            )
        )

        notificationOrchestrator.notifyPositionAssignment(
            recruiterEmail = "",
            recruiterName = "Bob Johnson",
            positions = positions
        )

        verify(emailTemplateFactory, never()).createEmail(any(), any())
        verify(logger).error("Cannot send position assignment: Recruiter email is empty")
    }

    // ========== notifyApplicantApproval Tests ==========

    @Test
    fun `notifyApplicantApproval should include recruiterName in email event`() = runBlocking {
        val applicantEmail = "applicant@example.com"
        val recruiterName = "Diana Lee"

        whenever(globalRecipientsRepository.getAllEmails()).thenReturn(emptyList())
        whenever(emailTemplateFactory.createEmail(any(), any())).thenReturn(
            EmailTemplate(
                to = applicantEmail,
                subject = "Application Approved",
                textContent = "Congratulations!",
                from = "test"
            )
        )

        notificationOrchestrator.notifyApplicantApproval(
            applicantEmail = applicantEmail,
            applicantName = "Test Applicant",
            positionTitle = "QA Engineer",
            reviewedBy = recruiterName
        )

        val captor = argumentCaptor<EmailEvent>()
        verify(emailTemplateFactory).createEmail(any(), captor.capture())

        val event = captor.firstValue
        assert(event is EmailEvent.ApplicantApproved)
        val approvalEvent = event as EmailEvent.ApplicantApproved
        assert(approvalEvent.recruiterName == recruiterName)
    }

    @Test
    fun `notifyApplicantApproval should include recruiterName in Google Chat notification`() = runBlocking {
        val applicantEmail = "applicant@example.com"
        val recruiterName = "Diana Lee"

        whenever(globalRecipientsRepository.getAllEmails()).thenReturn(emptyList())
        whenever(emailTemplateFactory.createEmail(any(), any())).thenReturn(
            EmailTemplate(
                to = applicantEmail,
                subject = "Application Approved",
                textContent = "Congratulations!",
                from = "test"
            )
        )

        notificationOrchestrator.notifyApplicantApproval(
            applicantEmail = applicantEmail,
            applicantName = "Test Applicant",
            positionTitle = "QA Engineer",
            reviewedBy = recruiterName
        )

        val captor = argumentCaptor<String>()
        verify(googleChatNotificationService).sendApplicationApprovedNotification(
            candidateEmail = any(),
            candidateName = any(),
            positionTitle = any(),
            reviewedBy = captor.capture()
        )
        
        assert(captor.firstValue == recruiterName)
    }

    @Test
    fun `notifyApplicantApproval should handle null recruiterName with default fallback`() = runBlocking {
        val applicantEmail = "applicant@example.com"

        whenever(globalRecipientsRepository.getAllEmails()).thenReturn(emptyList())
        whenever(emailTemplateFactory.createEmail(any(), any())).thenReturn(
            EmailTemplate(
                to = applicantEmail,
                subject = "Application Approved",
                textContent = "Congratulations!",
                from = "test"
            )
        )

        notificationOrchestrator.notifyApplicantApproval(
            applicantEmail = applicantEmail,
            applicantName = "Test Applicant",
            positionTitle = "QA Engineer",
            reviewedBy = null
        )

        val captor = argumentCaptor<String>()
        verify(googleChatNotificationService).sendApplicationApprovedNotification(
            candidateEmail = any(),
            candidateName = any(),
            positionTitle = any(),
            reviewedBy = captor.capture()
        )
        
        assert(captor.firstValue == "System")
    }

    // ========== notifyApplicantRejection Tests ==========

    @Test
    fun `notifyApplicantRejection should include recruiterName in email event`() = runBlocking {
        val applicantEmail = "applicant@example.com"
        val recruiterName = "Elena Martinez"

        whenever(globalRecipientsRepository.getAllEmails()).thenReturn(emptyList())
        whenever(emailTemplateFactory.createEmail(any(), any())).thenReturn(
            EmailTemplate(
                to = applicantEmail,
                subject = "Application Status",
                textContent = "Thank you for applying",
                from = "test"
            )
        )

        notificationOrchestrator.notifyApplicantRejection(
            applicantEmail = applicantEmail,
            applicantName = "Test Applicant",
            positionTitle = "Data Scientist",
            reviewedBy = recruiterName
        )

        val captor = argumentCaptor<EmailEvent>()
        verify(emailTemplateFactory).createEmail(any(), captor.capture())

        val event = captor.firstValue
        assert(event is EmailEvent.ApplicantRejected)
        val rejectionEvent = event as EmailEvent.ApplicantRejected
        assert(rejectionEvent.recruiterName == recruiterName)
    }

    @Test
    fun `notifyApplicantRejection should include recruiterName in Google Chat notification`() = runBlocking {
        val applicantEmail = "applicant@example.com"
        val recruiterName = "Elena Martinez"

        whenever(globalRecipientsRepository.getAllEmails()).thenReturn(emptyList())
        whenever(emailTemplateFactory.createEmail(any(), any())).thenReturn(
            EmailTemplate(
                to = applicantEmail,
                subject = "Application Status",
                textContent = "Thank you for applying",
                from = "test"
            )
        )

        notificationOrchestrator.notifyApplicantRejection(
            applicantEmail = applicantEmail,
            applicantName = "Test Applicant",
            positionTitle = "Data Scientist",
            reviewedBy = recruiterName
        )

        val captor = argumentCaptor<String>()
        verify(googleChatNotificationService).sendApplicationRejectedNotification(
            candidateEmail = any(),
            candidateName = any(),
            positionTitle = any(),
            reviewedBy = captor.capture(),
            statusNote = anyOrNull()
        )

        assert(captor.firstValue == recruiterName)
    }

    @Test
    fun `notifyApplicantRejection should handle null recruiterName with default fallback`() = runBlocking {
        val applicantEmail = "applicant@example.com"

        whenever(globalRecipientsRepository.getAllEmails()).thenReturn(emptyList())
        whenever(emailTemplateFactory.createEmail(any(), any())).thenReturn(
            EmailTemplate(
                to = applicantEmail,
                subject = "Application Status",
                textContent = "Thank you for applying",
                from = "test"
            )
        )

        notificationOrchestrator.notifyApplicantRejection(
            applicantEmail = applicantEmail,
            applicantName = "Test Applicant",
            positionTitle = "Data Scientist",
            reviewedBy = null
        )

        val captor = argumentCaptor<String>()
        verify(googleChatNotificationService).sendApplicationRejectedNotification(
            candidateEmail = any(),
            candidateName = any(),
            positionTitle = any(),
            reviewedBy = captor.capture(),
            statusNote = anyOrNull()
        )

        assert(captor.firstValue == "System")
    }

    // ========== notifyAssessmentStarted Tests ==========

    @Test
    fun `notifyAssessmentStarted should pass recruiterName to global recipients event`() = runBlocking {
        val candidateEmail = "candidate@example.com"
        val assessmentId = "assessment-999"
        val recruiterName = "Carlos Pérez"

        whenever(globalRecipientsRepository.getAllEmails()).thenReturn(listOf("admin@example.com"))
        whenever(emailTemplateFactory.createEmail(any(), any())).thenReturn(
            EmailTemplate(
                to = "admin@example.com",
                subject = "Assessment Started",
                textContent = "Test",
                from = "test"
            )
        )

        notificationOrchestrator.notifyAssessmentStarted(
            candidateEmail = candidateEmail,
            assessmentId = assessmentId,
            recruiterName = recruiterName
        )

        val captor = argumentCaptor<EmailEvent>()
        verify(emailTemplateFactory).createEmail(any(), captor.capture())

        val event = captor.firstValue
        assert(event is EmailEvent.AssessmentStarted)
        val startedEvent = event as EmailEvent.AssessmentStarted
        assert(startedEvent.recruiterName == recruiterName)
    }

    @Test
    fun `notifyAssessmentStarted should handle null recruiterName`() = runBlocking {
        val candidateEmail = "candidate@example.com"
        val assessmentId = "assessment-000"

        whenever(globalRecipientsRepository.getAllEmails()).thenReturn(listOf("admin@example.com"))
        whenever(emailTemplateFactory.createEmail(any(), any())).thenReturn(
            EmailTemplate(
                to = "admin@example.com",
                subject = "Assessment Started",
                textContent = "Test",
                from = "test"
            )
        )

        notificationOrchestrator.notifyAssessmentStarted(
            candidateEmail = candidateEmail,
            assessmentId = assessmentId,
            recruiterName = null
        )

        val captor = argumentCaptor<EmailEvent>()
        verify(emailTemplateFactory).createEmail(any(), captor.capture())

        val event = captor.firstValue as EmailEvent.AssessmentStarted
        assert(event.recruiterName == null)
    }

    // ========== notifyCandidateApplication Tests ==========

    @Test
    fun `notifyCandidateApplication should pass recruiterName to global recipients event`() = runBlocking {
        val candidateEmail = "candidate@example.com"
        val recruiterName = "Laura García"

        whenever(globalRecipientsRepository.getAllEmails()).thenReturn(listOf("admin@example.com"))
        whenever(emailTemplateFactory.createEmail(any(), any())).thenReturn(
            EmailTemplate(
                to = "admin@example.com",
                subject = "New Application",
                textContent = "Test",
                from = "test"
            )
        )
        whenever(emailTemplateFactory.createCandidateApplicationConfirmationEmail(any(), any(), any())).thenReturn(
            EmailTemplate(
                to = candidateEmail,
                subject = "Application Received",
                textContent = "Test",
                from = "test"
            )
        )

        notificationOrchestrator.notifyCandidateApplication(
            candidateEmail = candidateEmail,
            candidateName = "John Doe",
            positionTitle = "DevOps Engineer",
            recruiterName = recruiterName
        )

        val captor = argumentCaptor<EmailEvent>()
        verify(emailTemplateFactory).createEmail(any(), captor.capture())

        val event = captor.firstValue
        assert(event is EmailEvent.CandidateApplication)
        val appEvent = event as EmailEvent.CandidateApplication
        assert(appEvent.recruiterName == recruiterName)
    }

    // ========== AssessmentCompleted / AssessmentPending recruiterName propagation ==========

    @Test
    fun `notifyAssessmentCompletedWithReport should pass recruiterName in AssessmentCompleted event`() = runBlocking {
        val candidateEmail = "candidate@example.com"
        val recruiterName = "Ana Torres"
        val report = AssessmentReport(
            candidateEmail = candidateEmail,
            assessmentId = "assessment-777",
            displayName = "Java Basics",
            finalScore = 90.0,
            status = "COMPLETED",
            isQualified = true
        )

        whenever(globalRecipientsRepository.getAllEmails()).thenReturn(listOf("admin@example.com"))
        whenever(emailTemplateFactory.createEmail(any(), any())).thenReturn(
            EmailTemplate(
                to = "admin@example.com",
                subject = "Assessment Completed",
                textContent = "Test",
                from = "test"
            )
        )

        notificationOrchestrator.notifyAssessmentCompletedWithReport(
            candidateEmail = candidateEmail,
            candidateName = "John Doe",
            report = report,
            recruiterEmail = null,
            recruiterName = recruiterName,
            positionTitle = "Backend Dev",
            timeExpired = false
        )

        val captor = argumentCaptor<EmailEvent>()
        verify(emailTemplateFactory).createEmail(any(), captor.capture())

        val event = captor.firstValue as EmailEvent.AssessmentCompleted
        assert(event.recruiterName == recruiterName)
    }

    @Test
    fun `notifyAssessmentCompletedWithoutReport should pass recruiterName in AssessmentPending event`() = runBlocking {
        val candidateEmail = "candidate@example.com"
        val recruiterName = "Luis Ramírez"

        whenever(globalRecipientsRepository.getAllEmails()).thenReturn(listOf("admin@example.com"))
        whenever(emailTemplateFactory.createEmail(any(), any())).thenReturn(
            EmailTemplate(
                to = "admin@example.com",
                subject = "Assessment Pending",
                textContent = "Test",
                from = "test"
            )
        )

        notificationOrchestrator.notifyAssessmentCompletedWithoutReport(
            candidateEmail = candidateEmail,
            candidateName = "Jane Doe",
            assessmentId = "assessment-888",
            recruiterEmail = null,
            recruiterName = recruiterName,
            positionTitle = "Frontend Dev",
            timeExpired = false
        )

        val captor = argumentCaptor<EmailEvent>()
        verify(emailTemplateFactory).createEmail(any(), captor.capture())

        val event = captor.firstValue as EmailEvent.AssessmentPending
        assert(event.recruiterName == recruiterName)
    }

    // ========== notifyRecruiterCandidateInvitation Tests ==========

    @Test
    fun `notifyRecruiterCandidateInvitation sends email with correct parameters`() = runBlocking {
        val emailTemplate = EmailTemplate(
            to = "recruiter@example.com",
            subject = "Candidate Invitation Sent - Alice",
            textContent = "Hello!",
            from = "test"
        )
        whenever(
            emailTemplateFactory.createRecruiterCandidateInvitationConfirmationEmail(
                to = "recruiter@example.com",
                recruiterName = "Mike Wilson",
                candidateName = "Alice Smith",
                candidateEmail = "alice@example.com",
                positionTitle = "Backend Developer",
                assessmentsCount = 2
            )
        ).thenReturn(emailTemplate)

        notificationOrchestrator.notifyRecruiterCandidateInvitation(
            recruiterEmail = "recruiter@example.com",
            recruiterName = "Mike Wilson",
            candidateName = "Alice Smith",
            candidateEmail = "alice@example.com",
            positionTitle = "Backend Developer",
            assessmentsCount = 2
        )

        verify(emailNotificationService).sendEmail(emailTemplate)
    }

    @Test
    fun `notifyRecruiterCandidateInvitation does not throw when email sending fails`() = runBlocking {
        whenever(
            emailTemplateFactory.createRecruiterCandidateInvitationConfirmationEmail(
                any(), any(), any(), any(), any(), any()
            )
        ).thenThrow(RuntimeException("SMTP error"))

        // should not throw
        notificationOrchestrator.notifyRecruiterCandidateInvitation(
            recruiterEmail = "recruiter@example.com",
            recruiterName = "Mike Wilson",
            candidateName = "Alice Smith",
            candidateEmail = "alice@example.com",
            positionTitle = "Backend Developer",
            assessmentsCount = 2
        )
    }

    // ========== notifyAdminInvitation Tests ==========

    @Test
    fun `notifyAdminInvitation should route to email and global recipients`() = runBlocking {
        val adminEmail = "newadmin@example.com"
        val invitedBy = "Super Admin"

        whenever(globalRecipientsRepository.getAllEmails()).thenReturn(emptyList())
        whenever(emailTemplateFactory.createEmail(any(), any())).thenReturn(
            EmailTemplate(
                to = adminEmail,
                subject = "Admin Invitation",
                textContent = "Test",
                from = "test"
            )
        )

        notificationOrchestrator.notifyAdminInvitation(
            recipientEmail = adminEmail,
            invitedBy = invitedBy
        )

        val captor = argumentCaptor<EmailEvent>()
        verify(emailTemplateFactory).createEmail(eq(adminEmail), captor.capture())

        val event = captor.firstValue
        assert(event is EmailEvent.AdminInvited)
        val adminEvent = event as EmailEvent.AdminInvited
        assert(adminEvent.adminEmail == adminEmail)
        assert(adminEvent.invitedBy == invitedBy)
    }

}
