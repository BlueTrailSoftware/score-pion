package org.example.notifier.infrastructure.adapter.coderbyte.webhook

import org.example.notifier.infrastructure.external.coderbyte.CoderbyteWebhook
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CoderbyteWebhookAdapterTest {

    private val webhook = CoderbyteWebhook(
        operation = "assessment_completed",
        organizationId = "org-1",
        email = "candidate@test.com",
        reportUrl = "https://coderbyte.com/report/123",
        assessmentId = "assessment-1",
        reportReady = true,
        timeExpired = true
    )

    @Test
    fun `toAssessmentStartedEvent maps fields correctly`() {
        val event = CoderbyteWebhookAdapter.toAssessmentStartedEvent(webhook)

        assertEquals("candidate@test.com", event.candidateEmail)
        assertEquals("assessment-1", event.assessmentId)
        assertEquals("org-1", event.organizationId)
    }

    @Test
    fun `toAssessmentCompletedEvent maps fields correctly`() {
        val event = CoderbyteWebhookAdapter.toAssessmentCompletedEvent(webhook)

        assertEquals("candidate@test.com", event.candidateEmail)
        assertEquals("assessment-1", event.assessmentId)
        assertTrue(event.isReportReady)
        assertTrue(event.wasTimeExpired)
        assertEquals("org-1", event.organizationId)
    }
}
