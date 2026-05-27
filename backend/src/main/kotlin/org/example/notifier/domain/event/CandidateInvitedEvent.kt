package org.example.notifier.domain.event

data class CandidateInvitedEvent(
    val candidateEmail: String,
    val candidateName: String,
    val positionTitle: String,
    val recruiterEmail: String,
    val recruiterName: String,
    val assessmentsCount: Int
)
