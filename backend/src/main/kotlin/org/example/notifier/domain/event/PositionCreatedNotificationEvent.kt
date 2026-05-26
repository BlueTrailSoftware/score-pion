package org.example.notifier.domain.event

import org.example.notifier.domain.position.OpenPosition

data class PositionCreatedNotificationEvent(
    val createdBy: String,
    val position: OpenPosition,
    val assessmentNames: List<String>
)
