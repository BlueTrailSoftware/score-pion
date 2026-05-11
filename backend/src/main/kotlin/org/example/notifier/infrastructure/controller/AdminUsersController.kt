package org.example.notifier.infrastructure.controller

import org.example.notifier.application.useCases.getAllAdmins.GetAllAdminsCommand
import org.example.notifier.application.useCases.getAllAdmins.GetAllAdminsUseCase
import org.example.notifier.application.useCases.getPendingInvitations.GetPendingInvitationsUseCase
import org.example.notifier.application.useCases.inviteUser.InviteUserCommand
import org.example.notifier.application.useCases.inviteUser.InviteUserUseCase
import org.example.notifier.application.useCases.updateAdminActiveStatus.UpdateAdminActiveStatusCommand
import org.example.notifier.application.useCases.updateAdminActiveStatus.UpdateAdminActiveStatusUseCase
import org.example.notifier.domain.user.UserRole
import org.example.notifier.infrastructure.dto.mapper.*
import org.example.notifier.infrastructure.dto.request.InviteAdminRequest
import org.example.notifier.infrastructure.dto.request.UpdateActiveStatusRequest
import org.example.notifier.infrastructure.dto.response.ApiResponse
import org.example.notifier.infrastructure.dto.response.PagedData
import org.example.notifier.application.model.shared.map
import org.example.notifier.infrastructure.util.cappedPageSize
import org.example.notifier.infrastructure.dto.response.toPagedData
import org.example.notifier.infrastructure.dto.response.RecruiterInvitationResponse
import org.example.notifier.infrastructure.dto.response.RecruiterListResponse
import org.example.notifier.infrastructure.dto.response.UserResponse
import org.example.notifier.infrastructure.logging.LoggerPort
import org.example.notifier.infrastructure.security.SecurityUtils
import org.example.notifier.infrastructure.util.factory.ResponseEntityFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "Admin - Users", description = "Manage admin accounts and pending invitations (admin only)")
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
class AdminUsersController(
    private val inviteUserUseCase: InviteUserUseCase,
    private val getAllAdminsUseCase: GetAllAdminsUseCase,
    private val updateAdminActiveStatusUseCase: UpdateAdminActiveStatusUseCase,
    private val getPendingInvitationsUseCase: GetPendingInvitationsUseCase,
    private val securityUtils: SecurityUtils,
    private val responseFactory: ResponseEntityFactory,
    private val logger: LoggerPort
) {

    @Operation(summary = "Invite a new admin")
    @PostMapping("/invite")
    suspend fun inviteAdmin(
        @RequestBody request: InviteAdminRequest
    ): ResponseEntity<ApiResponse<RecruiterInvitationResponse>> {
        return try {
            val currentAdmin = securityUtils.getCurrentUser()
            val result = inviteUserUseCase.execute(
                InviteUserCommand(
                    email = request.email,
                    role = UserRole.ADMIN,
                    invitedBy = currentAdmin.id,
                    adminName = currentAdmin.name
                )
            )
            logger.info("ADMIN_INVITATION: Admin ${currentAdmin.email} invited ${request.email} as new ADMIN")
            responseFactory.success("Admin invitation sent successfully", result.toAdminInvitationResponse())
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid request parameter ${e.message}")
            responseFactory.badRequest(e.message ?: "Invalid request")
        } catch (e: Exception) {
            logger.error("Unexpected error when admin invite ${e.message}")
            responseFactory.error("Failed to create admin invitation: ${e.message}")
        }
    }

    @Operation(summary = "Get all admins")
    @GetMapping("/admins")
    suspend fun getAllAdmins(
        @RequestParam(required = false, defaultValue = "0") page: Int = 0,
        @RequestParam(required = false, defaultValue = "10") pageSize: Int = 10
    ): ResponseEntity<ApiResponse<PagedData<RecruiterListResponse>>> {
        return try {
            val effectivePageSize = pageSize.cappedPageSize()
            val result = getAllAdminsUseCase.execute(GetAllAdminsCommand(page = page, pageSize = effectivePageSize))
            responseFactory.success(
                "Admins data retrieved successfully",
                result.map { it.toResponse() }.toPagedData(page, effectivePageSize)
            )
        } catch (e: Exception) {
            logger.error("Error fetching admins data: ${e.message}", e)
            responseFactory.error("Failed to fetch admins data: ${e.message}")
        }
    }

    @Operation(summary = "Activate or deactivate an admin account")
    @PutMapping("/admins/{id}/activate")
    suspend fun updateAdminActiveStatus(
        @PathVariable id: String,
        @RequestBody request: UpdateActiveStatusRequest
    ): ResponseEntity<ApiResponse<UserResponse>> {
        return try {
            val result = updateAdminActiveStatusUseCase.execute(
                UpdateAdminActiveStatusCommand(adminId = id, isActive = request.isActive)
            )
            responseFactory.success("Admin status updated successfully", result.toResponse())
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid request parameter ${e.message}")
            responseFactory.notFound(e.message ?: "Admin not found")
        }
    }

    @Operation(summary = "Get pending user invitations")
    @GetMapping("/invitations")
    suspend fun getPendingInvitations(): ResponseEntity<ApiResponse<List<RecruiterInvitationResponse>>> {
        return try {
            val invitations = getPendingInvitationsUseCase.execute().map { it.toResponse() }
            responseFactory.success("Invitations retrieved successfully", invitations)
        } catch (e: Exception) {
            logger.error("Error getting pending invitations: ${e.message}", e)
            responseFactory.error("Failed to return pending invitations: ${e.message}")
        }
    }
}


