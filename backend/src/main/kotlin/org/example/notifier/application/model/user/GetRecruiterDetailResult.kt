package org.example.notifier.application.model.user

import java.time.LocalDateTime
import org.example.notifier.application.model.position.RecruiterPositionItem

data class GetRecruiterDetailResult(
    val id: String,
    val email: String,
    val name: String,
    val role: String,
    val isActive: Boolean,
    val positions: List<RecruiterPositionItem>,
    val positionsCount: Int,
    val createdAt: LocalDateTime
)