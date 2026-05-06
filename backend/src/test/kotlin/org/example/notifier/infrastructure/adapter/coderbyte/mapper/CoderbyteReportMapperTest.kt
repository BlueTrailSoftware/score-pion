package org.example.notifier.infrastructure.adapter.coderbyte.mapper

import org.example.notifier.infrastructure.external.CheatingDetails
import org.example.notifier.infrastructure.external.McDetail
import org.example.notifier.infrastructure.external.Meta
import org.example.notifier.infrastructure.external.Report
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CoderbyteReportMapperTest {

    private fun buildReport(
        displayName: String? = "Kotlin Assessment",
        finalScore: Int? = 85,
        mcScore: Int? = 70,
        codeScore: Int? = 90,
        qualifyingScore: Int? = 75,
        status: String? = "completed",
        qualified: Boolean? = true,
        mcDetails: List<McDetail>? = null,
        cheatingDetails: CheatingDetails? = null,
        username: String? = "john.doe",
        dateJoined: String? = "2024-01-15",
        testId: String? = "test-42",
        totalChallenges: Int? = 10,
        reportReady: Boolean? = true,
        cheatingFlag: String? = "none"
    ) = Report(
        username = username,
        email = null,
        dateJoined = dateJoined,
        testId = testId,
        displayName = displayName,
        reportLink = "https://coderbyte.com/report/1",
        status = status,
        timeTaken = 45,
        totalChallenges = totalChallenges,
        challengeDetails = emptyList(),
        videoResponses = emptyMap(),
        mcDetails = mcDetails,
        oeDetails = emptyList(),
        scorecard = emptyMap(),
        adminNotes = emptyMap(),
        voteDecision = null,
        reportReady = reportReady,
        workspaces = emptyList(),
        invitedByAdmin = null,
        cheatingFlag = cheatingFlag,
        cheatingDetails = cheatingDetails,
        mcScore = mcScore,
        codeScore = codeScore,
        finalScore = finalScore,
        qualified = qualified,
        qualifyingScore = qualifyingScore
    )

    @Test
    fun `maps candidateEmail and assessmentId from parameters`() {
        val result = CoderbyteReportMapper.toDomainReport(buildReport(), "candidate@test.com", "assessment-1")

        assertEquals("candidate@test.com", result.candidateEmail)
        assertEquals("assessment-1", result.assessmentId)
    }

    @Test
    fun `maps displayName from report`() {
        val result = CoderbyteReportMapper.toDomainReport(buildReport(displayName = "Java Test"), "a@b.com", "id-1")

        assertEquals("Java Test", result.displayName)
    }

    @Test
    fun `displayName null maps to empty string`() {
        val result = CoderbyteReportMapper.toDomainReport(buildReport(displayName = null), "a@b.com", "id-1")

        assertEquals("", result.displayName)
    }

    @Test
    fun `maps scores correctly`() {
        val result = CoderbyteReportMapper.toDomainReport(
            buildReport(finalScore = 80, mcScore = 60, codeScore = 95, qualifyingScore = 70),
            "a@b.com", "id-1"
        )

        assertEquals(80.0, result.finalScore)
        assertEquals(60, result.mcScore)
        assertEquals(95, result.codeScore)
        assertEquals(70, result.qualifyingScore)
    }

    @Test
    fun `finalScore null maps to 0 dot 0`() {
        val result = CoderbyteReportMapper.toDomainReport(buildReport(finalScore = null), "a@b.com", "id-1")

        assertEquals(0.0, result.finalScore)
    }

    @Test
    fun `qualified null maps to false`() {
        val result = CoderbyteReportMapper.toDomainReport(buildReport(qualified = null), "a@b.com", "id-1")

        assertFalse(result.isQualified)
    }

    @Test
    fun `isQualified reflects qualified flag`() {
        assertTrue(CoderbyteReportMapper.toDomainReport(buildReport(qualified = true), "a@b.com", "id-1").isQualified)
        assertFalse(CoderbyteReportMapper.toDomainReport(buildReport(qualified = false), "a@b.com", "id-1").isQualified)
    }

    @Test
    fun `cheatingDetails null maps to null`() {
        val result = CoderbyteReportMapper.toDomainReport(buildReport(cheatingDetails = null), "a@b.com", "id-1")

        assertNull(result.cheatingDetails)
    }

    @Test
    fun `cheatingDetails maps all fields correctly`() {
        val details = CheatingDetails(
            tabLeaving = 3,
            plagiarism = "none",
            pastedCode = "low",
            suspiciousActivity = true,
            aiUsage = false
        )
        val result = CoderbyteReportMapper.toDomainReport(buildReport(cheatingDetails = details), "a@b.com", "id-1")

        assertNotNull(result.cheatingDetails)
        assertEquals(3, result.cheatingDetails!!.tabLeaving)
        assertEquals("none", result.cheatingDetails!!.plagiarism)
        assertEquals("low", result.cheatingDetails!!.pastedCode)
        assertTrue(result.cheatingDetails!!.suspiciousActivity)
        assertFalse(result.cheatingDetails!!.aiUsage)
    }

    @Test
    fun `mcDetails null maps to null`() {
        val result = CoderbyteReportMapper.toDomainReport(buildReport(mcDetails = null), "a@b.com", "id-1")

        assertNull(result.mcDetails)
    }

    @Test
    fun `mcDetails maps with tags from meta`() {
        val mcDetail = McDetail(
            id = "q1",
            question = "What is Kotlin?",
            correct = true,
            answer = "A JVM language",
            meta = Meta(tags = listOf("kotlin", "language"))
        )
        val result = CoderbyteReportMapper.toDomainReport(buildReport(mcDetails = listOf(mcDetail)), "a@b.com", "id-1")

        assertNotNull(result.mcDetails)
        assertEquals(1, result.mcDetails!!.size)
        assertEquals("q1", result.mcDetails!![0].id)
        assertEquals(true, result.mcDetails!![0].correct)
        assertEquals(listOf("kotlin", "language"), result.mcDetails!![0].tags)
    }

    @Test
    fun `metadata contains correct keys and provider`() {
        val result = CoderbyteReportMapper.toDomainReport(
            buildReport(username = "alice", dateJoined = "2024-01-01", testId = "t-99",
                totalChallenges = 5, reportReady = true, cheatingFlag = "low"),
            "a@b.com", "id-1"
        )

        assertEquals("coderbyte", result.metadata["provider"])
        assertEquals("alice", result.metadata["username"])
        assertEquals("2024-01-01", result.metadata["dateJoined"])
        assertEquals("t-99", result.metadata["testId"])
        assertEquals(5, result.metadata["totalChallenges"])
        assertEquals(true, result.metadata["reportReady"])
        assertEquals("low", result.metadata["cheatingFlag"])
    }
}
