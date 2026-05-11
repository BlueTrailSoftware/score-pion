package org.example.notifier.application.service.core

import org.example.notifier.domain.invitation.RecruiterInvitation

interface RecruiterInvitationService {
    suspend fun createInvitation(email: String, invitedBy: String, positionIds: List<String>? = null, role: String? = null): RecruiterInvitation
    suspend fun findByEmail(email: String): RecruiterInvitation?
    suspend fun acceptInvitation(invitation: RecruiterInvitation): RecruiterInvitation
    suspend fun revokeInvitation(email: String): Boolean
    suspend fun getAllPendingInvitations(): List<RecruiterInvitation>
    suspend fun getAllInvitations(): List<RecruiterInvitation>
}
