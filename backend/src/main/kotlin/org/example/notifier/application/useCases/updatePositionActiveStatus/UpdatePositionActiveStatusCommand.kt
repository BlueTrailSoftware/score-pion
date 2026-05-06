package org.example.notifier.application.useCases.updatePositionActiveStatus

data class UpdatePositionActiveStatusCommand(
    val positionId: String,
    val isActive: Boolean
)
