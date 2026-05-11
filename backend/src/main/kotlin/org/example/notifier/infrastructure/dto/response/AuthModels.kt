package org.example.notifier.infrastructure.dto.response

data class UserProfileResponse(
    val status: String,
    val data: UserProfileData
)

data class UserProfileData(
    val id: String,
    val email: String,
    val name: String,
    val pictureUrl: String?,
    val role: String,
    val createdAt: String
)

data class EmptyApiResponse(
    val status: String,
    val message: String? = null
)
