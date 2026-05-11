package org.example.notifier.infrastructure.dto.request

data class AddEmailRequest(val email: String)
data class UpdateEmailRequest(val oldEmail: String, val newEmail: String)