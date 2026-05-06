package org.example.notifier.application.model.position

import java.time.LocalDateTime

data class PositionResult(
    val id: String,
    val title: String,
    val description: String,
    val external: Boolean,
    val assessments: List<PositionAssessmentItem>,
    val fileUrl: String?,
    val createdBy: String,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val isFileDeleted: Boolean = false,
    val workMode: String,
    val location: String,
    val jobType: String? = null,
    val experienceMin: Int? = null,
    val experienceMax: Int? = null,
    val skills: List<String> = emptyList()
)
