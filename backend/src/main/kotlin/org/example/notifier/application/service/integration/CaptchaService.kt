package org.example.notifier.application.service.integration

import org.example.notifier.infrastructure.external.RecaptchaResponse
import org.example.notifier.infrastructure.logging.LoggerPort
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

/**
 * Service for validating Google reCAPTCHA v3 tokens.
 * Protects public endpoints from spam and bot attacks.
 */
@Service
class CaptchaService(
    private val logger: LoggerPort,
    @Value("\${recaptcha.secret.key}") private val secretKey: String,
    @Value("\${recaptcha.verification.url}") private val verificationUrl: String,
    @Value("\${recaptcha.min.score}") private val minScore: Double
) {

    private val webClient = WebClient.builder().build()

    /**
     * Validates a reCAPTCHA token with Google's API.
     * 
     * @param token The reCAPTCHA token from the frontend
     * @param expectedAction The expected action name (e.g., "apply")
     * @return true if validation passes, false otherwise
     */
    suspend fun validateToken(token: String, expectedAction: String): Boolean {
        return try {
            logger.debug("Validating reCAPTCHA token for action: $expectedAction")
            
            val formData = LinkedMultiValueMap<String, String>().apply {
                add("secret", secretKey)
                add("response", token)
            }

            val response = webClient.post()
                .uri(verificationUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .awaitBody<RecaptchaResponse>()
            
            val isValid = response.success && 
                         response.score >= minScore && 
                         response.action == expectedAction
            
            if (!isValid) {
                logger.warn(
                    "reCAPTCHA validation failed - success: ${response.success}, " +
                    "score: ${response.score}, action: ${response.action}, " +
                    "expected action: $expectedAction"
                )
            } else {
                logger.debug("reCAPTCHA validation successful - score: ${response.score}")
            }
            
            isValid
            
        } catch (e: Exception) {
            logger.error("Error validating reCAPTCHA token: ${e.message}", e)
            false
        }
    }
}
