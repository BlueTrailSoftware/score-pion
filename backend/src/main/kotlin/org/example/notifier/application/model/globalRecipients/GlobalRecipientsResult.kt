package org.example.notifier.application.model.globalRecipients

data class GlobalRecipientsResult(
    val emails: List<String>,
    val description: String,
    val updatedAt: String,
    val updatedBy: String?
)