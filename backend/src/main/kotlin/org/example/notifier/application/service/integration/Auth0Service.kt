package org.example.notifier.application.service.integration

import com.auth0.jwk.JwkProviderBuilder
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import org.example.notifier.infrastructure.external.Auth0UserInfo
import org.example.notifier.infrastructure.logging.LoggerPort
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.interfaces.RSAPublicKey

@Service
class Auth0Service(
    @Value("\${auth0.domain}")
    private val domain: String,
    @Value("\${auth0.client-id}")
    private val clientId: String,
    @Value("\${auth0.client-secret}")
    private val clientSecret: String,
    @Value("\${auth0.redirect-uri}")
    private val redirectUri: String,
    private val logger: LoggerPort
) {

    private val webClient = WebClient.builder()
        .baseUrl("https://$domain")
        .build()

    private val jwkProvider by lazy {
        try {
            JwkProviderBuilder(URL("https://$domain/.well-known/jwks.json"))
                .build()
        } catch (e: Exception) {
            logger.error("Failed to initialize JWK provider: ${e.message}", e)
            throw e
        }
    }

    /**
     * Generate Auth0 authorization URL
     */
    fun getAuthorizationUrl(): String {
        val scope = "openid profile email"
        val encodedRedirectUri = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8.toString())
        val encodedScope = URLEncoder.encode(scope, StandardCharsets.UTF_8.toString())
        
        return "https://$domain/authorize?" +
                "response_type=code&" +
                "client_id=$clientId&" +
                "redirect_uri=$encodedRedirectUri&" +
                "scope=$encodedScope"
    }

    /**
     * Exchange authorization code for tokens
     */
    suspend fun exchangeCodeForTokens(code: String): TokenResponse? {
        return try {
            webClient.post()
                .uri("/oauth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(
                    mapOf(
                        "grant_type" to "authorization_code",
                        "client_id" to clientId,
                        "client_secret" to clientSecret,
                        "code" to code,
                        "redirect_uri" to redirectUri
                    )
                )
                .retrieve()
                .awaitBody<TokenResponse>()
        } catch (e: Exception) {
            logger.error("Failed to exchange code for tokens: ${e.message}", e)
            null
        }
    }

    /**
     * Verify and decode Auth0 ID token
     */
    fun verifyIdToken(idToken: String): DecodedJWT? {
        return try {
            val jwt = JWT.decode(idToken)
            val jwk = jwkProvider.get(jwt.keyId)
            val algorithm = Algorithm.RSA256(jwk.publicKey as RSAPublicKey, null)
            
            val verifier = JWT.require(algorithm)
                .withIssuer("https://$domain/")
                .withAudience(clientId)
                .build()
            
            verifier.verify(idToken)
        } catch (e: Exception) {
            logger.error("Failed to verify ID token: ${e.message}", e)
            null
        }
    }

    /**
     * Extract user info from Auth0 ID token
     */
    fun extractUserInfo(jwt: DecodedJWT): Auth0UserInfo {
        return Auth0UserInfo(
            auth0Id = jwt.subject,
            email = jwt.getClaim("email").asString(),
            name = jwt.getClaim("name").asString()?.takeIf { it.isNotBlank() } ?: jwt.getClaim("email").asString(),
            pictureUrl = jwt.getClaim("picture").asString(),
            emailVerified = jwt.getClaim("email_verified").asBoolean() ?: false
        )
    }

    data class TokenResponse(
        val access_token: String,
        val id_token: String,
        val token_type: String,
        val expires_in: Int
    )
}
