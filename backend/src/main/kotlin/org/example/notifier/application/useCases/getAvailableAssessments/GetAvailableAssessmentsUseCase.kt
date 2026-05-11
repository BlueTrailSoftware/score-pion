package org.example.notifier.application.useCases.getAvailableAssessments

import org.example.notifier.application.model.assessment.AssessmentItem
import org.example.notifier.application.service.integration.AssessmentPlatformService
import org.springframework.stereotype.Component

@Component
class GetAvailableAssessmentsUseCase(
    private val assessmentPlatformService: AssessmentPlatformService
) {

    suspend fun execute(): List<AssessmentItem> {
        return assessmentPlatformService.getAvailableAssessments().map { info ->
            AssessmentItem(
                displayName = info.title,
                testId = info.id
            )
        }
    }
}
