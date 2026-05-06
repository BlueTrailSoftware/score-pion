package org.example.notifier.application.model.invitation

import java.time.LocalDateTime

data class InvitationItem(
    val id: String,
    val email: String,
    val invitedBy: String,
    val assignedPositions: List<String>,
    val status: String,
    val createdAt: LocalDateTime,
    val expiresAt: LocalDateTime,
    val acceptedAt: LocalDateTime?
)