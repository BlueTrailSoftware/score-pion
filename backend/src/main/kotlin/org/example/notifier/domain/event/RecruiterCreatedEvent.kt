package org.example.notifier.domain.event

import org.example.notifier.domain.invitation.RecruiterInvitation
import org.example.notifier.domain.user.User

/**
 * Event sent when a new recruiter is created from an invitation.
 * This lets you handle follow-up tasks separately, like giving assessments.
 */
data class RecruiterCreatedEvent(
    val recruiter: User,
    val invitation: RecruiterInvitation
)
