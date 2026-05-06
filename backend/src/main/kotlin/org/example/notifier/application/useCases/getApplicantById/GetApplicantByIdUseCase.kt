package org.example.notifier.application.useCases.getApplicantById

import org.example.notifier.application.model.applicant.ApplicantItem
import org.example.notifier.application.model.applicant.toApplicantItem
import org.example.notifier.application.service.core.ApplicantService
import org.example.notifier.application.service.core.OpenPositionService
import org.springframework.stereotype.Component

@Component
class GetApplicantByIdUseCase(
    private val applicantService: ApplicantService,
    private val openPositionService: OpenPositionService
) {

    suspend fun execute(command: GetApplicantByIdCommand): ApplicantItem? {
        val applicant = applicantService.getApplicantById(command.id) ?: return null
        val position = openPositionService.getPosition(applicant.positionId)
        return applicant.toApplicantItem(position?.title)
    }
}