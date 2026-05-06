package org.example.notifier.application.model.applicant

import org.example.notifier.application.model.assessment.AssessmentInvitationItem
import org.example.notifier.domain.applicant.Applicant

internal fun Applicant.toApplicantItem(
    positionTitle: String? = null,
    assessments: List<AssessmentInvitationItem>? = null
) = ApplicantItem(
    id = id,
    name = name,
    email = email,
    phone = phone,
    positionId = positionId,
    positionTitle = positionTitle,
    status = status.name,
    source = source,
    createdAt = createdAt,
    updatedAt = updatedAt,
    reviewedBy = reviewedBy,
    reviewedAt = reviewedAt,
    fileUrl = fileUrl,
    linkedinUrl = linkedinUrl,
    isFileDeleted = isFileDeleted,
    statusNote = statusNote,
    assessments = assessments
)