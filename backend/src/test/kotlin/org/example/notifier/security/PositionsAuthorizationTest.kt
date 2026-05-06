package org.example.notifier.security

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.model.position.PositionResult
import org.example.notifier.application.model.shared.PagedResult
import org.example.notifier.application.useCases.getAllPositions.GetAllPositionsUseCase
import org.example.notifier.application.useCases.getPositionById.GetPositionByIdUseCase
import org.example.notifier.application.useCases.getRecruiterPositions.GetRecruiterPositionsUseCase
import org.example.notifier.infrastructure.controller.PositionsController
import org.example.notifier.infrastructure.logging.LoggerPort
import org.example.notifier.infrastructure.security.SecurityUtils
import org.example.notifier.infrastructure.util.factory.ResponseEntityFactory
import org.example.notifier.security.AuthMocks.withAdmin
import org.example.notifier.security.AuthMocks.withRecruiter
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.LocalDateTime

@WebFluxTest(
    controllers = [PositionsController::class],
    excludeAutoConfiguration = [ReactiveSecurityAutoConfiguration::class]
)
@Import(TestSecurityConfig::class, ResponseEntityFactory::class)
class PositionsAuthorizationTest : BaseAuthorizationTest() {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockBean private lateinit var getAllPositionsUseCase: GetAllPositionsUseCase
    @MockBean private lateinit var getRecruiterPositionsUseCase: GetRecruiterPositionsUseCase
    @MockBean private lateinit var getPositionByIdUseCase: GetPositionByIdUseCase
    @MockBean private lateinit var securityUtils: SecurityUtils
    @MockBean private lateinit var logger: LoggerPort

    private val now = LocalDateTime.of(2026, 1, 1, 0, 0)

    @Nested
    @DisplayName("Unauthenticated — must get 401")
    inner class UnauthenticatedAccess {

        @Test
        fun `unauthenticated cannot list positions`() {
            webTestClient
                .get().uri("/positions")
                .exchange()
                .expectStatus().isUnauthorized
        }

        @Test
        fun `unauthenticated cannot get position by ID`() {
            webTestClient
                .get().uri("/positions/any-id")
                .exchange()
                .expectStatus().isUnauthorized
        }
    }

    @Nested
    @DisplayName("RECRUITER — can list own, cannot access unassigned (IDOR)")
    inner class RecruiterAccess {

        @Test
        fun `RECRUITER can list positions`() = runBlocking {
            whenever(securityUtils.getCurrentUser()).thenReturn(AuthMocks.recruiterUser)
            whenever(getRecruiterPositionsUseCase.execute(any())).thenReturn(PagedResult(emptyList(), 0))

            webTestClient.withRecruiter()
                .get().uri("/positions")
                .exchange()
                .expectStatus().isOk
        }

        @Test
        fun `RECRUITER cannot access unassigned position — IDOR protection`() = runBlocking {
            whenever(securityUtils.getCurrentUser()).thenReturn(AuthMocks.recruiterUser)
            whenever(getRecruiterPositionsUseCase.execute(any())).thenReturn(PagedResult(emptyList(), 0))

            webTestClient.withRecruiter()
                .get().uri("/positions/unassigned-pos-id")
                .exchange()
                .expectStatus().isForbidden
        }
    }

    @Nested
    @DisplayName("ADMIN — can access any position")
    inner class AdminAccess {

        @Test
        fun `ADMIN can access any position by ID`() = runBlocking {
            whenever(securityUtils.getCurrentUser()).thenReturn(AuthMocks.adminUser)
            whenever(getPositionByIdUseCase.execute(any())).thenReturn(
                PositionResult(
                    id = "pos-1", title = "Test", description = "desc",
                    external = true, assessments = emptyList(), fileUrl = null,
                    createdBy = "admin", isActive = true,
                    createdAt = now, updatedAt = now,
                    workMode = "Onsite", location = ""
                )
            )

            webTestClient.withAdmin()
                .get().uri("/positions/pos-1")
                .exchange()
                .expectStatus().isOk
        }
    }
}
