package org.example.notifier.domain.position

import java.time.LocalDateTime
import java.util.UUID

data class OpenPositionAssessment(
    val id: String = UUID.randomUUID().toString(),
    val openPositionId: String,
    val assessmentId: String,
    val assessmentName: String,
    val addedAt: LocalDateTime = LocalDateTime.now()
)