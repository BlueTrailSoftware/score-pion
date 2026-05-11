package org.example.notifier.security

import org.example.notifier.application.useCases.getCandidates.GetCandidatesUseCase
import org.example.notifier.application.useCases.inviteCandidate.InviteCandidateUseCase
import org.example.notifier.infrastructure.controller.CandidatesController
import org.example.notifier.infrastructure.logging.LoggerPort
import org.example.notifier.infrastructure.security.SecurityUtils
import org.example.notifier.infrastructure.util.factory.ResponseEntityFactory
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(
    controllers = [CandidatesController::class],
    excludeAutoConfiguration = [ReactiveSecurityAutoConfiguration::class]
)
@Import(TestSecurityConfig::class, ResponseEntityFactory::class)
@DisplayName("Candidates — unauthenticated must get 401")
class CandidatesAuthorizationTest : BaseAuthorizationTest() {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockBean private lateinit var getCandidatesUseCase: GetCandidatesUseCase
    @MockBean private lateinit var inviteCandidateUseCase: InviteCandidateUseCase
    @MockBean private lateinit var securityUtils: SecurityUtils
    @MockBean private lateinit var logger: LoggerPort

    @Test
    fun `unauthenticated cannot list candidates`() {
        webTestClient
            .get().uri("/candidates")
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `unauthenticated cannot invite candidates`() {
        webTestClient
            .post().uri("/candidates/invite")
            .header("Content-Type", "application/json")
            .bodyValue("""{"email":"test@test.com","candidateName":"Test","positionId":"pos-1"}""")
            .exchange()
            .expectStatus().isUnauthorized
    }
}
