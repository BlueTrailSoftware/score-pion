package org.example.notifier.application.useCases.updateAdminActiveStatus

data class UpdateAdminActiveStatusCommand(
    val adminId: String,
    val isActive: Boolean
)
