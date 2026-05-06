package org.example.notifier.infrastructure.dto.mapper

import org.example.notifier.application.model.applicant.ApplicantItem
import org.example.notifier.application.model.position.PublicPositionItem
import org.example.notifier.infrastructure.dto.response.ApplicantResponse
import org.example.notifier.infrastructure.dto.response.PublicPositionResponse

internal fun ApplicantItem.toResponse() = ApplicantResponse(
    id = id,
    name = name,
    email = email,
    phone = phone.orEmpty(),
    positionId = positionId,
    positionTitle = positionTitle,
    status = status,
    source = source,
    createdAt = createdAt,
    updatedAt = updatedAt,
    reviewedBy = reviewedBy,
    reviewedAt = reviewedAt,
    fileUrl = fileUrl,
    linkedinUrl = linkedinUrl,
    isFileDeleted = isFileDeleted,
    statusNote = statusNote,
    assessments = assessments?.map { a ->
        org.example.notifier.infrastructure.dto.response.AssessmentInvitationDetail(
            assessmentId = a.assessmentId,
            assessmentName = a.assessmentName,
            status = a.status,
            finalScore = a.finalScore,
            mcScore = a.mcScore,
            codeScore = a.codeScore,
            qualified = a.qualified,
            completedAt = a.completedAt,
            plagiarism = a.plagiarism,
            pastedCode = a.pastedCode,
            suspiciousActivity = a.suspiciousActivity,
            aiUsage = a.aiUsage,
            tabSwitchCount = a.tabSwitchCount
        )
    }
)

internal fun PublicPositionItem.toResponse() = PublicPositionResponse(
    id = id,
    title = title,
    description = description,
    fileUrl = fileUrl,
    createdAt = createdAt,
    workMode = workMode,
    location = location,
    jobType = jobType,
    experienceMin = experienceMin,
    experienceMax = experienceMax,
    skills = skills
)