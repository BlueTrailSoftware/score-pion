package org.example.notifier.infrastructure.client

import org.example.notifier.infrastructure.dto.response.AssessmentsResponse
import org.example.notifier.infrastructure.dto.response.InviteCandidateResponse
import org.example.notifier.infrastructure.external.CoderbyteResponse

/**
 * Coderbyte API client abstraction
 */
interface CoderbyteApiClient {

    /**
     * Fetches all assessments from Coderbyte
     */
    suspend fun getAssessments(): AssessmentsResponse

    /**
     * Invites a candidate to an assessment
     */
    suspend fun inviteCandidate(email: String, assessmentUrl: String): InviteCandidateResponse

    /**
     * Retrieves candidate's assessment report
     */
    suspend fun getCandidateReport(email: String, assessmentId: String): CoderbyteResponse
}
