package org.example.notifier.application.useCases.getRecruiterPositions

import org.example.notifier.application.model.shared.PagedResult
import org.example.notifier.application.model.shared.toPagedResult
import org.example.notifier.application.model.position.RecruiterPositionItem
import org.example.notifier.application.service.core.OpenPositionService
import org.springframework.stereotype.Component

@Component
class GetRecruiterPositionsUseCase(
    private val openPositionService: OpenPositionService
) {

    suspend fun execute(command: GetRecruiterPositionsCommand): PagedResult<RecruiterPositionItem> {
        val positions = openPositionService.getRecruiterPositions(command.recruiterId)

        return positions.map { position ->
            val assessmentsCount = openPositionService.getPositionAssessments(position.id).size
            RecruiterPositionItem(
                id = position.id,
                title = position.title,
                description = position.description,
                external = position.external,
                assessmentsCount = assessmentsCount,
                isActive = position.isActive,
                createdAt = position.createdAt,
                workMode = position.workMode,
                location = position.location
            )
        }
        .sortedByDescending { it.createdAt }
        .toPagedResult(command.page, command.pageSize)
    }
}
