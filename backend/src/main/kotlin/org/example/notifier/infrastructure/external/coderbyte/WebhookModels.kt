package org.example.notifier.infrastructure.external.coderbyte

import com.fasterxml.jackson.annotation.JsonProperty

data class CoderbyteWebhook(
    val operation: String,
    @JsonProperty("organization_id")
    val organizationId: String,
    val email: String,
    @JsonProperty("report_url")
    val reportUrl: String,
    @JsonProperty("assessment_id")
    val assessmentId: String,
    @JsonProperty("report_ready")
    val reportReady: Boolean,
    @JsonProperty("time_expired")
    val timeExpired: Boolean
)
