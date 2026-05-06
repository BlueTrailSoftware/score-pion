package org.example.notifier.infrastructure.external

/**
 * User info extracted from Auth0 ID token.
 */
data class Auth0UserInfo(
    val auth0Id: String,
    val email: String,
    val name: String,
    val pictureUrl: String?,
    val emailVerified: Boolean
)
