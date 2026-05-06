package org.example.notifier.application.useCases.updatePosition

import org.springframework.http.codec.multipart.FilePart

data class UpdatePositionCommand(
    val positionId: String,
    val title: String,
    val description: String,
    val external: Boolean,
    val assessmentIds: List<String>,
    val filePart: FilePart? = null,
    val deleteFile: Boolean = false,
    val workMode: String = "Onsite",
    val location: String = "",
    val jobType: String? = null,
    val experienceMin: Int? = null,
    val experienceMax: Int? = null,
    val skills: List<String> = emptyList()
)
