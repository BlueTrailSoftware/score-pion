package org.example.notifier.infrastructure.controller

import org.example.notifier.application.model.shared.PageQuery
import org.example.notifier.application.model.shared.map
import org.example.notifier.application.useCases.getCandidates.GetCandidatesCommand
import org.example.notifier.application.useCases.getCandidates.GetCandidatesUseCase
import org.example.notifier.application.useCases.inviteCandidate.InviteCandidateCommand
import org.example.notifier.application.useCases.inviteCandidate.InviteCandidateUseCase
import org.example.notifier.infrastructure.dto.mapper.toResponse
import org.example.notifier.infrastructure.dto.request.InviteCandidateRequest
import org.example.notifier.infrastructure.dto.response.toPagedData
import org.example.notifier.infrastructure.dto.response.ApiResponse
import org.example.notifier.infrastructure.dto.response.CandidateInvitationResponse
import org.example.notifier.infrastructure.dto.response.PagedData
import org.example.notifier.infrastructure.logging.LoggerPort
import org.example.notifier.infrastructure.security.SecurityUtils
import org.example.notifier.infrastructure.util.cappedPageSize
import org.example.notifier.infrastructure.util.factory.ResponseEntityFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "Candidates", description = "Manage candidates and send assessment invitations")
@RestController
@RequestMapping("/candidates")
class CandidatesController(
    private val getCandidatesUseCase: GetCandidatesUseCase,
    private val inviteCandidateUseCase: InviteCandidateUseCase,
    private val securityUtils: SecurityUtils,
    private val responseFactory: ResponseEntityFactory,
    private val logger: LoggerPort
) {

    /**
     * Gets candidates invited by the current recruiter, grouped by candidate+position.
     * ADMIN can optionally filter by recruiterId; RECRUITER always sees only their own.
     */
    @Operation(summary = "Get candidates (ADMIN: all or filter by recruiterId, RECRUITER: own candidates only)")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUITER')")
    suspend fun getCandidates(
        @RequestParam(required = false) recruiterId: String? = null,
        @RequestParam(required = false) search: String? = null,
        @RequestParam(required = false, defaultValue = "0") page: Int = 0,
        @RequestParam(required = false, defaultValue = "10") pageSize: Int = 10
    ): ResponseEntity<ApiResponse<PagedData<CandidateInvitationResponse>>> {
        val currentUser = securityUtils.getCurrentUser()

        val effectiveRecruiterId = when {
            currentUser.isAdmin() -> recruiterId
            else -> currentUser.id
        }

        logger.info("${currentUser.role} ${currentUser.id} requesting candidates for recruiterId=$effectiveRecruiterId search=$search page=$page pageSize=$pageSize")

        val pageQuery = PageQuery(page = page, pageSize = pageSize.cappedPageSize())

        return try {
            val result = getCandidatesUseCase.execute(
                GetCandidatesCommand(
                    recruiterId = effectiveRecruiterId,
                    search = search,
                    pageQuery = pageQuery
                )
            )
            logger.info("Found ${result.total} candidates, returning page $page")
            responseFactory.success(
                "Candidates retrieved successfully",
                result.map { it.toResponse() }.toPagedData(pageQuery)
            )
        } catch (e: Exception) {
            logger.error("Error fetching candidates: ${e.message}", e)
            responseFactory.error("Failed to fetch candidates")
        }
    }

    /**
     * Invites a candidate to all assessments associated with a position.
     * Both ADMIN and RECRUITER roles can invite candidates.
     */
    @Operation(summary = "Invite a candidate to a position's assessments")
    @PostMapping("/invite")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUITER')")
    suspend fun inviteCandidate(@RequestBody request: InviteCandidateRequest): ResponseEntity<ApiResponse<Unit>> {
        val userId = securityUtils.getCurrentUserId()

        logger.info("User $userId inviting candidate ${request.email} to position ${request.positionId}")

        return try {
            inviteCandidateUseCase.execute(
                InviteCandidateCommand(
                    candidateEmail = request.email,
                    candidateName = request.candidateName,
                    positionId = request.positionId,
                    recruiterId = userId
                )
            )
            logger.info("Successfully invited candidate ${request.email} to position ${request.positionId}")
            responseFactory.noContent()
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid request: ${e.message}")
            responseFactory.badRequest(e.message ?: "Invalid request")
        } catch (e: Exception) {
            logger.error("Error inviting candidate: ${e.message}", e)
            responseFactory.error("Failed to invite candidate")
        }
    }
}
