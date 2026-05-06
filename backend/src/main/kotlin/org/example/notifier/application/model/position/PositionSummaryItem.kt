package org.example.notifier.application.model.position

import java.time.LocalDateTime

data class PositionSummaryItem(
    val id: String,
    val title: String,
    val description: String,
    val external: Boolean,
    val assessmentsCount: Int,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val workMode: String = "Onsite",
    val location: String = ""
)
