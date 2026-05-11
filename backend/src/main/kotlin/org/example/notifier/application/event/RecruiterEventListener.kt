package org.example.notifier.application.event

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.example.notifier.domain.invitation.RecruiterInvitation
import org.example.notifier.domain.user.User
import org.example.notifier.application.service.core.OpenPositionService
import org.slf4j.LoggerFactory
import org.example.notifier.domain.event.RecruiterCreatedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

private val logger = LoggerFactory.getLogger(RecruiterEventListener::class.java)

/**
 * Handles events related to recruiter lifecycle
 * This component is responsible for post-creation tasks like position assignment
 */
@Component
class RecruiterEventListener(
    private val openPositionService: OpenPositionService,
) {

    /**
     * Handle recruiter creation event by copying positions from invitation
     * This runs asynchronously after the recruiter is created
     */
    @EventListener
    fun onRecruiterCreated(event: RecruiterCreatedEvent) {
        logger.info("Handling recruiter created event for {}", event.recruiter.email)
        // Launch in IO dispatcher for async database operations
        CoroutineScope(Dispatchers.IO).launch {
            copyInitialPositionsFromInvitation(event.recruiter, event.invitation)
        }
    }

    /**
     * Copies assigned positions from invitation to newly created recruiter
     */
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
