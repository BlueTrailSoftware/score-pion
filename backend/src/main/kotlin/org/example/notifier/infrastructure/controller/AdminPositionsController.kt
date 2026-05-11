package org.example.notifier.infrastructure.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.example.notifier.application.useCases.createPosition.CreatePositionCommand
import org.example.notifier.application.useCases.createPosition.CreatePositionUseCase
import org.example.notifier.application.useCases.getAllPositions.GetAllPositionsCommand
import org.example.notifier.application.useCases.getAllPositions.GetAllPositionsUseCase
import org.example.notifier.application.useCases.getAvailableAssessments.GetAvailableAssessmentsUseCase
import org.example.notifier.application.useCases.getPositionById.GetPositionByIdCommand
import org.example.notifier.application.useCases.getPositionById.GetPositionByIdUseCase
import org.example.notifier.application.useCases.updatePosition.UpdatePositionCommand
import org.example.notifier.application.useCases.updatePosition.UpdatePositionUseCase
import org.example.notifier.application.useCases.updatePositionActiveStatus.UpdatePositionActiveStatusCommand
import org.example.notifier.application.useCases.updatePositionActiveStatus.UpdatePositionActiveStatusUseCase
import org.example.notifier.infrastructure.dto.mapper.*
import org.example.notifier.application.model.shared.map
import org.example.notifier.infrastructure.util.cappedPageSize
import org.example.notifier.infrastructure.dto.response.toPagedData
import org.example.notifier.infrastructure.dto.request.CreatePositionRequest
import org.example.notifier.infrastructure.dto.request.UpdateActiveStatusRequest
import org.example.notifier.infrastructure.dto.request.UpdatePositionRequest
import org.example.notifier.infrastructure.dto.response.ApiResponse
import org.example.notifier.infrastructure.dto.response.AssessmentsResponse.ExamData
import org.example.notifier.infrastructure.dto.response.PagedData
import org.example.notifier.infrastructure.dto.response.PositionResponse
import org.example.notifier.infrastructure.dto.response.PositionSummaryResponse
import org.example.notifier.infrastructure.logging.LoggerPort
import org.example.notifier.infrastructure.security.SecurityUtils
import org.example.notifier.infrastructure.util.FileValidator
import org.example.notifier.infrastructure.util.factory.ResponseEntityFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "Admin - Positions", description = "Manage job positions (admin only)")
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
class AdminPositionsController(
    private val createPositionUseCase: CreatePositionUseCase,
    private val getAllPositionsUseCase: GetAllPositionsUseCase,
    private val getPositionByIdUseCase: GetPositionByIdUseCase,
    private val updatePositionUseCase: UpdatePositionUseCase,
    private val updatePositionActiveStatusUseCase: UpdatePositionActiveStatusUseCase,
    private val getAvailableAssessmentsUseCase: GetAvailableAssessmentsUseCase,
    private val securityUtils: SecurityUtils,
    private val responseFactory: ResponseEntityFactory,
    private val logger: LoggerPort,
    private val objectMapper: ObjectMapper
) {

    @Operation(summary = "Get available Coderbyte assessments")
    @GetMapping("/assessments")
    suspend fun getAssessments(): ResponseEntity<ApiResponse<List<ExamData>>> {
        return try {
            val assessments = getAvailableAssessmentsUseCase.execute().map { it.toResponse() }
            responseFactory.success("Assessments retrieved successfully", assessments)
        } catch (e: IllegalStateException) {
            logger.error("Invalid response from Coderbyte API: ${e.message}")
            responseFactory.error("Failed to fetch assessments: Invalid response from assessment service")
        } catch (e: Exception) {
            logger.error("Unexpected error when getAssessments: ${e.message}", e)
            responseFactory.error("Failed to get assessment list: ${e.message}")
        }
    }

    @Operation(summary = "Create a new position")
    @PostMapping("/positions", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    suspend fun createPosition(
        @RequestPart("data") requestJson: String,
        @RequestPart("file", required = false) filePart: FilePart?
    ): ResponseEntity<ApiResponse<PositionResponse>> {
        return try {
            logger.info("Received createPosition request")
            val request = objectMapper.readValue(requestJson, CreatePositionRequest::class.java)
            FileValidator.validateFile(filePart)
            val currentAdmin = securityUtils.getCurrentUser()
            val result = createPositionUseCase.execute(
                CreatePositionCommand(
                    title = request.title,
                    description = request.description,
                    external = request.external,
                    assessmentIds = request.assessmentIds,
                    createdByEmail = currentAdmin.email,
                    filePart = filePart,
                    workMode = request.workMode,
                    location = request.location,
                    jobType = request.jobType,
                    experienceMin = request.experienceMin,
                    experienceMax = request.experienceMax,
                    skills = request.skills ?: emptyList()
                )
            )
            responseFactory.success("Position created successfully", result.toResponse())
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid request parameter: ${e.message}")
            responseFactory.badRequest(e.message ?: "Invalid request")
        } catch (e: Exception) {
            logger.error("Error creating position: ${e.message}", e)
            responseFactory.error("Failed to create position: ${e.message}")
        }
    }

    @Operation(summary = "Get all positions")
    @GetMapping("/positions")
    suspend fun getAllPositions(
        @RequestParam(required = false, defaultValue = "false") activeOnly: Boolean,
        @RequestParam(required = false, defaultValue = "0") page: Int = 0,
        @RequestParam(required = false, defaultValue = "10") pageSize: Int = 10
    ): ResponseEntity<ApiResponse<PagedData<PositionSummaryResponse>>> {
        return try {
            val effectivePageSize = pageSize.cappedPageSize()
            val result = getAllPositionsUseCase.execute(
                GetAllPositionsCommand(activeOnly = activeOnly, page = page, pageSize = effectivePageSize)
            )
            responseFactory.success(
                "Positions retrieved successfully",
                result.map { it.toResponse() }.toPagedData(page, effectivePageSize)
            )
        } catch (e: Exception) {
            logger.error("Error fetching positions: ${e.message}", e)
            responseFactory.error("Failed to fetch positions: ${e.message}")
        }
    }

    @Operation(summary = "Get position by ID")
    @GetMapping("/positions/{id}")
    suspend fun getPositionById(
        @PathVariable id: String
    ): ResponseEntity<ApiResponse<PositionResponse>> {
        return try {
            val result = getPositionByIdUseCase.execute(GetPositionByIdCommand(positionId = id))
                ?: return responseFactory.notFound("Position not found")
            responseFactory.success("Position retrieved successfully", result.toResponse())
        } catch (e: Exception) {
            logger.error("Error fetching position: ${e.message}", e)
            responseFactory.error("Failed to fetch position: ${e.message}")
        }
    }

    @Operation(summary = "Update an existing position")
    @PutMapping("/positions/{id}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    suspend fun updatePosition(
        @PathVariable id: String,
        @RequestPart("data") requestJson: String,
        @RequestPart("file", required = false) filePart: FilePart?
    ): ResponseEntity<ApiResponse<PositionResponse>> {
        return try {
            logger.info("Updating position: $id")
            val request = objectMapper.readValue(requestJson, UpdatePositionRequest::class.java)
            FileValidator.validateFile(filePart)
            val result = updatePositionUseCase.execute(
                UpdatePositionCommand(
                    positionId = id,
                    title = request.title,
                    description = request.description,
                    external = request.external,
                    assessmentIds = request.assessmentIds,
                    filePart = filePart,
                    deleteFile = request.deleteFile ?: false,
                    workMode = request.workMode,
                    location = request.location,
                    jobType = request.jobType,
                    experienceMin = request.experienceMin,
                    experienceMax = request.experienceMax,
                    skills = request.skills ?: emptyList()
                )
            )
            responseFactory.success("Position updated successfully", result.toResponse())
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid request parameter: ${e.message}")
            responseFactory.notFound(e.message ?: "Position not found")
        } catch (e: Exception) {
            logger.error("Error updating position: ${e.message}", e)
            responseFactory.error("Failed to update position: ${e.message}")
        }
    }

    @Operation(summary = "Activate or deactivate a position")
    @PatchMapping("/positions/{id}/activate")
    suspend fun updatePositionActiveStatus(
        @PathVariable id: String,
        @RequestBody request: UpdateActiveStatusRequest
    ): ResponseEntity<ApiResponse<PositionResponse>> {
        return try {
            val result = updatePositionActiveStatusUseCase.execute(
                UpdatePositionActiveStatusCommand(positionId = id, isActive = request.isActive)
            ) ?: return responseFactory.notFound("Position not found or already in requested state")

            val message = if (request.isActive) "Position activated successfully" else "Position deactivated successfully"
            responseFactory.success(message, result.toResponse())
        } catch (e: Exception) {
            logger.error("Error updating position status: ${e.message}", e)
            responseFactory.error("Failed to update position status: ${e.message}")
        }
    }
}


