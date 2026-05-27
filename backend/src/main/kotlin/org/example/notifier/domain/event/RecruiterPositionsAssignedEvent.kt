package org.example.notifier.domain.event

import org.example.notifier.domain.position.OpenPosition

data class RecruiterPositionsAssignedEvent(
    val recruiterEmail: String,
    val recruiterName: String,
    val positions: List<OpenPosition>
)
