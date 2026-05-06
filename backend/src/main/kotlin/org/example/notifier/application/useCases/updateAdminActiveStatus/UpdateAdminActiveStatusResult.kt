package org.example.notifier.application.useCases.updateAdminActiveStatus

import java.time.LocalDateTime

data class UpdateAdminActiveStatusResult(
    val id: String,
    val email: String,
    val name: String,
    val role: String,
    val isActive: Boolean,
    val createdAt: LocalDateTime
)
