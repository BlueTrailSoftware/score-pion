package org.example.notifier.infrastructure.adapter.coderbyte.webhook

import org.example.notifier.domain.event.AssessmentCompletedEvent
import org.example.notifier.domain.event.AssessmentStartedEvent
import org.example.notifier.infrastructure.external.coderbyte.CoderbyteWebhook

object CoderbyteWebhookAdapter {

    fun toAssessmentStartedEvent(webhook: CoderbyteWebhook): AssessmentStartedEvent {
        return AssessmentStartedEvent(
            candidateEmail = webhook.email.trim().lowercase(),
            assessmentId = webhook.assessmentId,
            organizationId = webhook.organizationId
        )
    }

    fun toAssessmentCompletedEvent(webhook: CoderbyteWebhook): AssessmentCompletedEvent {
        return AssessmentCompletedEvent(
            candidateEmail = webhook.email.trim().lowercase(),
            assessmentId = webhook.assessmentId,
            isReportReady = webhook.reportReady,
            wasTimeExpired = webhook.timeExpired,
            organizationId = webhook.organizationId
        )
    }
}
