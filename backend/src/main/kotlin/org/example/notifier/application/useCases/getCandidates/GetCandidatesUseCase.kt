package org.example.notifier.application.useCases.getCandidates

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.example.notifier.application.model.applicant.CandidateItem
import org.example.notifier.application.model.shared.PagedResult
import org.example.notifier.application.model.shared.applySortAndPage
import org.example.notifier.application.model.assessment.toAssessmentInvitationItem
import org.example.notifier.application.service.core.InvitationService
import org.example.notifier.application.service.core.OpenPositionService
import org.example.notifier.domain.applicant.CandidatePositionKey
import org.example.notifier.domain.invitation.Invitation
import org.example.notifier.domain.position.OpenPosition
import org.example.notifier.domain.shared.SystemConstants
import org.springframework.stereotype.Component

@Component
class GetCandidatesUseCase(
    private val invitationService: InvitationService,
    private val openPositionService: OpenPositionService
) {

    suspend fun execute(command: GetCandidatesCommand): PagedResult<CandidateItem> {
        val invitations = fetchInvitations(command.recruiterId)

        return coroutineScope {
            val positionsMap = fetchPositionsMap(invitations)

            buildCandidateItems(invitations, positionsMap)
                .filterBySearch(command.search)
                .applySortAndPage(
                    pageQuery = command.pageQuery,
                    comparators = emptyMap(),
                    default = compareBy<CandidateItem> { it.invitedAt }.reversed()
                )
        }
    }

    private suspend fun fetchInvitations(recruiterId: String?): List<Invitation> =
        if (recruiterId != null) {
            invitationService.findByRecruiterId(recruiterId)
        } else {
            invitationService.findAll()
                .filter { it.recruiterId != SystemConstants.SYSTEM_RECRUITER_ID }
        }

    private suspend fun fetchPositionsMap(invitations: List<Invitation>): Map<String, OpenPosition?> =
        coroutineScope {
            invitations
                .mapTo(LinkedHashSet()) { it.openPositionId }
                .map { pid -> async { pid to openPositionService.getPosition(pid) } }
                .awaitAll()
                .toMap()
        }

    private fun buildCandidateItems(
        invitations: List<Invitation>,
        positionsMap: Map<String, OpenPosition?>
    ): List<CandidateItem> =
        invitations
            .groupBy { CandidatePositionKey(it.candidateEmail, it.openPositionId) }
            .map { (_, candidateInvitations) ->
                val first = candidateInvitations.first()
                CandidateItem(
                    candidateEmail = first.candidateEmail,
                    candidateName = first.candidateName,
                    positionId = first.openPositionId,
                    positionTitle = positionsMap[first.openPositionId]?.title,
                    recruiterId = first.recruiterId,
                    assessments = candidateInvitations.map { it.toAssessmentInvitationItem() },
                    invitedAt = candidateInvitations.minOf { it.createdAt }
                )
            }

    private fun List<CandidateItem>.filterBySearch(search: String?): List<CandidateItem> {
        if (search.isNullOrBlank()) return this
        val q = search.trim().lowercase()
        return filter { c ->
            c.candidateName.lowercase().contains(q) ||
            c.candidateEmail.lowercase().contains(q) ||
            (c.positionTitle?.lowercase()?.contains(q) == true)
        }
    }
}