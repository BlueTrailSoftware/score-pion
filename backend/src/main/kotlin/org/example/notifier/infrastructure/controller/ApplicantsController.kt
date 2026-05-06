package org.example.notifier.infrastructure.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.example.notifier.application.useCases.getApplicantById.GetApplicantByIdCommand
import org.example.notifier.application.useCases.getApplicantById.GetApplicantByIdUseCase
import org.example.notifier.application.model.shared.PageQuery
import org.example.notifier.application.model.shared.SortDirection
import org.example.notifier.application.model.shared.map
import org.example.notifier.application.useCases.getApplicants.GetApplicantsCommand
import org.example.notifier.application.useCases.getApplicants.GetApplicantsUseCase
import org.example.notifier.application.useCases.getPositionById.GetPositionByIdCommand
import org.example.notifier.application.useCases.getPositionById.GetPositionByIdUseCase
import org.example.notifier.application.useCases.getPublicPositions.GetPublicPositionsUseCase
import org.example.notifier.application.useCases.submitApplication.SubmitApplicationCommand
import org.example.notifier.application.useCases.submitApplication.SubmitApplicationUseCase
import org.example.notifier.application.useCases.updateApplicant.UpdateApplicantCommand
import org.example.notifier.application.useCases.updateApplicant.UpdateApplicantUseCase
import org.example.notifier.application.useCases.updateApplicantStatus.UpdateApplicantStatusCommand
import org.example.notifier.application.useCases.updateApplicantStatus.UpdateApplicantStatusUseCase
import org.example.notifier.infrastructure.dto.mapper.toResponse
import org.example.notifier.infrastructure.dto.request.ApplyToPositionRequest
import org.example.notifier.infrastructure.dto.response.toPagedData
import org.example.notifier.infrastructure.dto.request.UpdateApplicantStatusRequest
import org.example.notifier.infrastructure.dto.request.UpdateApplicantRequest
import org.example.notifier.infrastructure.dto.response.ApiResponse
import org.example.notifier.infrastructure.dto.response.ApplicantResponse
import org.example.notifier.infrastructure.dto.response.PagedData
import org.example.notifier.infrastructure.dto.response.PublicPositionResponse
import org.example.notifier.infrastructure.logging.LoggerPort
import org.example.notifier.infrastructure.security.SecurityUtils
import org.example.notifier.infrastructure.util.FileValidator
import org.example.notifier.infrastructure.util.factory.ResponseEntityFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "Applicants", description = "Public job application endpoints and admin applicant management")
@RestController
@RequestMapping("/applicants")
class ApplicantsController(
    private val submitApplicationUseCase: SubmitApplicationUseCase,
    private val getPublicPositionsUseCase: GetPublicPositionsUseCase,
    private val getPositionByIdUseCase: GetPositionByIdUseCase,
    private val getApplicantsUseCase: GetApplicantsUseCase,
    private val getApplicantByIdUseCase: GetApplicantByIdUseCase,
    private val updateApplicantStatusUseCase: UpdateApplicantStatusUseCase,
    private val updateApplicantUseCase: UpdateApplicantUseCase,
    private val securityUtils: SecurityUtils,
    private val responseFactory: ResponseEntityFactory,
    private val logger: LoggerPort,
    private val objectMapper: ObjectMapper
) {

    /**
     * Public endpoint for external candidates to submit their application. No authentication required.
     */
    @Operation(summary = "Submit a job application (public, no auth required)")
    @SecurityRequirements
    @PostMapping("/apply", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    suspend fun applyToPosition(
        @RequestPart("data") requestJson: String,
        @RequestPart("file", required = false) filePart: FilePart?
    ): ResponseEntity<ApiResponse<ApplicantResponse>> {
        return try {
            val request = objectMapper.readValue(requestJson, ApplyToPositionRequest::class.java)

            logger.info("New application received for position ${request.positionId} from ${request.email}")

            FileValidator.validateFile(filePart)

            val result = submitApplicationUseCase.execute(
                SubmitApplicationCommand(
                    name = request.name,
                    email = request.email,
                    phone = request.phone,
                    positionId = request.positionId,
                    filePart = filePart,
                    linkedinUrl = request.linkedinUrl,
                    gdprConsent = request.gdprConsent,
                    captchaToken = request.captchaToken
                )
            )

            logger.info("Application ${result.id} created successfully")
            responseFactory.success("Application submitted successfully", result.toResponse())
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid application request: ${e.message}")
            responseFactory.badRequest(e.message ?: "Invalid request")
        } catch (e: Exception) {
            logger.error("Error processing application: ${e.message}", e)
            responseFactory.error("Failed to submit application")
        }
    }

    /**
     * Public endpoint to view available positions. Lists all active positions marked as external.
     * No authentication required.
     */
    @Operation(summary = "Get available external positions (public, no auth required)")
    @SecurityRequirements
    @GetMapping("/positions")
    suspend fun getAvailablePositions(): ResponseEntity<ApiResponse<List<PublicPositionResponse>>> {
        return try {
            val positions = getPublicPositionsUseCase.execute()
            logger.info("Found ${positions.size} available positions")
            responseFactory.success("Available positions retrieved successfully", positions.map { it.toResponse() })
        } catch (e: Exception) {
            logger.error("Error fetching available positions: ${e.message}", e)
            responseFactory.error("Failed to fetch positions")
        }
    }

    /**
     * Public endpoint to view position details. No authentication required.
     */
    @Operation(summary = "Get external position detail by ID (public, no auth required)")
    @SecurityRequirements
    @GetMapping("/positions/{id}")
    suspend fun getPositionDetail(
        @PathVariable id: String
    ): ResponseEntity<ApiResponse<PublicPositionResponse>> {
        return try {
            val position = getPositionByIdUseCase.execute(GetPositionByIdCommand(id))

            if (position == null || !position.isActive || !position.external) {
                logger.warn(
                    "Position $id not found or not available (exists: ${position != null}, active: ${position?.isActive}, external: ${position?.external})"
                )
                return responseFactory.notFound("Position not available")
            }

            val response = PublicPositionResponse(
                id = position.id,
                title = position.title,
                description = position.description,
                fileUrl = position.fileUrl,
                createdAt = position.createdAt,
                workMode = position.workMode,
                location = position.location,
                jobType = position.jobType,
                experienceMin = position.experienceMin,
                experienceMax = position.experienceMax,
                skills = position.skills
            )

            logger.info("Position $id details retrieved successfully")
            responseFactory.success("Position details retrieved successfully", response)
        } catch (e: Exception) {
            logger.error("Error fetching position $id: ${e.message}", e)
            responseFactory.error("Failed to fetch position details")
        }
    }

    /** Gets all applicants with optional filters. ADMIN sees all; RECRUITER sees only applicants from their assigned positions. */
    @Operation(summary = "Get all applicants with optional filters (ADMIN: all, RECRUITER: assigned positions only)")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUITER')")
    suspend fun getAllApplicants(
        @RequestParam(required = false) status: String? = null,
        @RequestParam(required = false) positionId: String? = null,
        @RequestParam(required = false) search: String? = null,
        @RequestParam(required = false, defaultValue = "0") page: Int = 0,
        @RequestParam(required = false, defaultValue = "10") pageSize: Int = 10,
        @RequestParam(required = false) sortField: String? = null,
        @RequestParam(required = false) sortDirection: String? = null
    ): ResponseEntity<ApiResponse<PagedData<ApplicantResponse>>> {
        val pageQuery = try {
            PageQuery(
                page = page,
                pageSize = pageSize,
                sortField = sortField,
                sortDirection = sortDirection?.let { SortDirection.valueOf(it.uppercase()) } ?: SortDirection.DESC
            )
        } catch (e: IllegalArgumentException) {
            return responseFactory.badRequest("Invalid sortDirection: $sortDirection")
        }

        val currentUser = securityUtils.getCurrentUser()
        logger.info(
            "${currentUser.role} ${currentUser.id} requesting applicants - status: $status, positionId: $positionId, search: $search, page: $page, pageSize: $pageSize, sortField: $sortField, sortDirection: $sortDirection"
        )

        return try {
            val result = getApplicantsUseCase.execute(
                GetApplicantsCommand(
                    currentUserId = currentUser.id,
                    isAdmin = currentUser.isAdmin(),
                    status = status,
                    positionId = positionId,
                    search = search,
                    pageQuery = pageQuery
                )
            )

            logger.info("Found ${result.total} applicants, returning page $page")
            responseFactory.success(
                "Applicants retrieved successfully",
                result.map { it.toResponse() }.toPagedData(pageQuery)
            )
        } catch (e: org.springframework.security.access.AccessDeniedException) {
            logger.warn("Access denied for ${currentUser.role} ${currentUser.id}: ${e.message}")
            responseFactory.forbidden("Access denied to the requested resource")
        } catch (e: Exception) {
            logger.error("Error fetching applicants: ${e.message}", e)
            responseFactory.error("Failed to fetch applicants")
        }
    }

    /** Gets a single applicant by ID. ADMIN only. */
    @Operation(summary = "Get applicant by ID (admin only)")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    suspend fun getApplicantById(
        @PathVariable id: String
    ): ResponseEntity<ApiResponse<ApplicantResponse>> {
        val currentUser = securityUtils.getCurrentUser()
        logger.info("Admin ${currentUser.id} requesting applicant $id")

        return try {
            val result = getApplicantByIdUseCase.execute(GetApplicantByIdCommand(id))
                ?: return responseFactory.notFound("Applicant not found")

            logger.info("Applicant $id retrieved successfully")
            responseFactory.success("Applicant retrieved successfully", result.toResponse())
        } catch (e: Exception) {
            logger.error("Error fetching applicant: ${e.message}", e)
            responseFactory.error("Failed to fetch applicant")
        }
    }

    /** Updates applicant status (pending, approved, rejected). ADMIN only. */
    @Operation(summary = "Update applicant status (pending / approved / rejected)")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    suspend fun updateApplicantStatus(
        @PathVariable id: String,
        @RequestBody request: UpdateApplicantStatusRequest
    ): ResponseEntity<ApiResponse<ApplicantResponse>> {
        val currentUser = securityUtils.getCurrentUser()
        logger.info("Admin ${currentUser.id} updating applicant $id status to ${request.status}")

        return try {
            val result = updateApplicantStatusUseCase.execute(
                UpdateApplicantStatusCommand(
                    id = id,
                    newStatus = request.status,
                    reviewedBy = currentUser,
                    statusNote = request.statusNote
                )
            )

            logger.info("Applicant $id status updated to ${request.status}")
            responseFactory.success("Applicant status updated successfully", result.toResponse())
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid status update request: ${e.message}")
            responseFactory.badRequest(e.message ?: "Invalid request")
        } catch (e: Exception) {
            logger.error("Error updating applicant status: ${e.message}", e)
            responseFactory.error("Failed to update applicant status")
        }
    }

    /** Updates applicant details and file. ADMIN only. */
    @Operation(summary = "Update applicant details and/or CV file (admin only)")
    @PutMapping("/{id}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @PreAuthorize("hasRole('ADMIN')")
    suspend fun updateApplicant(
        @PathVariable id: String,
        @RequestPart("data") requestJson: String,
        @RequestPart("file", required = false) filePart: FilePart?
    ): ResponseEntity<ApiResponse<ApplicantResponse>> {
        val currentUser = securityUtils.getCurrentUser()
        logger.info("Admin ${currentUser.id} updating applicant $id")

        return try {
            val request = objectMapper.readValue(requestJson, UpdateApplicantRequest::class.java)

            FileValidator.validateFile(filePart)

            val result = updateApplicantUseCase.execute(
                UpdateApplicantCommand(
                    id = id,
                    name = request.name,
                    email = request.email,
                    phone = request.phone,
                    filePart = filePart,
                    deleteFile = request.deleteFile
                )
            )

            logger.info("Applicant $id updated successfully")
            responseFactory.success("Applicant updated successfully", result.toResponse())
        } catch (e: Exception) {
            logger.error("Error updating applicant: ${e.message}", e)
            responseFactory.error("Failed to update applicant")
        }
    }
}