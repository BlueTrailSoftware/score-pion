package org.example.notifier.application.useCases.createPosition

import org.example.notifier.application.service.core.OpenPositionService
import org.example.notifier.application.service.integration.AssessmentPlatformService
import org.example.notifier.application.service.notification.NotificationOrchestrator
import org.example.notifier.application.model.position.PositionAssessmentItem
import org.example.notifier.application.model.position.PositionResult
import org.example.notifier.application.service.file.FileService
import org.example.notifier.application.util.validatePositionExtendedFields
import org.example.notifier.domain.position.OpenPosition
import org.springframework.stereotype.Component

@Component
class CreatePositionUseCase(
    private val openPositionService: OpenPositionService,
    private val assessmentPlatformService: AssessmentPlatformService,
    private val notificationOrchestrator: NotificationOrchestrator,
    private val fileService: FileService
    ) {

    private companion object {
        private const val UNKNOWN_ASSESSMENT_NAME = "Unknown Assessment"
    }

    suspend fun execute(command: CreatePositionCommand): PositionResult {
        validatePositionExtendedFields(
            workMode = command.workMode,
            location = command.location,
            jobType = command.jobType,
            experienceMin = command.experienceMin,
            experienceMax = command.experienceMax,
            skills = command.skills
        )

        val availableAssessments = assessmentPlatformService.getAvailableAssessments()
        val assessmentNames = availableAssessments
            .filter { it.id in command.assessmentIds }
            .associate { it.id to (it.title ?: UNKNOWN_ASSESSMENT_NAME) }

        val positionDraft = OpenPosition(
            title = command.title,
            description = command.description,
            external = command.external,
            createdBy = command.createdByEmail,
            workMode = command.workMode,
            location = command.location,
            jobType = command.jobType,
            experienceMin = command.experienceMin,
            experienceMax = command.experienceMax,
            skills = command.skills
        )

        val fileUrl = if (command.filePart != null) {
            fileService.upload(command.filePart, "positions", positionDraft.id)
        } else null

        val position = openPositionService.createPosition(
            position = positionDraft.copy(fileUrl = fileUrl),
            assessmentNames = assessmentNames
        )

        val assessments = openPositionService.getPositionAssessments(position.id)
            .map { PositionAssessmentItem(it.assessmentId, it.assessmentName, it.addedAt) }

        notificationOrchestrator.notifyPositionCreated(
            createdBy = command.createdByEmail,
            position = position,
            assessmentNames = assessmentNames.values.toList()
        )

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
