package org.example.notifier.controller.AdminUsersController

import org.example.notifier.application.useCases.getAllAdmins.GetAllAdminsUseCase
import org.example.notifier.application.useCases.getPendingInvitations.GetPendingInvitationsUseCase
import org.example.notifier.application.useCases.inviteUser.InviteUserUseCase
import org.example.notifier.application.useCases.updateAdminActiveStatus.UpdateAdminActiveStatusUseCase
import org.example.notifier.infrastructure.controller.AdminUsersController
import org.example.notifier.infrastructure.dto.request.InviteAdminRequest
import org.example.notifier.infrastructure.logging.LoggerPort
import org.example.notifier.infrastructure.security.SecurityUtils
import org.example.notifier.infrastructure.util.factory.ResponseEntityFactory
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf
import org.springframework.test.web.reactive.server.WebTestClient
import org.example.notifier.application.service.security.AuthTokenService
import org.example.notifier.application.service.core.UserService
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.context.annotation.Import
import org.example.notifier.infrastructure.security.SecurityConfig
import org.example.notifier.infrastructure.security.JwtAuthenticationWebFilter

@WebFluxTest(controllers = [AdminUsersController::class])
@Import(SecurityConfig::class, JwtAuthenticationWebFilter::class)
class AdminSecurityIntegrationTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockBean
    private lateinit var inviteUserUseCase: InviteUserUseCase

    @MockBean
    private lateinit var getAllAdminsUseCase: GetAllAdminsUseCase

    @MockBean
    private lateinit var updateAdminActiveStatusUseCase: UpdateAdminActiveStatusUseCase

    @MockBean
    private lateinit var getPendingInvitationsUseCase: GetPendingInvitationsUseCase

    @MockBean
    private lateinit var securityUtils: SecurityUtils

    @MockBean
    private lateinit var responseFactory: ResponseEntityFactory

    @MockBean
    private lateinit var logger: LoggerPort

    @MockBean
    private lateinit var authTokenService: AuthTokenService

    @MockBean
    private lateinit var userService: UserService

    @MockBean
    private lateinit var corsConfigurationSource: CorsConfigurationSource

    @Test
    @WithMockUser(roles = ["RECRUITER"])
    fun `POST admin invite should return 403 Forbidden for RECRUITER role`() {
        // A user with ROLE_RECRUITER attempts to invite another Admin
        webTestClient
            .mutateWith(csrf())
            .post().uri("/admin/invite")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"email":"hacker@example.com"}""")
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `POST admin invite should not return 403 Forbidden for ADMIN role`() {
        // A user with ROLE_ADMIN attempts to invite another Admin
        webTestClient
            .mutateWith(csrf())
            .post().uri("/admin/invite")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"email":"newadmin@example.com"}""")
            .exchange()
            // It might return OK, or 500 depending on UseCase mocks, but NOT 403 or 401
            .expectStatus().is2xxSuccessful
    }

    @Test
    @WithMockUser(roles = ["USER"])
    fun `POST admin invite should return 403 Forbidden for generic USER role`() {
        // A generic user attempts to invite an Admin
        webTestClient
            .mutateWith(csrf())
            .post().uri("/admin/invite")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"email":"hacker@example.com"}""")
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    @WithMockUser(roles = ["HACKER_INTRUDER"])
    fun `POST admin invite should return 403 Forbidden for any RANDOM role`() {
        // Someone with an invented/stolen role attempts to invite an Admin
        webTestClient
            .mutateWith(csrf())
            .post().uri("/admin/invite")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"email":"hacker@example.com"}""")
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun `POST admin invite should return 401 Unauthorized when NO token is provided`() {
        // An anonymous request without any JWT token
        webTestClient
            .mutateWith(csrf())
            .post().uri("/admin/invite")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"email":"anon@example.com"}""")
            .exchange()
            // Without @WithMockUser, the request is completely unauthenticated
            .expectStatus().isUnauthorized
    }
}
