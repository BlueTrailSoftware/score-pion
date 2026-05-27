package org.example.notifier.domain.event

data class DataErasureRequestedEvent(
    val email: String,
    val token: String
)
