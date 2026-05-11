package org.example.notifier.application.useCases.requestDataErasure

data class RequestDataErasureCommand(
    val email: String,
    val captchaToken: String
)
