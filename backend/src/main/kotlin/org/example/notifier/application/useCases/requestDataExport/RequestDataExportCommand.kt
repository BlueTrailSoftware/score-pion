package org.example.notifier.application.useCases.requestDataExport

data class RequestDataExportCommand(
    val email: String,
    val captchaToken: String
)
