package org.example.notifier.application.useCases.createPosition

import org.springframework.http.codec.multipart.FilePart

data class CreatePositionCommand(
    val title: String,
    val description: String,
    val external: Boolean,
    val assessmentIds: List<String>,
    val createdByEmail: String,
    val filePart: FilePart? = null,
    val workMode: String = "Onsite",
    val location: String = "",
    val jobType: String? = null,
    val experienceMin: Int? = null,
    val experienceMax: Int? = null,
    val skills: List<String> = emptyList()
)
