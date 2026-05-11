package org.example.notifier.application.model.user

import java.time.LocalDateTime

data class RecruiterListItem(
    val id: String,
    val email: String,
    val name: String,
    val isActive: Boolean,
    val status: String,
    val positionsCount: Int,
    val createdAt: LocalDateTime
)