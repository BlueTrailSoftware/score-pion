package org.example.notifier.domain.user

import java.time.LocalDateTime
import java.util.UUID

data class User(
    val id: String = UUID.randomUUID().toString(),
    val email: String,
    val name: String,
    val googleId: String? = null,
    val auth0Id: String? = null,
    val pictureUrl: String? = null,
    val role: String = "RECRUITER",
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun isAdmin(): Boolean = role == "ADMIN"

    fun isRecruiter(): Boolean = role == "RECRUITER"
}