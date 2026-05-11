package org.example.notifier.security

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.useCases.getApplicantById.GetApplicantByIdUseCase
import org.example.notifier.application.useCases.getApplicants.GetApplicantsUseCase
import org.example.notifier.application.useCases.getPositionById.GetPositionByIdUseCase
import org.example.notifier.application.useCases.getPublicPositions.GetPublicPositionsUseCase
import org.example.notifier.application.useCases.submitApplication.SubmitApplicationUseCase
import org.example.notifier.application.useCases.updateApplicant.UpdateApplicantUseCase
import org.example.notifier.application.useCases.updateApplicantStatus.UpdateApplicantStatusUseCase
import org.example.notifier.infrastructure.controller.ApplicantsController
import org.example.notifier.infrastructure.logging.LoggerPort
import org.example.notifier.infrastructure.security.SecurityUtils
import org.example.notifier.infrastructure.util.factory.ResponseEntityFactory
import org.example.notifier.security.AuthMocks.withAdmin
import org.example.notifier.security.AuthMocks.withRecruiter
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.example.notifier.application.model.shared.PagedResult
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters

@WebFluxTest(
    controllers = [ApplicantsController::class],
    excludeAutoConfiguration = [ReactiveSecurityAutoConfiguration::class]
)
@Import(TestSecurityConfig::class, ResponseEntityFactory::class)
class ApplicantsAuthorizationTest : BaseAuthorizationTest() {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockBean private lateinit var submitApplicationUseCase: SubmitApplicationUseCase
    @MockBean private lateinit var getPublicPositionsUseCase: GetPublicPositionsUseCase
    @MockBean private lateinit var getPositionByIdUseCase: GetPositionByIdUseCase
    @MockBean private lateinit var getApplicantsUseCase: GetApplicantsUseCase
    @MockBean private lateinit var getApplicantByIdUseCase: GetApplicantByIdUseCase
    @MockBean private lateinit var updateApplicantStatusUseCase: UpdateApplicantStatusUseCase
    @MockBean private lateinit var updateApplicantUseCase: UpdateApplicantUseCase
    @MockBean private lateinit var securityUtils: SecurityUtils
    @MockBean private lateinit var logger: LoggerPort

    @Nested
    @DisplayName("ADMIN-only endpoints — RECRUITER must get 403")
    inner class AdminOnlyEndpoints {

        @Test
        fun `RECRUITER cannot get applicant by ID`() {
            webTestClient.withRecruiter()
                .get().uri("/applicants/any-id")
                .exchange()
                .expectStatus().isForbidden
        }

        @Test
        fun `RECRUITER cannot update applicant status`() {
            webTestClient.withRecruiter()
                .patch().uri("/applicants/any-id/status")
                .header("Content-Type", "application/json")
                .bodyValue("""{"status":"REJECTED"}""")
                .exchange()
                .expectStatus().isForbidden
        }

        @Test
        fun `RECRUITER cannot update applicant`() {
            val builder = MultipartBodyBuilder()
            builder.part("data", """{"name":"test"}""")

            webTestClient.withRecruiter()
                .put().uri("/applicants/any-id")
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().isForbidden
        }
    }

    @Nested
    @DisplayName("Protected endpoints — unauthenticated must get 401")
    inner class UnauthenticatedAccess {

        @Test
        fun `unauthenticated cannot list applicants`() {
            webTestClient
                .get().uri("/applicants")
                .exchange()
                .expectStatus().isUnauthorized
        }

        @Test
        fun `unauthenticated cannot get applicant by ID`() {
            webTestClient
                .get().uri("/applicants/any-id")
                .exchange()
                .expectStatus().isUnauthorized
        }
    }

    @Nested
    @DisplayName("Authorized access — ADMIN can reach protected endpoints")
    inner class AdminAccess {

        @Test
        fun `ADMIN can list applicants`() = runBlocking {
            whenever(securityUtils.getCurrentUser()).thenReturn(AuthMocks.adminUser)
            whenever(getApplicantsUseCase.execute(any())).thenReturn(PagedResult(emptyList(), 0))

            webTestClient.withAdmin()
                .get().uri("/applicants")
                .exchange()
                .expectStatus().isOk
        }
    }
}
