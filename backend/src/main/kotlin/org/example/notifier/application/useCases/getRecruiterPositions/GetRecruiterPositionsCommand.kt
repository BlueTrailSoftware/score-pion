package org.example.notifier.application.useCases.getRecruiterPositions

data class GetRecruiterPositionsCommand(
    val recruiterId: String,
    val page: Int = 0,
    val pageSize: Int = 10
)
