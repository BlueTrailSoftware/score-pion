package org.example.notifier.application.model.user

import java.time.LocalDateTime

data class UserProfileResult(
    val id: String,
    val email: String,
    val name: String,
    val pictureUrl: String?,
    val role: String,
    val isActive: Boolean,
    val createdAt: LocalDateTime
)