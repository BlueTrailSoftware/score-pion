package org.example.notifier.application.useCases.auth0Callback

sealed class Auth0CallbackResult {
    data class Success(val userId: String, val authToken: String, val role: String) : Auth0CallbackResult()
    object InvalidCode : Auth0CallbackResult()
    object EmailNotVerified : Auth0CallbackResult()
    object AccessDenied : Auth0CallbackResult()
    object AccountDeactivated : Auth0CallbackResult()
}
