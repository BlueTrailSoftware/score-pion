package org.example.notifier.application.service.core.impl

import org.example.notifier.application.service.integration.AssessmentInfo
import org.example.notifier.application.service.integration.AssessmentPlatformService
import org.example.notifier.domain.shared.AssessmentReport
import org.example.notifier.infrastructure.adapter.coderbyte.mapper.CoderbyteReportMapper
import org.example.notifier.infrastructure.client.CoderbyteApiClient
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class CoderbyteAssessmentService(
    private val coderbyteApiClient: CoderbyteApiClient
) : AssessmentPlatformService {

    private val logger = LoggerFactory.getLogger(CoderbyteAssessmentService::class.java)

    override suspend fun getCandidateReport(
        candidateEmail: String,
        assessmentId: String
    ): AssessmentReport? {
        return try {
            val response = coderbyteApiClient.getCandidateReport(candidateEmail, assessmentId)

            val coderbyteReport = response.data.reports.firstOrNull()

            if (coderbyteReport == null) {
                logger.warn("No report found in Coderbyte response for candidate: {}", candidateEmail)
                return null
            }

            CoderbyteReportMapper.toDomainReport(
                coderbyteReport = coderbyteReport,
                candidateEmail = candidateEmail,
                assessmentId = assessmentId
            )
        } catch (e: Exception) {
            logger.error("Error fetching Coderbyte report for $candidateEmail", e)
            null
        }
    }

    @Cacheable("assessments", unless = "#result == null")
    override suspend fun getAvailableAssessments(): List<AssessmentInfo> {
        logger.info("Fetching assessments from Coderbyte API")
        val response = coderbyteApiClient.getAssessments()

        return response.data?.assessments.orEmpty()
            .filter { it.displayName != null && it.testID != null }
            .map { assessment ->
                AssessmentInfo(
                    id = assessment.testID!!,
                    title = assessment.displayName!!,
                    publicUrl = assessment.publicURL,
                    metadata = mapOf("provider" to "coderbyte")
                )
            }
            .also { logger.info("Successfully processed ${it.size} assessments") }
    }

    override suspend fun sendCandidateInvitation(email: String, publicUrl: String) {
        coderbyteApiClient.inviteCandidate(email, publicUrl)
    }
}