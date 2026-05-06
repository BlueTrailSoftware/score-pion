package org.example.notifier.application.useCases.inviteUser

import java.time.LocalDateTime

data class InviteUserResult(
    val id: String,
    val email: String,
    val invitedBy: String,
    val assignedPositions: List<String>,
    val status: String,
    val createdAt: LocalDateTime,
    val expiresAt: LocalDateTime,
    val acceptedAt: LocalDateTime? = null
)
