package org.example.notifier.application.model.globalRecipients

import org.example.notifier.domain.globalRecipients.GlobalRecipients

fun GlobalRecipients.toResult() = GlobalRecipientsResult(
    emails = emails.toList(),
    description = description,
    updatedAt = updatedAt,
    updatedBy = updatedBy
)