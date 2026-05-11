package org.example.notifier.application.useCases.getAllPositions

data class GetAllPositionsCommand(
    val activeOnly: Boolean,
    val page: Int = 0,
    val pageSize: Int = 10
)
