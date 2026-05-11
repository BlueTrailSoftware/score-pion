package org.example.notifier.application.useCases.getAllPositions

import org.example.notifier.application.service.core.OpenPositionService
import org.example.notifier.application.model.position.PositionSummaryItem
import org.example.notifier.application.model.shared.PagedResult
import org.example.notifier.application.model.shared.toPagedResult
import org.springframework.stereotype.Component

@Component
class GetAllPositionsUseCase(
    private val openPositionService: OpenPositionService
) {

    suspend fun execute(command: GetAllPositionsCommand): PagedResult<PositionSummaryItem> {
        val positions = if (command.activeOnly) {
            openPositionService.getActivePositions()
        } else {
            openPositionService.getAllPositions()
        }

        return positions
            .map { position ->
                val assessmentsCount = openPositionService.getPositionAssessments(position.id).size
                PositionSummaryItem(
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
