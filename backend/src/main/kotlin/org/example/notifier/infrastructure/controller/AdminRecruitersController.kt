package org.example.notifier.infrastructure.controller

import org.example.notifier.application.useCases.getRecruiterDetail.GetRecruiterDetailCommand
import org.example.notifier.application.useCases.getRecruiterDetail.GetRecruiterDetailUseCase
import org.example.notifier.application.useCases.getRecruiterPositions.GetRecruiterPositionsCommand
import org.example.notifier.application.useCases.getRecruiterPositions.GetRecruiterPositionsUseCase
import org.example.notifier.application.useCases.getRecruiters.GetRecruitersCommand
import org.example.notifier.application.useCases.getRecruiters.GetRecruitersUseCase
import org.example.notifier.application.useCases.inviteUser.InviteUserCommand
import org.example.notifier.application.useCases.inviteUser.InviteUserUseCase
import org.example.notifier.application.useCases.syncRecruiterPositions.SyncRecruiterPositionsCommand
import org.example.notifier.application.useCases.syncRecruiterPositions.SyncRecruiterPositionsUseCase
import org.example.notifier.application.useCases.updateRecruiterActiveStatus.UpdateRecruiterActiveStatusCommand
import org.example.notifier.application.useCases.updateRecruiterActiveStatus.UpdateRecruiterActiveStatusUseCase
import org.example.notifier.domain.user.UserRole
import org.example.notifier.infrastructure.dto.mapper.*
import org.example.notifier.infrastructure.dto.request.GrantPositionAccessRequest
import org.example.notifier.infrastructure.dto.request.InviteRecruiterRequest
import org.example.notifier.infrastructure.dto.request.UpdateActiveStatusRequest
import org.example.notifier.infrastructure.dto.response.ApiResponse
import org.example.notifier.infrastructure.dto.response.PagedData
import org.example.notifier.infrastructure.dto.response.RecruiterDetailResponse
import org.example.notifier.infrastructure.dto.response.RecruiterInvitationResponse
import org.example.notifier.infrastructure.dto.response.RecruiterListResponse
import org.example.notifier.infrastructure.dto.response.RecruiterPositionResponse
import org.example.notifier.infrastructure.dto.response.UserResponse
import org.example.notifier.infrastructure.logging.LoggerPort
import org.example.notifier.infrastructure.security.SecurityUtils
import org.example.notifier.application.model.shared.map
import org.example.notifier.infrastructure.util.cappedPageSize
import org.example.notifier.infrastructure.dto.response.toPagedData
import org.example.notifier.infrastructure.util.factory.ResponseEntityFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "Admin - Recruiters", description = "Manage recruiter accounts and position assignments (admin only)")
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
class AdminRecruitersController(
    private val inviteUserUseCase: InviteUserUseCase,
    private val getRecruitersUseCase: GetRecruitersUseCase,
    private val getRecruiterDetailUseCase: GetRecruiterDetailUseCase,
    private val getRecruiterPositionsUseCase: GetRecruiterPositionsUseCase,
    private val updateRecruiterActiveStatusUseCase: UpdateRecruiterActiveStatusUseCase,
    private val syncRecruiterPositionsUseCase: SyncRecruiterPositionsUseCase,
    private val securityUtils: SecurityUtils,
    private val responseFactory: ResponseEntityFactory,
    private val logger: LoggerPort
) {

    @Operation(summary = "Invite a new recruiter")
    @PostMapping("/recruiters/invite")
    suspend fun inviteRecruiter(
        @RequestBody request: InviteRecruiterRequest
    ): ResponseEntity<ApiResponse<RecruiterInvitationResponse>> {
        return try {
            val currentAdmin = securityUtils.getCurrentUser()
            val result = inviteUserUseCase.execute(
                InviteUserCommand(
                    email = request.email,
                    role = UserRole.RECRUITER,
                    positionIds = request.positionIds,
                    invitedBy = currentAdmin.id,
                    adminName = currentAdmin.name
                )
            )
            responseFactory.success("Invitation sent successfully", result.toResponse())
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid request parameter ${e.message}")
            responseFactory.badRequest(e.message ?: "Invalid request")
        } catch (e: Exception) {
            logger.error("Unexpected error when recruiter invite ${e.message}")
            responseFactory.error("Failed to create invitation: ${e.message}")
        }
    }

    @Operation(summary = "Get all recruiters")
    @GetMapping("/recruiters")
    suspend fun getAllRecruiters(
        @RequestParam(required = false, defaultValue = "0") page: Int = 0,
        @RequestParam(required = false, defaultValue = "10") pageSize: Int = 10
    ): ResponseEntity<ApiResponse<PagedData<RecruiterListResponse>>> {
        return try {
            val effectivePageSize = pageSize.cappedPageSize()
            val result = getRecruitersUseCase.execute(GetRecruitersCommand(page = page, pageSize = effectivePageSize))
            responseFactory.success(
                "Recruiters data retrieved successfully",
                result.map { it.toResponse() }.toPagedData(page, effectivePageSize)
            )
        } catch (e: Exception) {
            logger.error("Error fetching recruiters data: ${e.message}", e)
            responseFactory.error("Failed to fetch recruiters data: ${e.message}")
        }
    }

    @Operation(summary = "Get recruiter by ID")
    @GetMapping("/recruiters/{id}")
    suspend fun getRecruiterById(
        @PathVariable id: String
    ): ResponseEntity<ApiResponse<RecruiterDetailResponse>> {
        return try {
            val result = getRecruiterDetailUseCase.execute(GetRecruiterDetailCommand(recruiterId = id))
                ?: return responseFactory.notFound("Recruiter not found")
            responseFactory.success("Recruiter retrieved successfully", result.toResponse())
        } catch (e: Exception) {
            logger.error("Error fetching recruiter: ${e.message}", e)
            responseFactory.error("Failed to fetch recruiter: ${e.message}")
        }
    }

    @Operation(summary = "Activate or deactivate a recruiter account")
    @PutMapping("/recruiters/{id}/activate")
    suspend fun updateRecruiterActiveStatus(
        @PathVariable id: String,
        @RequestBody request: UpdateActiveStatusRequest
    ): ResponseEntity<ApiResponse<UserResponse>> {
        return try {
            val result = updateRecruiterActiveStatusUseCase.execute(
                UpdateRecruiterActiveStatusCommand(recruiterId = id, isActive = request.isActive)
            )
            responseFactory.success("User status updated successfully", result.toResponse())
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid request parameter ${e.message}")
            responseFactory.notFound(e.message ?: "User not found")
        }
    }

    @Operation(summary = "Get positions assigned to a recruiter")
    @GetMapping("/recruiters/{id}/positions")
    suspend fun getRecruiterPositions(
        @PathVariable id: String,
        @RequestParam(required = false, defaultValue = "0") page: Int = 0,
        @RequestParam(required = false, defaultValue = "10") pageSize: Int = 10
    ): ResponseEntity<ApiResponse<PagedData<RecruiterPositionResponse>>> {
        return try {
            val effectivePageSize = pageSize.cappedPageSize()
            val result = getRecruiterPositionsUseCase.execute(
                GetRecruiterPositionsCommand(recruiterId = id, page = page, pageSize = effectivePageSize)
            )
            responseFactory.success(
                "Positions retrieved successfully",
                result.map { it.toResponse() }.toPagedData(page, effectivePageSize)
            )
        } catch (e: Exception) {
            logger.error("Error fetching recruiter positions: ${e.message}", e)
            responseFactory.error("Failed to fetch recruiter positions: ${e.message}")
        }
    }

    @Operation(summary = "Sync (replace) position assignments for a recruiter")
    @PutMapping("/recruiters/{id}/positions")
    suspend fun syncRecruiterPositions(
        @PathVariable id: String,
        @RequestBody request: GrantPositionAccessRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        return try {
            val currentAdmin = securityUtils.getCurrentUser()
            syncRecruiterPositionsUseCase.execute(
                SyncRecruiterPositionsCommand(
                    recruiterId = id,
                    positionIds = request.positionIds,
                    grantedBy = currentAdmin.id
                )
            )
            responseFactory.success("Recruiter positions synchronized successfully")
        } catch (e: Exception) {
            logger.error("Error syncing recruiter positions: ${e.message}", e)
            responseFactory.error("Failed to sync recruiter positions: ${e.message}")
        }
    }
}


