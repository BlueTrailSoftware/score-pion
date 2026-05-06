package org.example.notifier.application.useCases.inviteCandidate

data class InviteCandidateCommand(
    val candidateEmail: String,
    val candidateName: String,
    val positionId: String,
    val recruiterId: String
)