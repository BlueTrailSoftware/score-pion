package org.example.notifier.application.useCases.syncRecruiterPositions

data class SyncRecruiterPositionsCommand(
    val recruiterId: String,
    val positionIds: List<String>,
    val grantedBy: String
)
