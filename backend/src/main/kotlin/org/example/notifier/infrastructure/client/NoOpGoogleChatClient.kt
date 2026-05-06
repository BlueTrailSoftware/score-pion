package org.example.notifier.infrastructure.client

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

private val logger = LoggerFactory.getLogger(NoOpGoogleChatClient::class.java)

@Component
@ConditionalOnProperty(name = ["app.debug.enabled"], havingValue = "true")
class NoOpGoogleChatClient : GoogleChatClient {

    override suspend fun sendMessage(webhookUrl: String, message: String) {
        logger.info("[DEBUG] Skipping Google Chat message to webhook: {}", webhookUrl)
    }
}