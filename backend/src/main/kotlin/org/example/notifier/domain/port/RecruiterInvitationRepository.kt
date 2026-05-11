package org.example.notifier.domain.port

import org.example.notifier.domain.invitation.RecruiterInvitation

interface RecruiterInvitationRepository {
    suspend fun save(invitation: RecruiterInvitation): RecruiterInvitation
    suspend fun findById(id: String): RecruiterInvitation?
    suspend fun findByEmail(email: String): RecruiterInvitation?
    suspend fun findAll(): List<RecruiterInvitation>
    suspend fun delete(invitation: RecruiterInvitation)
}
