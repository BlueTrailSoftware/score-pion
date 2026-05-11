package org.example.notifier.infrastructure.dto.request

data class InviteCandidateRequest(
    val email: String,
    val candidateName: String,
    val positionId: String
)
