package org.example.notifier.application.useCases.getPublicPositions

import org.example.notifier.application.model.position.PublicPositionItem
import org.example.notifier.application.service.core.OpenPositionService
import org.springframework.stereotype.Component

@Component
class GetPublicPositionsUseCase(
    private val openPositionService: OpenPositionService
) {

    suspend fun execute(): List<PublicPositionItem> {
        return openPositionService.getActivePositions()
            .filter { it.external }
            .sortedByDescending { it.createdAt }
            .map { position ->
                PublicPositionItem(
                    id = position.id,
                    title = position.title,
                    description = position.description,
                    fileUrl = position.fileUrl,
                    createdAt = position.createdAt,
                    workMode = position.workMode,
                    location = position.location,
                    jobType = position.jobType,
                    experienceMin = position.experienceMin,
                    experienceMax = position.experienceMax,
                    skills = position.skills
                )
            }
    }
}