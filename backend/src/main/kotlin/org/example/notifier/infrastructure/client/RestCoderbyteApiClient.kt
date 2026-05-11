package org.example.notifier.infrastructure.client

import org.example.notifier.infrastructure.dto.request.CoderByteInviteCandidateRequest
import org.example.notifier.infrastructure.dto.response.AssessmentsResponse
import org.example.notifier.infrastructure.dto.response.InviteCandidateResponse
import org.example.notifier.infrastructure.external.CoderbyteResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

private val logger = LoggerFactory.getLogger(RestCoderbyteApiClient::class.java)

@Component
@ConditionalOnProperty(name = ["app.debug.enabled"], havingValue = "false", matchIfMissing = true)
class RestCoderbyteApiClient(
    @Value("\${coderbyte.baseUrl}") baseUrl: String,
    @Value("\${coderbyte.apiToken}") apiToken: String
) : CoderbyteApiClient {

    private val webClient = WebClient.builder()
        .baseUrl(baseUrl)
        .defaultHeader("Authorization", "Bearer $apiToken")
        .build()

    override suspend fun getAssessments(): AssessmentsResponse {
        logger.info("Fetching assessments from Coderbyte API")

        return webClient.get()
            .uri("/organization/assessments")
            .retrieve()
            .awaitBody()
    }

    override suspend fun inviteCandidate(email: String, assessmentUrl: String): InviteCandidateResponse {
        logger.info("Inviting candidate {} to assessment", email)

        val requestBody = CoderByteInviteCandidateRequest(
            candidates = listOf(email),
            assessment_url = assessmentUrl
        )

        val response = webClient.post()
            .uri("/organization/candidates/invite")
            .bodyValue(requestBody)
            .retrieve()
            .awaitBody<InviteCandidateResponse>()

        logger.info("Invitation sent - Email: {}, Status: {}", email, response.status)
        return response
    }

    override suspend fun getCandidateReport(email: String, assessmentId: String): CoderbyteResponse {
        logger.info("Fetching report for candidate: {}, assessment: {}", email, assessmentId)

        return webClient.get()
            .uri("/organization/candidates/$email/$assessmentId")
            .retrieve()
            .awaitBody()
    }
}
