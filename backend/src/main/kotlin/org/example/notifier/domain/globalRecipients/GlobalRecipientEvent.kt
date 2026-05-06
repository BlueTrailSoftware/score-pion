package org.example.notifier.domain.globalRecipients

import org.example.notifier.domain.position.OpenPosition
import org.example.notifier.domain.shared.AssessmentReport

sealed class GlobalRecipientEvent {
    data class CandidateInvited(
        val candidateEmail: String,
        val candidateName: String,
        val positionTitle: String,
        val recruiterName: String,
        val assessmentsCount: Int
    ) : GlobalRecipientEvent()

    data class AssessmentCompleted(
        val candidateEmail: String,
        val candidateName: String?,
        val report: AssessmentReport,
        val positionTitle: String?,
        val timeExpired: Boolean
    ) : GlobalRecipientEvent()

    data class AssessmentPending(
        val candidateEmail: String,
        val candidateName: String?,
        val assessmentId: String,
        val positionTitle: String?,
        val timeExpired: Boolean
    ) : GlobalRecipientEvent()

    data class PositionAssigned(
        val recruiterName: String,
        val positions: List<OpenPosition>,
        val inviteLink: String
    ) : GlobalRecipientEvent()

    data class PositionCreated(
        val positionTitle: String,
        val positionDescription: String,
        val createdBy: String,
        val positionLink: String,
        val isExternal: Boolean,
        val assessmentNames: List<String>,
    ) : GlobalRecipientEvent()
}