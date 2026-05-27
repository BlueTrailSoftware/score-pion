package org.example.notifier.domain.event

data class UserInvitedEvent(
    val recipientEmail: String,
    val role: String,
    val adminName: String? = null
)
