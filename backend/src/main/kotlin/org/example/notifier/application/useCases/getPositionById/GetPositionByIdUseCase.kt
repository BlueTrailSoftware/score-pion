package org.example.notifier.application.useCases.getPositionById

import org.example.notifier.application.service.core.OpenPositionService
import org.example.notifier.application.model.position.PositionAssessmentItem
import org.example.notifier.application.model.position.PositionResult
import org.springframework.stereotype.Component

@Component
class GetPositionByIdUseCase(
    private val openPositionService: OpenPositionService
) {

    suspend fun execute(command: GetPositionByIdCommand): PositionResult? {
        val position = openPositionService.getPosition(command.positionId) ?: return null

        val assessments = openPositionService.getPositionAssessments(position.id)
            .map { PositionAssessmentItem(it.assessmentId, it.assessmentName, it.addedAt) }

        return PositionResult(
            id = position.id,
            title = position.title,
            description = position.description,
            external = position.external,
            assessments = assessments,
            fileUrl = position.fileUrl,
            createdBy = position.createdBy,
            isActive = position.isActive,
            createdAt = position.createdAt,
            updatedAt = position.updatedAt,
            isFileDeleted = position.isFileDeleted,
            workMode = position.workMode,
            location = position.location,
            jobType = position.jobType,
            experienceMin = position.experienceMin,
            experienceMax = position.experienceMax,
            skills = position.skills
        )
    }
}
