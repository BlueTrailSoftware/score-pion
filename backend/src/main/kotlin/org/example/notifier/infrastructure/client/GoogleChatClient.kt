package org.example.notifier.infrastructure.client

/**
 * Google Chat webhook client abstraction
 */
interface GoogleChatClient {

    /**
     * Sends a message to Google Chat webhook
     */
    suspend fun sendMessage(webhookUrl: String, message: String)
}
