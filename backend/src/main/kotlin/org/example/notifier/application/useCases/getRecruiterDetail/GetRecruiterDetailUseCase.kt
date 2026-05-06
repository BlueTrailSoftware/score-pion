package org.example.notifier.application.useCases.getRecruiterDetail

import org.example.notifier.application.model.position.RecruiterPositionItem
import org.example.notifier.application.model.user.GetRecruiterDetailResult
import org.example.notifier.application.service.core.OpenPositionService
import org.example.notifier.application.service.core.UserService
import org.springframework.stereotype.Component

@Component
class GetRecruiterDetailUseCase(
    private val userService: UserService,
    private val openPositionService: OpenPositionService
) {

    suspend fun execute(command: GetRecruiterDetailCommand): GetRecruiterDetailResult? {
        val recruiter = userService.findById(command.recruiterId) ?: return null

        val positions = openPositionService.getRecruiterPositions(recruiter.id)
        val positionItems = positions.map { position ->
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

        return GetRecruiterDetailResult(
            id = recruiter.id,
            email = recruiter.email,
            name = recruiter.name,
            role = recruiter.role,
            isActive = recruiter.isActive,
            positions = positionItems,
            positionsCount = positions.size,
            createdAt = recruiter.createdAt
        )
    }
}