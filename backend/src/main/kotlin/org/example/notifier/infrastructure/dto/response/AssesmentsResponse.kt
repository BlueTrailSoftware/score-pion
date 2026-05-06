package org.example.notifier.infrastructure.dto.response

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonIgnoreProperties(ignoreUnknown = true)
data class AssessmentsResponse(
    val data: AssessmentData? = null,
    val status: String? = null
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class AssessmentData(
        val assessments: List<Assessment> = emptyList()
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
    data class Assessment(
        @JsonAlias("publicUrl", "public_url", "publicURL")
        val publicURL: String? = null,

        val overviewStats: OverviewStats? = null,
        val closed: Boolean? = null,
        val createdDate: String? = null,
        val workspaces: List<String> = emptyList(),
        val displayName: String? = null,

        @JsonAlias("testId", "test_id", "testID")
        val testID: String? = null
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
    data class OverviewStats(
        val qualified: Int? = null,
        val assessed: Long? = null,
        val projectCount: Long? = null,
        val challengeCount: Long? = null,
        val qualifyingScore: Long? = null,
        val openEndedCount: Long? = null,
        val codeEditorTemplateID: String? = null,
        val total: Long? = null,
        val multipleChoiceCount: Long? = null,
        val emailTemplateID: String? = null,
        val candidatesCheatedCount: Long? = null,
        val ratings: Ratings? = null,
        val createdByEmail: String? = null,
        val welcomeTemplateID: String? = null
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Ratings(
        val up: Long? = null,
        val down: Long? = null
    )

    data class ExamData(val displayName: String? = null, val testID: String? = null)
}
