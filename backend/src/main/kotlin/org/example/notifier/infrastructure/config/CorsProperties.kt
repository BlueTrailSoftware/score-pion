package org.example.notifier.infrastructure.config

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

private val logger = LoggerFactory.getLogger(CorsProperties::class.java)

@Component
@ConfigurationProperties(prefix = "cors")
class CorsProperties {
    lateinit var allowedOrigins: List<String>

    @PostConstruct
    fun logCorsOrigins() {
        logger.info("Allowed CORS origins: {}", allowedOrigins)
    }
}
