package org.example.notifier.application.useCases.checkRecruiterInvitation

data class CheckRecruiterInvitationResult(
    val allowed: Boolean,
    val reason: String? = null
)
