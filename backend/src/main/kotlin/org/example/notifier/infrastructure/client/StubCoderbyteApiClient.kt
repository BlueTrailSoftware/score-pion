package org.example.notifier.infrastructure.client

import org.example.notifier.infrastructure.dto.response.AssessmentsResponse
import org.example.notifier.infrastructure.dto.response.CandidateData
import org.example.notifier.infrastructure.dto.response.InviteCandidateResponse
import org.example.notifier.infrastructure.external.CoderbyteResponse
import org.example.notifier.infrastructure.external.Report
import org.example.notifier.infrastructure.external.ReportData
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

private val logger = LoggerFactory.getLogger(StubCoderbyteApiClient::class.java)

/**
 * Stub implementation of CoderbyteApiClient for local development (app.debug.enabled=true).
 *
 * Read operations return realistic fixture data so the UI remains functional.
 * Write operations (inviteCandidate) are no-ops — no HTTP call is made.
 */
@Component
@ConditionalOnProperty(name = ["app.debug.enabled"], havingValue = "true")
class StubCoderbyteApiClient : CoderbyteApiClient {

    override suspend fun getAssessments(): AssessmentsResponse {
        logger.info("[DEBUG] Returning stub Coderbyte assessments")
        return AssessmentsResponse(
            status = "success",
            data = AssessmentsResponse.AssessmentData(
                assessments = listOf(
                    AssessmentsResponse.Assessment(
                        testID = "stub-001",
                        displayName = "Stub: Frontend Developer Assessment",
                        publicURL = "https://coderbyte.com/sl/stub-frontend"
                    ),
                    AssessmentsResponse.Assessment(
                        testID = "stub-002",
                        displayName = "Stub: Backend Developer Assessment",
                        publicURL = "https://coderbyte.com/sl/stub-backend"
                    ),
                    AssessmentsResponse.Assessment(
                        testID = "stub-003",
                        displayName = "Stub: Full Stack Assessment",
                        publicURL = "https://coderbyte.com/sl/stub-fullstack"
                    )
                )
            )
        )
    }

    override suspend fun inviteCandidate(email: String, assessmentUrl: String): InviteCandidateResponse {
        logger.info("[DEBUG] Skipping Coderbyte invitation for: {}", email)
        return InviteCandidateResponse(status = "debug", data = CandidateData(candidates = emptyList(), test_id = ""))
    }

    override suspend fun getCandidateReport(email: String, assessmentId: String): CoderbyteResponse {
        logger.info("[DEBUG] Returning stub Coderbyte report for: {} / assessment: {}", email, assessmentId)
        return CoderbyteResponse(
            status = "success",
            data = ReportData(
                reports = listOf(
                    Report(
                        username = email.substringBefore("@"),
                        email = email,
                        dateJoined = null,
                        testId = assessmentId,
                        displayName = "Stub: $assessmentId",
                        reportLink = "https://coderbyte.com/report/stub:$assessmentId",
                        status = "completed",
                        timeTaken = 45,
                        totalChallenges = 5,
                        challengeDetails = emptyList(),
                        videoResponses = emptyMap(),
                        mcDetails = emptyList(),
                        oeDetails = emptyList(),
                        scorecard = emptyMap(),
                        adminNotes = emptyMap(),
                        voteDecision = null,
                        reportReady = true,
                        workspaces = emptyList(),
                        invitedByAdmin = null,
                        cheatingFlag = "NONE",
                        cheatingDetails = null,
                        mcScore = 80,
                        codeScore = 75,
                        finalScore = 78,
                        qualified = true,
                        qualifyingScore = 70
                    )
                )
            )
        )
    }
}