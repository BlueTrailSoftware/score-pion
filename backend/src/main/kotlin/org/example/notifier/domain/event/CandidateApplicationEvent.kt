package org.example.notifier.domain.event

data class CandidateApplicationEvent(
    val candidateEmail: String,
    val candidateName: String,
    val positionTitle: String,
    val recruiterName: String? = null
)
