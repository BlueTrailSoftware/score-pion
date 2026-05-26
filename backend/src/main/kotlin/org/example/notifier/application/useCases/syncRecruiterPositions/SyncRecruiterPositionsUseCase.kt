package org.example.notifier.application.useCases.syncRecruiterPositions

import org.example.notifier.application.service.core.OpenPositionService
import org.example.notifier.application.service.core.UserService
import org.example.notifier.domain.event.RecruiterPositionsAssignedEvent
import org.example.notifier.domain.position.OpenPosition
import org.example.notifier.infrastructure.logging.LoggerPort
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class SyncRecruiterPositionsUseCase(
    private val openPositionService: OpenPositionService,
    private val userService: UserService,
    private val eventPublisher: ApplicationEventPublisher,
    private val logger: LoggerPort
) {

    suspend fun execute(command: SyncRecruiterPositionsCommand) {
        val currentPositionIds = openPositionService.getRecruiterPositions(command.recruiterId)
            .map { it.id }.toSet()
        val desiredPositionIds = command.positionIds.toSet()

        currentPositionIds
            .filter { it !in desiredPositionIds }
            .forEach { positionId ->
                openPositionService.revokeRecruiterAccess(command.recruiterId, positionId)
                logger.info("Revoked access: recruiter=${command.recruiterId}, position=$positionId")
            }

        desiredPositionIds
            .filter { it !in currentPositionIds }
            .forEach { positionId ->
                try {
                    openPositionService.grantRecruiterAccess(command.recruiterId, positionId, command.grantedBy)
                    logger.info("Granted access: recruiter=${command.recruiterId}, position=$positionId")
                } catch (e: IllegalArgumentException) {
                    logger.error("Failed to grant access to position $positionId: ${e.message}")
                }
            }

        val assignedPositions = openPositionService.getPositionsByIdsBatch(command.positionIds)
        publishRecruiterAssignmentEvent(command.recruiterId, assignedPositions)
    }

    private suspend fun publishRecruiterAssignmentEvent(recruiterId: String, positions: List<OpenPosition>) {
        val recruiter = userService.findById(recruiterId)

        if (recruiter == null) {
            logger.error("Cannot publish position assignment event: Recruiter not found with ID: {}", recruiterId)
            return
        }

        if (recruiter.email.isBlank()) {
            logger.error("Cannot publish position assignment event: Recruiter has no email. ID: {}", recruiterId)
            return
        }

        eventPublisher.publishEvent(
            RecruiterPositionsAssignedEvent(
                recruiterEmail = recruiter.email,
                recruiterName = recruiter.name,
                positions = positions
            )
        )
    }
}
