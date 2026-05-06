package org.example.notifier.application.model.user

import java.time.LocalDateTime

data class AdminListItem(
    val id: String,
    val email: String,
    val name: String,
    val isActive: Boolean,
    val status: String?,
    val createdAt: LocalDateTime
)