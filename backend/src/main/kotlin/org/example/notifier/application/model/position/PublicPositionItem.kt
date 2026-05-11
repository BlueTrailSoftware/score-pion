package org.example.notifier.application.model.position

import java.time.LocalDateTime

data class PublicPositionItem(
    val id: String,
    val title: String,
    val description: String,
    val fileUrl: String?,
    val createdAt: LocalDateTime,
    val workMode: String = "Onsite",
    val location: String = "",
    val jobType: String? = null,
    val experienceMin: Int? = null,
    val experienceMax: Int? = null,
    val skills: List<String> = emptyList()
)