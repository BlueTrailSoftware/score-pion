package org.example.notifier.infrastructure.dto.request

import com.fasterxml.jackson.annotation.JsonAlias

data class InviteRecruiterRequest(
    val email: String,
    val positionIds: List<String>? = null
)

data class InviteAdminRequest(
    val email: String
)

data class UpdateActiveStatusRequest(
    val isActive: Boolean
)

data class CreatePositionRequest(
    val title: String,
    val description: String,
    val external: Boolean = false,
    @JsonAlias("assessments")
    val assessmentIds: List<String> = emptyList(),
    val workMode: String,
    val location: String,
    val jobType: String? = null,
    val experienceMin: Int? = null,
    val experienceMax: Int? = null,
    val skills: List<String>? = null
)

data class UpdatePositionRequest(
    val title: String,
    val description: String,
    val external: Boolean,
    val assessmentIds: List<String>,
    val deleteFile: Boolean = false,
    val workMode: String,
    val location: String,
    val jobType: String? = null,
    val experienceMin: Int? = null,
    val experienceMax: Int? = null,
    val skills: List<String>? = null
)

data class GrantPositionAccessRequest(
    val positionIds: List<String>
)
