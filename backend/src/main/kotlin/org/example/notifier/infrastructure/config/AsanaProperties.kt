package org.example.notifier.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "ticket.manager")
data class AsanaProperties(
    var token: String = "",
    var projectId: String = "",
    var apiUrl: String = "https://app.asana.com/api/1.0"
)
