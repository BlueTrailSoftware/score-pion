package org.example.notifier.application.service.integration

import org.example.notifier.domain.shared.AssessmentReport

interface AssessmentPlatformService {

    suspend fun getCandidateReport(
        candidateEmail: String,
        assessmentId: String
    ): AssessmentReport?

    suspend fun getAvailableAssessments(): List<AssessmentInfo>

    suspend fun sendCandidateInvitation(email: String, publicUrl: String)
}

data class AssessmentInfo(
    val id: String,
    val title: String,
    val publicUrl: String? = null,
    val metadata: Map<String, Any> = emptyMap()
)
