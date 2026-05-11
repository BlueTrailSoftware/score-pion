package org.example.notifier.application.useCases.requestDataErasure

sealed class RequestDataErasureResult {
    object NoApplicantFound : RequestDataErasureResult()
    object VerificationEmailSent : RequestDataErasureResult()
}
