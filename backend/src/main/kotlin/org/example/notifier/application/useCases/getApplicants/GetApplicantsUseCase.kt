package org.example.notifier.application.useCases.getApplicants

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.example.notifier.application.model.applicant.ApplicantItem
import org.example.notifier.application.model.shared.PagedResult
import org.example.notifier.application.model.shared.applySortAndPage
import org.example.notifier.application.model.applicant.toApplicantItem
import org.example.notifier.application.model.assessment.toAssessmentInvitationItem
import org.example.notifier.application.service.core.ApplicantService
import org.example.notifier.application.service.core.InvitationService
import org.example.notifier.application.service.core.OpenPositionService
import org.example.notifier.domain.applicant.Applicant
import org.example.notifier.domain.applicant.CandidatePositionKey
import org.example.notifier.domain.shared.SystemConstants
import org.example.notifier.infrastructure.logging.LoggerPort
import org.springframework.stereotype.Component

@Component
class GetApplicantsUseCase(
    private val applicantService: ApplicantService,
    private val openPositionService: OpenPositionService,
    private val invitationService: InvitationService,
    private val logger: LoggerPort
) {

    suspend fun execute(command: GetApplicantsCommand): PagedResult<ApplicantItem> {
        logger.info("Fetching applicants for recruiter ${command.currentUserId} - status: ${command.status}, positionId: ${command.positionId}, search: ${command.search}")

        val applicants = fetchApplicants(command)
        if (applicants.isEmpty()) return PagedResult(items = emptyList(), total = 0)

        return enrichAndPage(applicants, command)
    }

    private suspend fun fetchApplicants(command: GetApplicantsCommand): List<Applicant> {
        if (command.isAdmin) {
            return applicantService.getAllApplicants(command.status, command.positionId, command.search)
        }

        val recruiterInvitations = invitationService.findByRecruiterId(command.currentUserId)
        if (recruiterInvitations.isEmpty()) return emptyList()

        val invitedKeys = recruiterInvitations
            .map { CandidatePositionKey(it.candidateEmail, it.openPositionId) }
            .toSet()

        return applicantService.getApplicantsForRecruiter(
            invitedKeys,
            command.status,
            command.positionId,
            command.search
        )
    }

    private suspend fun enrichAndPage(
        applicants: List<Applicant>,
        command: GetApplicantsCommand
    ): PagedResult<ApplicantItem> = coroutineScope {
        val invitationMapDeferred = async {
            invitationService.findByRecruiterId(SystemConstants.SYSTEM_RECRUITER_ID)
                .groupBy { CandidatePositionKey(it.candidateEmail, it.openPositionId) }
        }
        val positionsMap = applicants.mapTo(LinkedHashSet()) { it.positionId }
            .map { pid -> async { pid to openPositionService.getPosition(pid) } }
            .awaitAll()
            .toMap()
        val invitationMap = invitationMapDeferred.await()

        applicants
            .map { applicant ->
                val assessments = invitationMap[CandidatePositionKey(applicant.email, applicant.positionId)]
                    ?.map { it.toAssessmentInvitationItem() }
                applicant.toApplicantItem(positionsMap[applicant.positionId]?.title, assessments)
            }
            .applySortAndPage(
                pageQuery = command.pageQuery,
                comparators = mapOf(
                    "name"      to compareBy<ApplicantItem> { it.name.lowercase() },
                    "email"     to compareBy { it.email.lowercase() },
                    "createdAt" to compareBy { it.createdAt }
                ),
                default = compareBy<ApplicantItem> { it.createdAt }.reversed()
            )
    }
}
