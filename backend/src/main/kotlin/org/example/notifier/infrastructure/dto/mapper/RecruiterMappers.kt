package org.example.notifier.infrastructure.dto.mapper

import org.example.notifier.application.model.user.GetRecruiterDetailResult
import org.example.notifier.application.model.position.RecruiterPositionItem
import org.example.notifier.application.model.user.RecruiterListItem
import org.example.notifier.application.useCases.inviteUser.InviteUserResult
import org.example.notifier.application.useCases.updateRecruiterActiveStatus.UpdateRecruiterActiveStatusResult
import org.example.notifier.infrastructure.dto.response.RecruiterDetailResponse
import org.example.notifier.infrastructure.dto.response.RecruiterInvitationResponse
import org.example.notifier.infrastructure.dto.response.RecruiterListResponse
import org.example.notifier.infrastructure.dto.response.RecruiterPositionResponse
import org.example.notifier.infrastructure.dto.response.UserResponse

internal fun InviteUserResult.toResponse() =
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

internal fun RecruiterListItem.toResponse() =
    RecruiterListResponse(
        id = id,
        email = email,
        name = name,
        isActive = isActive,
        status = status,
        positionsCount = positionsCount,
        createdAt = createdAt
    )

internal fun GetRecruiterDetailResult.toResponse() =
    RecruiterDetailResponse(
        id = id,
        email = email,
        name = name,
        role = role,
        isActive = isActive,
        positions = positions.map { it.toPositionResponse() },
        positionsCount = positionsCount,
        createdAt = createdAt
    )

internal fun RecruiterPositionItem.toPositionResponse() =
    RecruiterPositionResponse(
        id = id,
        title = title,
        description = description,
        external = external,
        assessmentsCount = assessmentsCount,
        isActive = isActive,
        createdAt = createdAt,
        workMode = workMode,
        location = location
    )

internal fun UpdateRecruiterActiveStatusResult.toResponse() =
    UserResponse(
        id = id,
        email = email,
        name = name,
        role = role,
        isActive = isActive,
        createdAt = createdAt
    )

internal fun RecruiterPositionItem.toResponse() =
    RecruiterPositionResponse(
        id = id,
        title = title,
        description = description,
        external = external,
        assessmentsCount = assessmentsCount,
        isActive = isActive,
        createdAt = createdAt,
        workMode = workMode,
        location = location
    )
