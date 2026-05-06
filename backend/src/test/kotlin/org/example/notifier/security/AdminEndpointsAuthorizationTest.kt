package org.example.notifier.security

import org.example.notifier.application.useCases.createAsanaTicket.CreateAsanaTicketUseCase
import org.example.notifier.application.useCases.createPosition.CreatePositionUseCase
import org.example.notifier.application.useCases.getAllAdmins.GetAllAdminsUseCase
import org.example.notifier.application.useCases.getAllPositions.GetAllPositionsUseCase
import org.example.notifier.application.useCases.getAvailableAssessments.GetAvailableAssessmentsUseCase
import org.example.notifier.application.useCases.getPendingInvitations.GetPendingInvitationsUseCase
import org.example.notifier.application.useCases.getPositionById.GetPositionByIdUseCase
import org.example.notifier.application.useCases.getRecruiterDetail.GetRecruiterDetailUseCase
import org.example.notifier.application.useCases.getRecruiterPositions.GetRecruiterPositionsUseCase
import org.example.notifier.application.useCases.getRecruiters.GetRecruitersUseCase
import org.example.notifier.application.useCases.inviteUser.InviteUserUseCase
import org.example.notifier.application.useCases.syncRecruiterPositions.SyncRecruiterPositionsUseCase
import org.example.notifier.application.useCases.updateAdminActiveStatus.UpdateAdminActiveStatusUseCase
import org.example.notifier.application.useCases.updatePosition.UpdatePositionUseCase
import org.example.notifier.application.useCases.updatePositionActiveStatus.UpdatePositionActiveStatusUseCase
import org.example.notifier.application.useCases.updateRecruiterActiveStatus.UpdateRecruiterActiveStatusUseCase
import org.example.notifier.infrastructure.controller.AdminPositionsController
import org.example.notifier.infrastructure.controller.AdminRecruitersController
import org.example.notifier.infrastructure.controller.AdminTicketsController
import org.example.notifier.infrastructure.controller.AdminUsersController
import org.example.notifier.infrastructure.logging.LoggerPort
import org.example.notifier.infrastructure.security.SecurityUtils
import org.example.notifier.infrastructure.util.factory.ResponseEntityFactory
import org.example.notifier.security.AuthMocks.withRecruiter
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(
    controllers = [
        AdminPositionsController::class,
        AdminRecruitersController::class,
        AdminUsersController::class,
        AdminTicketsController::class
    ],
    excludeAutoConfiguration = [ReactiveSecurityAutoConfiguration::class]
)
@Import(TestSecurityConfig::class, ResponseEntityFactory::class)
@DisplayName("Admin endpoints — RECRUITER must get 403")
class AdminEndpointsAuthorizationTest : BaseAuthorizationTest() {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    // AdminPositionsController deps
    @MockBean private lateinit var createPositionUseCase: CreatePositionUseCase
    @MockBean private lateinit var getAllPositionsUseCase: GetAllPositionsUseCase
    @MockBean private lateinit var getPositionByIdUseCase: GetPositionByIdUseCase
    @MockBean private lateinit var updatePositionUseCase: UpdatePositionUseCase
    @MockBean private lateinit var updatePositionActiveStatusUseCase: UpdatePositionActiveStatusUseCase
    @MockBean private lateinit var getAvailableAssessmentsUseCase: GetAvailableAssessmentsUseCase

    // AdminRecruitersController deps
    @MockBean private lateinit var inviteUserUseCase: InviteUserUseCase
    @MockBean private lateinit var getRecruitersUseCase: GetRecruitersUseCase
    @MockBean private lateinit var getRecruiterDetailUseCase: GetRecruiterDetailUseCase
    @MockBean private lateinit var getRecruiterPositionsUseCase: GetRecruiterPositionsUseCase
    @MockBean private lateinit var updateRecruiterActiveStatusUseCase: UpdateRecruiterActiveStatusUseCase
    @MockBean private lateinit var syncRecruiterPositionsUseCase: SyncRecruiterPositionsUseCase

    // AdminUsersController deps
    @MockBean private lateinit var getAllAdminsUseCase: GetAllAdminsUseCase
    @MockBean private lateinit var updateAdminActiveStatusUseCase: UpdateAdminActiveStatusUseCase
    @MockBean private lateinit var getPendingInvitationsUseCase: GetPendingInvitationsUseCase

    // AdminTicketsController deps
    @MockBean private lateinit var createAsanaTicketUseCase: CreateAsanaTicketUseCase

    // Shared deps
    @MockBean private lateinit var securityUtils: SecurityUtils
    @MockBean private lateinit var logger: LoggerPort

    @Test
    fun `RECRUITER cannot access admin positions`() {
        webTestClient.withRecruiter()
            .get().uri("/admin/positions")
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun `RECRUITER cannot access admin recruiters`() {
        webTestClient.withRecruiter()
            .get().uri("/admin/recruiters")
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun `RECRUITER cannot access admin users`() {
        webTestClient.withRecruiter()
            .get().uri("/admin/admins")
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun `RECRUITER cannot create ticket`() {
        webTestClient.withRecruiter()
            .post().uri("/admin/ticket")
            .header("Content-Type", "application/json")
            .bodyValue("""{"readyDate":"2026-04-01","description":"test"}""")
            .exchange()
            .expectStatus().isForbidden
    }
}
