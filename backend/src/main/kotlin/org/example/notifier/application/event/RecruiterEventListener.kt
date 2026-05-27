package org.example.notifier.application.event

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.example.notifier.application.service.core.OpenPositionService
import org.example.notifier.domain.event.RecruiterCreatedEvent
import org.example.notifier.domain.invitation.RecruiterInvitation
import org.example.notifier.domain.user.User
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

private val logger = LoggerFactory.getLogger(RecruiterEventListener::class.java)

@Component
class RecruiterEventListener(
    private val openPositionService: OpenPositionService,
    private val applicationScope: CoroutineScope
) {

    @EventListener
    fun onRecruiterCreated(event: RecruiterCreatedEvent) {
        logger.info("Handling recruiter created event for {}", event.recruiter.email)
        applicationScope.launch {
            copyInitialPositionsFromInvitation(event.recruiter, event.invitation)
        }
    }

    private suspend fun copyInitialPositionsFromInvitation(
        recruiter: User,
        invitation: RecruiterInvitation
    ) {
        val positionIds = invitation.assignedPositions
        if (positionIds.isNotEmpty()) {
            try {
                logger.info("Assigning {} positions to recruiter {}", positionIds.size, recruiter.email)
                positionIds.forEach { positionId ->
                    openPositionService.grantRecruiterAccess(
                        recruiterId = recruiter.id,
                        positionId = positionId,
                        grantedBy = invitation.invitedBy
                    )
                    logger.debug("Granted access to position {} for recruiter {}", positionId, recruiter.email)
                }
                logger.info("Successfully assigned all positions to recruiter {}", recruiter.email)
            } catch (e: Exception) {
                logger.error("Failed to copy positions from invitation to recruiter {}: {}", recruiter.email, e.message, e)
            }
        } else {
            logger.debug("No positions to assign for recruiter {}", recruiter.email)
        }
    }
}
