package org.example.notifier.application.useCases.updateRecruiterActiveStatus

import java.time.LocalDateTime

data class UpdateRecruiterActiveStatusResult(
    val id: String,
    val email: String,
    val name: String,
    val role: String,
    val isActive: Boolean,
    val createdAt: LocalDateTime
)
