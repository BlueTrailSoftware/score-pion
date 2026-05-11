package org.example.notifier.security

import org.example.notifier.domain.user.User
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockAuthentication
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.LocalDateTime

object AuthMocks {

    private val now = LocalDateTime.of(2026, 1, 1, 0, 0)

    val adminUser = User(
        id = "admin-test-1",
        email = "admin@test.com",
        name = "Test Admin",
        role = "ADMIN",
        isActive = true,
        createdAt = now,
        updatedAt = now
    )

    val recruiterUser = User(
        id = "rec-test-1",
        email = "recruiter@test.com",
        name = "Test Recruiter",
        role = "RECRUITER",
        isActive = true,
        createdAt = now,
        updatedAt = now
    )

    private fun adminAuthentication(): Authentication =
        UsernamePasswordAuthenticationToken(
            adminUser, null, listOf(SimpleGrantedAuthority("ROLE_ADMIN"))
        )

    private fun recruiterAuthentication(): Authentication =
        UsernamePasswordAuthenticationToken(
            recruiterUser, null, listOf(SimpleGrantedAuthority("ROLE_RECRUITER"))
        )

    fun WebTestClient.withAdmin(): WebTestClient =
        mutateWith(mockAuthentication(adminAuthentication()))

    fun WebTestClient.withRecruiter(): WebTestClient =
        mutateWith(mockAuthentication(recruiterAuthentication()))
}
