package org.example.notifier.infrastructure.controller

import kotlinx.coroutines.reactive.awaitFirst
import org.example.notifier.application.useCases.googleSSOLogin.GoogleSSOLoginCommand
import org.example.notifier.application.useCases.googleSSOLogin.GoogleSSOLoginResult
import org.example.notifier.application.useCases.googleSSOLogin.GoogleSSOLoginUseCase
import org.example.notifier.infrastructure.logging.LoggerPort
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "Auth - Google SSO", description = "Google One Tap / Sign-In authentication flow")
@SecurityRequirements
@RestController
@RequestMapping("/google-sso")
class GoogleSSOController(
    private val googleSSOLoginUseCase: GoogleSSOLoginUseCase,
    @Value("\${frontend.url}")
    private val frontendUrl: String,
    private val logger: LoggerPort
) {

    @Operation(summary = "Authenticate via Google One Tap — redirects to frontend with JWT")
    @PostMapping
    suspend fun handleGoogleCallback(exchange: ServerWebExchange): ResponseEntity<Unit> {
        val formData = exchange.formData.awaitFirst()
        val credential = formData.getFirst("credential")

        logger.info("Received Google SSO callback — credential present: ${credential != null}")

        if (credential.isNullOrBlank()) {
            logger.warn("Google SSO callback — credential is null or blank")
            return redirect("$frontendUrl/login?gisError=${encodeUrl("Invalid authentication data")}")
        }

        return try {
            when (val result = googleSSOLoginUseCase.execute(GoogleSSOLoginCommand(credential))) {
                is GoogleSSOLoginResult.Success ->
                    redirect("$frontendUrl/google-sso?id=${result.userId}&token=${result.authToken}&permissions=${encodeUrl(result.role)}")
                is GoogleSSOLoginResult.InvalidCredential ->
                    redirect("$frontendUrl/login?gisError=${encodeUrl("Invalid Google token")}")
                is GoogleSSOLoginResult.EmailNotVerified ->
                    redirect("$frontendUrl/login?gisError=${encodeUrl("Email not verified")}")
                is GoogleSSOLoginResult.AccessDenied ->
                    redirect("$frontendUrl/login?gisError=${encodeUrl("Access denied. Your account has not been invited to this platform. Please contact an administrator.")}")
                is GoogleSSOLoginResult.AccountDeactivated ->
                    redirect("$frontendUrl/login?gisError=${encodeUrl("Your account has been deactivated. Please contact an administrator.")}")
            }
        } catch (e: Exception) {
            logger.warn("Google SSO callback — exception during processing: ${e.message}", e)
            redirect("$frontendUrl/login?gisError=${encodeUrl("Authentication failed. Please try again.")}")
        }
    }

    private fun redirect(url: String): ResponseEntity<Unit> =
        ResponseEntity.status(HttpStatus.FOUND).location(URI.create(url)).build()

    private fun encodeUrl(value: String): String =
        URLEncoder.encode(value, StandardCharsets.UTF_8.toString())
}
