package org.example.notifier.domain.globalRecipients

import java.time.Instant

data class GlobalRecipients(
    val id: String = "GLOBAL_RECIPIENTS",
    val version: String = "METADATA",
    val emails: MutableList<String> = mutableListOf(),
    val description: String = "Global email recipients list",
    val updatedAt: String = Instant.now().toString(),
    val updatedBy: String? = null
)