package org.example.notifier.domain.invitation

import java.time.LocalDateTime
import java.util.UUID
import org.example.notifier.domain.user.UserRole

data class RecruiterInvitation(
    val id: String = UUID.randomUUID().toString(),
    val email: String,
    val invitedBy: String,
    val assignedPositions: List<String> = emptyList(),
    val status: String = InvitationStatus.PENDING,
    val role: String = UserRole.RECRUITER,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val expiresAt: LocalDateTime = LocalDateTime.now().plusDays(3),
    var acceptedAt: LocalDateTime? = null
) {
    object InvitationStatus {
        const val PENDING = "PENDING"
        const val ACCEPTED = "ACCEPTED"
        const val EXPIRED = "EXPIRED"
        const val REVOKED = "REVOKED"
    }

    fun isValid(): Boolean {
        return status == InvitationStatus.PENDING &&
               LocalDateTime.now().isBefore(expiresAt)
    }
}