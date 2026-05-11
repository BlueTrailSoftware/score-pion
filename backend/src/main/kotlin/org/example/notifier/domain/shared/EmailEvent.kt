package org.example.notifier.domain.shared

import org.example.notifier.domain.position.OpenPosition

sealed class EmailEvent {

    // ========== RECRUITER EVENTS ==========

    data class RecruiterInvited(
        val recruiterEmail: String,
        val adminName: String?,
        val inviteLink: String
    ) : EmailEvent()

    data class AdminInvited(
        val adminEmail: String,
        val invitedBy: String?,
        val inviteLink: String
    ) : EmailEvent()

    // ========== APPLICANT EVENTS ==========

    data class ApplicantApproved(
        val applicantEmail: String,
        val applicantName: String?,
        val positionTitle: String,
        val recruiterName: String?,
    ) : EmailEvent()

    data class ApplicantRejected(
        val applicantEmail: String,
        val applicantName: String?,
        val positionTitle: String,
        val recruiterName: String?
    ) : EmailEvent()

    // ========== CANDIDATE EVENTS ==========

    data class CandidateInvited(
        val candidateEmail: String,
        val candidateName: String,
        val positionTitle: String,
        val recruiterName: String,
        val assessmentsCount: Int
    ) : EmailEvent()

    data class CandidateReport(
        val recruiterEmail: String,
        val recruiterName: String?,
        val candidateEmail: String,
        val candidateName: String?,
        val report: AssessmentReport
    ) : EmailEvent()

    data class CandidateAssessmentPending(
        val recruiterEmail: String,
        val recruiterName: String?,
        val candidateEmail: String,
        val candidateName: String?,
        val assessmentId: String,
        val timeExpired: Boolean
    ) : EmailEvent()

    // ========== ASSESSMENT EVENTS ==========

    data class AssessmentStarted(
        val candidateEmail: String,
        val assessmentId: String,
        val recruiterName: String?
    ) : EmailEvent()

    data class AssessmentCompleted(
        val candidateEmail: String,
        val candidateName: String?,
        val report: AssessmentReport,
        val positionTitle: String?,
        val timeExpired: Boolean,
        val recruiterName: String?
    ) : EmailEvent()

    data class AssessmentPending(
        val candidateEmail: String,
        val candidateName: String?,
        val assessmentId: String,
        val positionTitle: String?,
        val timeExpired: Boolean,
        val recruiterName: String?
    ) : EmailEvent()

    // ========== CANDIDATE APPLICATION EVENTS ==========

    data class CandidateApplication(
        val candidateEmail: String,
        val candidateName: String,
        val positionTitle: String,
        val recruiterName: String?
    ) : EmailEvent()

    // ========== POSITION EVENTS ==========

    data class PositionAssigned(
        val recruiterName: String,
        val positions: List<OpenPosition>,
        val inviteLink: String
    ) : EmailEvent()

    data class PositionCreated(
        val positionTitle: String,
        val positionDescription: String,
        val createdBy: String,
        val positionLink: String,
        val isExternal: Boolean,
        val assessmentNames: List<String>
    ) : EmailEvent()
}

data class AssessmentLink(
    val assessmentName: String,
    val link: String
)