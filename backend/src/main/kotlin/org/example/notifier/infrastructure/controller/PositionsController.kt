package org.example.notifier.infrastructure.controller

import org.example.notifier.application.model.position.PositionAssessmentItem
import org.example.notifier.application.model.position.PositionResult
import org.example.notifier.application.model.position.PositionSummaryItem
import org.example.notifier.application.useCases.getAllPositions.GetAllPositionsCommand
import org.example.notifier.application.useCases.getAllPositions.GetAllPositionsUseCase
import org.example.notifier.application.useCases.getPositionById.GetPositionByIdCommand
import org.example.notifier.application.useCases.getPositionById.GetPositionByIdUseCase
import org.example.notifier.application.useCases.getRecruiterPositions.GetRecruiterPositionsCommand
import org.example.notifier.application.useCases.getRecruiterPositions.GetRecruiterPositionsUseCase
import org.example.notifier.application.model.position.RecruiterPositionItem
import org.example.notifier.infrastructure.dto.response.ApiResponse
import org.example.notifier.infrastructure.dto.response.PagedData
import org.example.notifier.infrastructure.dto.response.PositionAssessmentResponse
import org.example.notifier.infrastructure.dto.response.PositionResponse
import org.example.notifier.infrastructure.dto.response.PositionSummaryResponse
import org.example.notifier.infrastructure.dto.response.RecruiterPositionResponse
import org.example.notifier.infrastructure.logging.LoggerPort
import org.example.notifier.infrastructure.security.SecurityUtils
import org.example.notifier.application.model.shared.map
import org.example.notifier.infrastructure.util.cappedPageSize
import org.example.notifier.infrastructure.dto.response.toPagedData
import org.example.notifier.infrastructure.util.factory.ResponseEntityFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag

/**
 * Unified controller for position operations.
 * Handles both ADMIN and RECRUITER roles automatically based on authentication.
 */
@Tag(name = "Positions", description = "View positions — ADMIN sees all, RECRUITER sees only assigned ones")
@RestController
@RequestMapping("/positions")
class PositionsController(
    private val getAllPositionsUseCase: GetAllPositionsUseCase,
    private val getRecruiterPositionsUseCase: GetRecruiterPositionsUseCase,
    private val getPositionByIdUseCase: GetPositionByIdUseCase,
    private val securityUtils: SecurityUtils,
    private val responseFactory: ResponseEntityFactory,
    private val logger: LoggerPort
) {

    /**
     * Gets positions based on user role:
     * - ADMIN: Returns all positions (with optional activeOnly filter)
     * - RECRUITER: Returns only positions assigned to the recruiter
     */
    @Operation(summary = "Get positions (ADMIN: all positions, RECRUITER: assigned only)")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUITER')")
    suspend fun getPositions(
        @RequestParam(required = false, defaultValue = "true") activeOnly: Boolean,
        @RequestParam(required = false, defaultValue = "0") page: Int = 0,
        @RequestParam(required = false, defaultValue = "10") pageSize: Int = 10
    ): ResponseEntity<ApiResponse<PagedData<*>>> {
        val currentUser = securityUtils.getCurrentUser()

        logger.info("User ${currentUser.id} (${currentUser.role}) requesting positions")

        return try {
            val effectivePageSize = pageSize.cappedPageSize()
            val result: PagedData<*> = if (currentUser.isAdmin()) {
                getAllPositionsUseCase.execute(GetAllPositionsCommand(activeOnly = activeOnly, page = page, pageSize = effectivePageSize))
                    .map { it.toResponse() }.toPagedData(page, effectivePageSize)
            } else {
                getRecruiterPositionsUseCase.execute(GetRecruiterPositionsCommand(recruiterId = currentUser.id, page = page, pageSize = effectivePageSize))
                    .map { it.toResponse() }.toPagedData(page, effectivePageSize)
            }

            logger.info("Found ${result.total} positions for user ${currentUser.id}")
            responseFactory.success("Positions retrieved successfully", result)

        } catch (e: Exception) {
            logger.error("Error fetching positions for user ${currentUser.id}: ${e.message}", e)
            responseFactory.error("Failed to fetch positions: ${e.message}")
        }
    }

    /**
     * Gets position details by ID:
     * - ADMIN: Can access any position
     * - RECRUITER: Can only access positions assigned to them
     */
    @Operation(summary = "Get position details by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUITER')")
    suspend fun getPositionById(@PathVariable id: String): ResponseEntity<ApiResponse<PositionResponse>> {
        val currentUser = securityUtils.getCurrentUser()

        logger.info("User ${currentUser.id} (${currentUser.role}) requesting position $id")

        return try {
            if (currentUser.isRecruiter()) {
                val recruiterPositions = getRecruiterPositionsUseCase.execute(
                    GetRecruiterPositionsCommand(recruiterId = currentUser.id, pageSize = Int.MAX_VALUE)
                )
                if (recruiterPositions.items.none { it.id == id }) {
                    logger.warn("Recruiter ${currentUser.id} attempted to access position $id without permission")
                    return responseFactory.forbidden("You don't have access to this position")
                }
            }

            val result = getPositionByIdUseCase.execute(GetPositionByIdCommand(positionId = id))
                ?: return responseFactory.notFound("Position not found")

            logger.info("User ${currentUser.id} successfully retrieved position $id")
            responseFactory.success("Position retrieved successfully", result.toResponse())

        } catch (e: Exception) {
            logger.error("Error fetching position for user ${currentUser.id}: ${e.message}", e)
            responseFactory.error("Failed to fetch position: ${e.message}")
        }
    }
}

// ── Mappers ──────────────────────────────────────────────────────────────────

private fun PositionSummaryItem.toResponse() =
    PositionSummaryResponse(
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

private fun RecruiterPositionItem.toResponse() =
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

private fun PositionAssessmentItem.toResponse() =
    PositionAssessmentResponse(
        assessmentId = assessmentId,
        assessmentName = assessmentName,
        addedAt = addedAt
    )

private fun PositionResult.toResponse() =
    PositionResponse(
        id = id,
        title = title,
        description = description,
        external = external,
        assessments = assessments.map { it.toResponse() },
        fileUrl = fileUrl,
        createdBy = createdBy,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isFileDeleted = isFileDeleted,
        workMode = workMode,
        location = location,
        jobType = jobType,
        experienceMin = experienceMin,
        experienceMax = experienceMax,
        skills = skills
    )
