package org.example.notifier.domain.position

import java.time.LocalDateTime
import java.util.UUID

data class OpenPositionRecruiterAccess(
    val id: String = UUID.randomUUID().toString(),
    val openPositionId: String,
    val recruiterId: String,
    val grantedBy: String,
    val grantedAt: LocalDateTime = LocalDateTime.now(),
    var isActive: Boolean = true
)