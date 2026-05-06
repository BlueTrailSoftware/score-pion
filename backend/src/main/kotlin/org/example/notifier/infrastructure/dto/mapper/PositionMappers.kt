package org.example.notifier.infrastructure.dto.mapper

import org.example.notifier.application.model.position.PositionAssessmentItem
import org.example.notifier.application.model.position.PositionResult
import org.example.notifier.application.model.position.PositionSummaryItem
import org.example.notifier.application.model.assessment.AssessmentItem
import org.example.notifier.infrastructure.dto.response.AssessmentsResponse.ExamData
import org.example.notifier.infrastructure.dto.response.PositionAssessmentResponse
import org.example.notifier.infrastructure.dto.response.PositionResponse
import org.example.notifier.infrastructure.dto.response.PositionSummaryResponse

internal fun AssessmentItem.toResponse() =
    ExamData(displayName = displayName, testID = testId)

internal fun PositionAssessmentItem.toResponse() =
    PositionAssessmentResponse(
        assessmentId = assessmentId,
        assessmentName = assessmentName,
        addedAt = addedAt
    )

internal fun PositionResult.toResponse() =
    PositionResponse(
        id = id,
        title = title,
        description = description,
        external = external,
        assessments = assessments.map { it.toResponse() },
        fileUrl = fileUrl,
        createdBy = createdBy,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isFileDeleted = isFileDeleted,
        workMode = workMode,
        location = location,
        jobType = jobType,
        experienceMin = experienceMin,
        experienceMax = experienceMax,
        skills = skills
    )

internal fun PositionSummaryItem.toResponse() =
    PositionSummaryResponse(
        id = id,
        title = title,
        description = description,
        external = external,
        assessmentsCount = assessmentsCount,
        isActive = isActive,
        createdAt = createdAt,
        workMode = workMode,
        location = location
    )
