package org.example.notifier.infrastructure.controller

import org.example.notifier.application.service.integration.Auth0Service
import org.example.notifier.application.useCases.auth0Callback.Auth0CallbackCommand
import org.example.notifier.application.useCases.auth0Callback.Auth0CallbackResult
import org.example.notifier.application.useCases.auth0Callback.Auth0CallbackUseCase
import org.example.notifier.infrastructure.logging.LoggerPort
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "Auth - Auth0", description = "Auth0 OAuth2 login, callback and logout flow")
@SecurityRequirements
@RestController
@RequestMapping
class Auth0SSOController(
    private val auth0Service: Auth0Service,
    private val auth0CallbackUseCase: Auth0CallbackUseCase,
    @Value("\${frontend.url}")
    private val frontendUrl: String,
    @Value("\${auth0.domain}")
    private val auth0Domain: String,
    @Value("\${auth0.client-id}")
    private val auth0ClientId: String,
    private val logger: LoggerPort
) {

    @Operation(summary = "Initiate Auth0 login — redirects to Auth0 authorization page")
    @GetMapping("/auth0-login")
    fun initiateAuth0Login(): ResponseEntity<Unit> {
        return try {
            logger.info("Initiating Auth0 login")
            val authorizationUrl = auth0Service.getAuthorizationUrl()
            logger.info("Redirecting to Auth0: $authorizationUrl")
            redirect(authorizationUrl)
        } catch (e: Exception) {
            logger.error("Failed to initiate Auth0 login: ${e.message}", e)
            redirect("$frontendUrl/login?gisError=${encodeUrl("Failed to initiate login. Please try again.")}")
        }
    }

    @Operation(summary = "Auth0 callback — exchanges authorization code for JWT")
    @GetMapping("/auth0-callback")
    suspend fun handleAuth0Callback(
        @RequestParam("code", required = false) code: String?,
        @RequestParam("error", required = false) error: String?,
        @RequestParam("error_description", required = false) errorDescription: String?
    ): ResponseEntity<Unit> {
        logger.info("Received Auth0 callback — code present: ${code != null}, error: $error")

        if (error != null) {
            logger.warn("Auth0 callback error: $error — $errorDescription")
            return redirect("$frontendUrl/login?gisError=${encodeUrl(errorDescription ?: "Authentication failed")}")
        }

        if (code.isNullOrBlank()) {
            logger.warn("Auth0 callback — code is null or blank")
            return redirect("$frontendUrl/login?gisError=${encodeUrl("Invalid authentication data")}")
        }

        return try {
            when (val result = auth0CallbackUseCase.execute(Auth0CallbackCommand(code))) {
                is Auth0CallbackResult.Success ->
                    redirect("$frontendUrl/google-sso?id=${result.userId}&token=${result.authToken}&permissions=${encodeUrl(result.role)}")
                is Auth0CallbackResult.InvalidCode ->
                    redirect("$frontendUrl/login?gisError=${encodeUrl("Failed to obtain tokens from Auth0")}")
                is Auth0CallbackResult.EmailNotVerified ->
                    redirect("$frontendUrl/login?gisError=${encodeUrl("Email not verified. Please check your email and verify your account.")}")
                is Auth0CallbackResult.AccessDenied ->
                    redirect("$frontendUrl/login?gisError=${encodeUrl("Access denied. Your account has not been invited to this platform. Please contact an administrator.")}")
                is Auth0CallbackResult.AccountDeactivated ->
                    redirect("$frontendUrl/login?gisError=${encodeUrl("Your account has been deactivated. Please contact an administrator.")}")
            }
        } catch (e: Exception) {
            logger.warn("Auth0 callback — exception during processing: ${e.message}", e)
            redirect("$frontendUrl/login?gisError=${encodeUrl("Authentication failed. Please try again.")}")
        }
    }

    @Operation(summary = "Auth0 logout — redirects to Auth0 logout endpoint")
    @GetMapping("/auth0-logout")
    fun handleAuth0Logout(): ResponseEntity<Unit> {
        return try {
            logger.info("Initiating Auth0 logout")
            val returnTo = encodeUrl("$frontendUrl/login")
            val logoutUrl = "https://$auth0Domain/v2/logout?client_id=$auth0ClientId&returnTo=$returnTo"
            logger.info("Redirecting to Auth0 logout: $logoutUrl")
            redirect(logoutUrl)
        } catch (e: Exception) {
            logger.error("Failed to initiate Auth0 logout: ${e.message}", e)
            redirect("$frontendUrl/login")
        }
    }

    private fun redirect(url: String): ResponseEntity<Unit> =
        ResponseEntity.status(HttpStatus.FOUND).location(URI.create(url)).build()

    private fun encodeUrl(value: String): String =
        URLEncoder.encode(value, StandardCharsets.UTF_8.toString())
}
