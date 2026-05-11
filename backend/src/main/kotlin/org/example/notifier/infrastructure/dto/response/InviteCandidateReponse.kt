package org.example.notifier.infrastructure.dto.response

data class InviteCandidateResponse(
    val status: String,
    val data: CandidateData
)

data class CandidateData(
    val candidates: List<CandidateInfo>,
    val test_id: String
)

data class CandidateInfo(
    val email: String,
    val url: String
)
