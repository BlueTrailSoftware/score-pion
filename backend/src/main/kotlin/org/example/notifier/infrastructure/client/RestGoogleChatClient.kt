package org.example.notifier.infrastructure.client

import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

private val logger = LoggerFactory.getLogger(RestGoogleChatClient::class.java)

@Component
@ConditionalOnProperty(name = ["app.debug.enabled"], havingValue = "false", matchIfMissing = true)
class RestGoogleChatClient : GoogleChatClient {

    private val chatClient = WebClient.create()

    override suspend fun sendMessage(webhookUrl: String, message: String) {
        logger.debug("Sending message to Google Chat webhook: {}", webhookUrl)

        try {
            chatClient.post()
                .uri(webhookUrl)
                .bodyValue(mapOf("text" to message))
                .retrieve()
                .toBodilessEntity()
                .awaitSingleOrNull()

            logger.debug("Message sent successfully to Google Chat")
        } catch (e: Exception) {
            logger.error(
                "Failed to send message to Google Chat webhook. " +
                "URL: {}, Error: {}",
                webhookUrl,
                e.message
            )
            throw e  // Re-throw to let service layer handle it
        }
    }
}
