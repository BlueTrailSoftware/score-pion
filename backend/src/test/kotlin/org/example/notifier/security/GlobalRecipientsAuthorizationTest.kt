package org.example.notifier.security

import org.example.notifier.application.useCases.addGlobalRecipientEmail.AddGlobalRecipientEmailUseCase
import org.example.notifier.application.useCases.getGlobalRecipientEmails.GetGlobalRecipientEmailsUseCase
import org.example.notifier.application.useCases.getGlobalRecipients.GetGlobalRecipientsUseCase
import org.example.notifier.application.useCases.removeGlobalRecipientEmail.RemoveGlobalRecipientEmailUseCase
import org.example.notifier.application.useCases.updateGlobalRecipientEmail.UpdateGlobalRecipientEmailUseCase
import org.example.notifier.infrastructure.controller.GlobalRecipientsController
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
    controllers = [GlobalRecipientsController::class],
    excludeAutoConfiguration = [ReactiveSecurityAutoConfiguration::class]
)
@Import(TestSecurityConfig::class)
@DisplayName("GlobalRecipients — ADMIN-only, class-level @PreAuthorize")
class GlobalRecipientsAuthorizationTest : BaseAuthorizationTest() {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockBean private lateinit var getGlobalRecipientsUseCase: GetGlobalRecipientsUseCase
    @MockBean private lateinit var getGlobalRecipientEmailsUseCase: GetGlobalRecipientEmailsUseCase
    @MockBean private lateinit var addGlobalRecipientEmailUseCase: AddGlobalRecipientEmailUseCase
    @MockBean private lateinit var removeGlobalRecipientEmailUseCase: RemoveGlobalRecipientEmailUseCase
    @MockBean private lateinit var updateGlobalRecipientEmailUseCase: UpdateGlobalRecipientEmailUseCase
    @MockBean private lateinit var securityUtils: SecurityUtils
    @MockBean private lateinit var responseFactory: ResponseEntityFactory
    @MockBean private lateinit var logger: LoggerPort

    @Test
    fun `RECRUITER cannot get recipients`() {
        webTestClient.withRecruiter()
            .get().uri("/settings/global-recipients")
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun `RECRUITER cannot get emails`() {
        webTestClient.withRecruiter()
            .get().uri("/settings/global-recipients/emails")
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun `RECRUITER cannot add email`() {
        webTestClient.withRecruiter()
            .post().uri("/settings/global-recipients/emails")
            .header("Content-Type", "application/json")
            .bodyValue("""{"email":"leak@example.com"}""")
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun `RECRUITER cannot remove email`() {
        webTestClient.withRecruiter()
            .delete().uri("/settings/global-recipients/emails/leak@example.com")
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun `RECRUITER cannot update email`() {
        webTestClient.withRecruiter()
            .put().uri("/settings/global-recipients/emails")
            .header("Content-Type", "application/json")
            .bodyValue("""{"oldEmail":"a@example.com","newEmail":"b@example.com"}""")
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun `unauthenticated cannot get recipients`() {
        webTestClient
            .get().uri("/settings/global-recipients")
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `unauthenticated cannot add email`() {
        webTestClient
            .post().uri("/settings/global-recipients/emails")
            .header("Content-Type", "application/json")
            .bodyValue("""{"email":"leak@example.com"}""")
            .exchange()
            .expectStatus().isUnauthorized
    }
}