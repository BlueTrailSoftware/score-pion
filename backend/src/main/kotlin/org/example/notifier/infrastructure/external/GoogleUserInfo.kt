package org.example.notifier.infrastructure.external

/**
 * User info response from Google OAuth API.
 */
data class GoogleUserInfo(
    val googleId: String,
    val email: String,
    val name: String,
    val pictureUrl: String?,
    val emailVerified: Boolean
)
