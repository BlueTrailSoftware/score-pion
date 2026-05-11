package org.example.notifier.application.useCases.googleSSOLogin

sealed class GoogleSSOLoginResult {
    data class Success(val userId: String, val authToken: String, val role: String) : GoogleSSOLoginResult()
    object InvalidCredential : GoogleSSOLoginResult()
    object EmailNotVerified : GoogleSSOLoginResult()
    object AccessDenied : GoogleSSOLoginResult()
    object AccountDeactivated : GoogleSSOLoginResult()
}
