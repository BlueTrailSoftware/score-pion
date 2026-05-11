package org.example.notifier.application.service.core.impl

import kotlinx.coroutines.runBlocking
import org.example.notifier.infrastructure.client.CoderbyteApiClient
import org.example.notifier.infrastructure.dto.response.AssessmentsResponse
import org.example.notifier.infrastructure.external.CoderbyteResponse
import org.example.notifier.infrastructure.external.Report
import org.example.notifier.infrastructure.external.ReportData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class CoderbyteAssessmentServiceTest {

    private lateinit var coderbyteApiClient: CoderbyteApiClient
    private lateinit var service: CoderbyteAssessmentService

    @BeforeEach
    fun setup() {
        coderbyteApiClient = mock(CoderbyteApiClient::class.java)
        service = CoderbyteAssessmentService(coderbyteApiClient)
    }

    // --- getAvailableAssessments ---

    @Test
    fun `getAvailableAssessments maps id, title and publicUrl correctly`() = runBlocking<Unit> {
        whenever(coderbyteApiClient.getAssessments()).thenReturn(
            buildAssessmentsResponse(listOf(
                AssessmentsResponse.Assessment(displayName = "Java Test", testID = "java-1", publicURL = "https://coderbyte.com/java-1")
            ))
        )

        val result = service.getAvailableAssessments()

        assertEquals(1, result.size)
        assertEquals("java-1", result[0].id)
        assertEquals("Java Test", result[0].title)
        assertEquals("https://coderbyte.com/java-1", result[0].publicUrl)
        assertEquals("coderbyte", result[0].metadata["provider"])
    }

    @Test
    fun `getAvailableAssessments returns publicUrl as null when assessment has no publicURL`() = runBlocking<Unit> {
        whenever(coderbyteApiClient.getAssessments()).thenReturn(
            buildAssessmentsResponse(listOf(
                AssessmentsResponse.Assessment(displayName = "Java Test", testID = "java-1", publicURL = null)
            ))
        )

        val result = service.getAvailableAssessments()

        assertEquals(1, result.size)
        assertNull(result[0].publicUrl)
    }

    @Test
    fun `getAvailableAssessments filters out assessments with null displayName`() = runBlocking<Unit> {
        whenever(coderbyteApiClient.getAssessments()).thenReturn(
            buildAssessmentsResponse(listOf(
                AssessmentsResponse.Assessment(displayName = null, testID = "java-1", publicURL = "https://coderbyte.com/java-1"),
                AssessmentsResponse.Assessment(displayName = "SQL Test", testID = "sql-2", publicURL = "https://coderbyte.com/sql-2")
            ))
        )

        val result = service.getAvailableAssessments()

        assertEquals(1, result.size)
        assertEquals("sql-2", result[0].id)
    }

    @Test
    fun `getAvailableAssessments filters out assessments with null testID`() = runBlocking<Unit> {
        whenever(coderbyteApiClient.getAssessments()).thenReturn(
            buildAssessmentsResponse(listOf(
                AssessmentsResponse.Assessment(displayName = "Java Test", testID = null, publicURL = "https://coderbyte.com/java-1"),
                AssessmentsResponse.Assessment(displayName = "SQL Test", testID = "sql-2", publicURL = "https://coderbyte.com/sql-2")
            ))
        )

        val result = service.getAvailableAssessments()

        assertEquals(1, result.size)
        assertEquals("sql-2", result[0].id)
    }

    @Test
    fun `getAvailableAssessments returns empty list when response has no data`() = runBlocking<Unit> {
        whenever(coderbyteApiClient.getAssessments()).thenReturn(
            AssessmentsResponse(data = null, status = "success")
        )

        val result = service.getAvailableAssessments()

        assertEquals(0, result.size)
    }

    @Test
    fun `getAvailableAssessments maps multiple assessments`() = runBlocking<Unit> {
        whenever(coderbyteApiClient.getAssessments()).thenReturn(
            buildAssessmentsResponse(listOf(
                AssessmentsResponse.Assessment(displayName = "Java Test", testID = "java-1", publicURL = "https://coderbyte.com/java-1"),
                AssessmentsResponse.Assessment(displayName = "SQL Test", testID = "sql-2", publicURL = "https://coderbyte.com/sql-2"),
                AssessmentsResponse.Assessment(displayName = "Kotlin Test", testID = "kotlin-3", publicURL = null)
            ))
        )

        val result = service.getAvailableAssessments()

        assertEquals(3, result.size)
        assertEquals("java-1", result[0].id)
        assertEquals("sql-2", result[1].id)
        assertEquals("kotlin-3", result[2].id)
        assertNull(result[2].publicUrl)
    }

    // --- sendCandidateInvitation ---

    @Test
    fun `sendCandidateInvitation delegates to coderbyteApiClient with correct args`() = runBlocking<Unit> {
        whenever(coderbyteApiClient.inviteCandidate(any(), any())).thenReturn(mock())

        service.sendCandidateInvitation("cand@example.com", "https://coderbyte.com/java-1")

        verify(coderbyteApiClient).inviteCandidate("cand@example.com", "https://coderbyte.com/java-1")
    }

    @Test
    fun `sendCandidateInvitation propagates exception from client`() = runBlocking<Unit> {
        whenever(coderbyteApiClient.inviteCandidate(any(), any()))
            .thenThrow(RuntimeException("Coderbyte API unavailable"))

        assertThrows<RuntimeException> {
            service.sendCandidateInvitation("cand@example.com", "https://coderbyte.com/java-1")
        }
    }

    // --- getCandidateReport ---

    @Test
    fun `getCandidateReport returns mapped domain report on success`() = runBlocking<Unit> {
        val report = buildReport(email = "cand@example.com", displayName = "Java Test", finalScore = 85)
        whenever(coderbyteApiClient.getCandidateReport("cand@example.com", "java-1"))
            .thenReturn(CoderbyteResponse(status = "success", data = ReportData(reports = listOf(report))))

        val result = service.getCandidateReport("cand@example.com", "java-1")

        assertNotNull(result)
        assertEquals("cand@example.com", result!!.candidateEmail)
        assertEquals("java-1", result.assessmentId)
        assertEquals("Java Test", result.displayName)
        assertEquals(85.0, result.finalScore)
    }

    @Test
    fun `getCandidateReport returns null when response has no reports`() = runBlocking<Unit> {
        whenever(coderbyteApiClient.getCandidateReport("cand@example.com", "java-1"))
            .thenReturn(CoderbyteResponse(status = "success", data = ReportData(reports = emptyList())))

        val result = service.getCandidateReport("cand@example.com", "java-1")

        assertNull(result)
    }

    @Test
    fun `getCandidateReport returns null when client throws`() = runBlocking<Unit> {
        whenever(coderbyteApiClient.getCandidateReport(any(), any()))
            .thenThrow(RuntimeException("Coderbyte API error"))

        val result = service.getCandidateReport("cand@example.com", "java-1")

        assertNull(result)
    }

    // --- helpers ---

    private fun buildAssessmentsResponse(assessments: List<AssessmentsResponse.Assessment>) =
        AssessmentsResponse(
            status = "success",
            data = AssessmentsResponse.AssessmentData(assessments = assessments)
        )

    private fun buildReport(
        email: String = "cand@example.com",
        displayName: String = "Test",
        finalScore: Int = 0
    ) = Report(
        username = null,
        email = email,
        dateJoined = null,
        testId = null,
        displayName = displayName,
        reportLink = null,
        status = "completed",
        timeTaken = null,
        totalChallenges = null,
        voteDecision = null,
        invitedByAdmin = null,
        finalScore = finalScore,
        qualified = true,
        qualifyingScore = null,
        mcScore = null,
        codeScore = null,
        cheatingDetails = null,
        reportReady = true,
        cheatingFlag = null
    )
}