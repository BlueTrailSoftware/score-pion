package org.example.notifier.infrastructure.external

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import org.example.notifier.infrastructure.json.deserializer.VideoResponsesDeserializer


data class CoderbyteResponse(
    val status: String,
    val data: ReportData
)

data class ReportData(
    val reports: List<Report>
)

data class Report(
    @JsonProperty("username")
    val username: String?,
    @JsonProperty("email")
    val email: String?,
    @JsonProperty("date_joined")
    val dateJoined: String?,
    @JsonProperty("test_id")
    val testId: String?,
    @JsonProperty("display_name")
    val displayName: String?,
    @JsonProperty("report_link")
    val reportLink: String?,
    @JsonProperty("status")
    val status: String?,
    @JsonProperty("time_taken")
    val timeTaken: Int?,
    @JsonProperty("total_challenges")
    val totalChallenges: Int?,
    @JsonProperty("challenge_details")
    val challengeDetails: List<Any>? = emptyList(),
    @JsonProperty("video_responses")
    @JsonDeserialize(using = VideoResponsesDeserializer::class)
    val videoResponses: Map<String, Any>? = emptyMap(),
    @JsonProperty("mc_details")
    val mcDetails: List<McDetail>? = emptyList(),
    @JsonProperty("oe_details")
    val oeDetails: List<Any>? = emptyList(),
    @JsonProperty("scorecard")
    val scorecard: Map<String, Any>? = emptyMap(),
    @JsonProperty("admin_notes")
    val adminNotes: Map<String, Any>? = emptyMap(),
    @JsonProperty("vote_decision")
    val voteDecision: String?,
    @JsonProperty("report_ready")
    val reportReady: Boolean?,
    @JsonProperty("workspaces")
    val workspaces: List<String>? = emptyList(),
    @JsonProperty("invited_by_admin")
    val invitedByAdmin: String?,
    @JsonProperty("cheating_flag")
    val cheatingFlag: String?,
    @JsonProperty("cheating_details")
    val cheatingDetails: CheatingDetails? = null,
    @JsonProperty("mc_score")
    val mcScore: Int?,
    @JsonProperty("code_score")
    val codeScore: Int?,
    @JsonProperty("final_score")
    val finalScore: Int?,
    @JsonProperty("qualified")
    val qualified: Boolean?,
    @JsonProperty("qualifying_score")
    val qualifyingScore: Int?
)

data class McDetail(
    @JsonProperty("id")
    val id: String?,
    @JsonProperty("question")
    val question: String?,
    @JsonProperty("correct")
    val correct: Boolean?,
    @JsonProperty("answer")
    val answer: String?,
    @JsonProperty("meta")
    val meta: Meta?
)

data class Meta(
    @JsonProperty("tags")
    val tags: List<String>? = emptyList()
)

data class CheatingDetails(
    @JsonProperty("tab_leaving")
    val tabLeaving: Int,
    @JsonProperty("plagiarism")
    val plagiarism: String,
    @JsonProperty("pasted_code")
    val pastedCode: String,
    @JsonProperty("suspicious_activity")
    val suspiciousActivity: Boolean,
    @JsonProperty("ai_usage")
    val aiUsage: Boolean
)
