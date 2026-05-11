package org.example.notifier.application.useCases.confirmDataErasure

sealed class ConfirmDataErasureResult {
    data class Anonymized(val count: Int) : ConfirmDataErasureResult()
    object InvalidToken : ConfirmDataErasureResult()
    object NotFound : ConfirmDataErasureResult()
    object AlreadyAnonymized : ConfirmDataErasureResult()
}
