package org.example.notifier.infrastructure.dto.mapper

import org.example.notifier.application.model.user.AdminListItem
import org.example.notifier.application.model.invitation.InvitationItem
import org.example.notifier.application.model.user.UserProfileResult
import org.example.notifier.application.useCases.inviteUser.InviteUserResult
import org.example.notifier.application.useCases.updateAdminActiveStatus.UpdateAdminActiveStatusResult
import org.example.notifier.infrastructure.dto.response.RecruiterInvitationResponse
import org.example.notifier.infrastructure.dto.response.RecruiterListResponse
import org.example.notifier.infrastructure.dto.response.UserResponse

internal fun InviteUserResult.toAdminInvitationResponse() =
    RecruiterInvitationResponse(
        id = id,
        email = email,
        invitedBy = invitedBy,
        assignedPositions = assignedPositions,
        status = status,
        createdAt = createdAt,
        expiresAt = expiresAt,
        acceptedAt = acceptedAt
    )

internal fun AdminListItem.toResponse() =
    RecruiterListResponse(
        id = id,
        email = email,
        name = name,
        isActive = isActive,
        status = status,
        positionsCount = 0,
        createdAt = createdAt
    )

internal fun UpdateAdminActiveStatusResult.toResponse() =
    UserResponse(
        id = id,
        email = email,
        name = name,
        role = role,
        isActive = isActive,
        createdAt = createdAt
    )

internal fun UserProfileResult.toResponse() =
    UserResponse(
        id = id,
        email = email,
        name = name,
        role = role,
        isActive = isActive,
        createdAt = createdAt,
        pictureUrl = pictureUrl
    )

internal fun InvitationItem.toResponse() =
    RecruiterInvitationResponse(
        id = id,
        email = email,
        invitedBy = invitedBy,
        assignedPositions = assignedPositions,
        status = status,
        createdAt = createdAt,
        expiresAt = expiresAt,
        acceptedAt = acceptedAt
    )
