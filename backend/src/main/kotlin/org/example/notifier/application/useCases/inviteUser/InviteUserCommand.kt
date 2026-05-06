package org.example.notifier.application.useCases.inviteUser

data class InviteUserCommand(
    val email: String,
    val role: String,
    val positionIds: List<String>? = null,
    val invitedBy: String,
    val adminName: String
)
