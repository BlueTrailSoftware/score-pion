package org.example.notifier.domain.position

import java.time.LocalDateTime
import java.util.UUID

data class OpenPosition(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val createdBy: String,
    val isActive: Boolean = true,
    val external: Boolean = false,
    val fileUrl: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    val isFileDeleted: Boolean = false,
    val workMode: String = "Onsite",
    val location: String = "",
    val jobType: String? = null,
    val experienceMin: Int? = null,
    val experienceMax: Int? = null,
    val skills: List<String> = emptyList()
)
