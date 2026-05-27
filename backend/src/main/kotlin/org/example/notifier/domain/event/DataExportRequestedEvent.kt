package org.example.notifier.domain.event

data class DataExportRequestedEvent(
    val email: String,
    val token: String
)
