package org.example.notifier.infrastructure.dto.request

data class CoderByteInviteCandidateRequest(
    val candidates: List<String>,
    val assessment_url: String
)
