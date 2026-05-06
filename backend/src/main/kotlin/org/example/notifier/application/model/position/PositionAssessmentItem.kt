package org.example.notifier.application.model.position

import java.time.LocalDateTime

data class PositionAssessmentItem(
    val assessmentId: String,
    val assessmentName: String,
    val addedAt: LocalDateTime
)