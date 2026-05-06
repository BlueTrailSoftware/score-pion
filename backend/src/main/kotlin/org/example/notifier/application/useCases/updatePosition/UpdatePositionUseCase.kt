package org.example.notifier.application.useCases.updatePosition

import org.example.notifier.application.service.core.OpenPositionService
import org.example.notifier.application.service.integration.AssessmentPlatformService
import org.example.notifier.application.service.file.FileService
import org.example.notifier.application.model.position.PositionAssessmentItem
import org.example.notifier.application.model.position.PositionResult
import org.example.notifier.application.util.validatePositionExtendedFields
import org.springframework.stereotype.Component

@Component
class UpdatePositionUseCase(
    private val openPositionService: OpenPositionService,
    private val assessmentPlatformService: AssessmentPlatformService,
    private val fileService: FileService
) {

    companion object {
        private const val UNKNOWN_ASSESSMENT_NAME = "Unknown Assessment"
    }

    suspend fun execute(command: UpdatePositionCommand): PositionResult {
        validatePositionExtendedFields(
            workMode = command.workMode,
            location = command.location,
            jobType = command.jobType,
            experienceMin = command.experienceMin,
            experienceMax = command.experienceMax,
            skills = command.skills
        )

        val currentPosition = openPositionService.getPosition(command.positionId)
            ?: throw IllegalArgumentException("Position not found with id: ${command.positionId}")

        val availableAssessments = assessmentPlatformService.getAvailableAssessments()
        val assessmentNames = availableAssessments
            .filter { it.id in command.assessmentIds }
            .associate { it.id to (it.title ?: UNKNOWN_ASSESSMENT_NAME) }

        val fileResult = fileService.handleFileUpdate(
            currentFileUrl = currentPosition.fileUrl,
            newFilePart = command.filePart,
            deleteFile = command.deleteFile,
            entityType = "positions",
            entityId = command.positionId
        )

        val position = openPositionService.updatePosition(
            id = command.positionId,
            title = command.title,
            description = command.description,
            external = command.external,
            assessmentIds = command.assessmentIds,
            assessmentNames = assessmentNames,
            fileUrl = fileResult.fileUrl,
            isFileDeleted = fileResult.isFileDeleted,
            workMode = command.workMode,
            location = command.location,
            jobType = command.jobType,
            experienceMin = command.experienceMin,
            experienceMax = command.experienceMax,
            skills = command.skills
        )

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
