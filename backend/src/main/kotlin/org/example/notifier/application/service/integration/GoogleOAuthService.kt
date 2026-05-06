package org.example.notifier.application.service.integration

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import org.example.notifier.infrastructure.external.GoogleUserInfo
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*

@Service
class GoogleOAuthService(
    @Value("\${google.client-id}")
    private val clientId: String
) {

    private val verifier: GoogleIdTokenVerifier by lazy {
        GoogleIdTokenVerifier.Builder(NetHttpTransport(), GsonFactory())
            .setAudience(Collections.singletonList(clientId))
            .build()
    }

    /**
     * Verify Google ID token and extract payload
     */
    fun verifyToken(idTokenString: String): GoogleIdToken.Payload? {
        return try {
            val idToken = verifier.verify(idTokenString)
            idToken?.payload
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Extract user info from Google token payload
     */
    fun extractUserInfo(payload: GoogleIdToken.Payload): GoogleUserInfo {
        return GoogleUserInfo(
            googleId = payload.subject,
            email = payload.email,
            name = payload["name"] as? String ?: payload.email,
            pictureUrl = payload["picture"] as? String,
            emailVerified = payload.emailVerified
        )
    }
}
