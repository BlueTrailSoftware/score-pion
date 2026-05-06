package org.example.notifier.infrastructure.controller

import org.example.notifier.application.useCases.confirmDataErasure.ConfirmDataErasureCommand
import org.example.notifier.application.useCases.confirmDataErasure.ConfirmDataErasureResult
import org.example.notifier.application.useCases.confirmDataErasure.ConfirmDataErasureUseCase
import org.example.notifier.application.useCases.downloadDataExport.DownloadDataExportCommand
import org.example.notifier.application.useCases.downloadDataExport.DownloadDataExportResult
import org.example.notifier.application.useCases.downloadDataExport.DownloadDataExportUseCase
import org.example.notifier.application.useCases.requestDataErasure.RequestDataErasureCommand
import org.example.notifier.application.useCases.requestDataErasure.RequestDataErasureResult
import org.example.notifier.application.useCases.requestDataErasure.RequestDataErasureUseCase
import org.example.notifier.application.useCases.requestDataExport.RequestDataExportCommand
import org.example.notifier.application.useCases.requestDataExport.RequestDataExportResult
import org.example.notifier.application.useCases.requestDataExport.RequestDataExportUseCase
import org.example.notifier.infrastructure.dto.request.PrivacyRequest
import org.example.notifier.infrastructure.dto.response.ApiResponse
import org.example.notifier.infrastructure.logging.LoggerPort
import org.example.notifier.infrastructure.util.factory.ResponseEntityFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "Privacy (GDPR)", description = "GDPR data erasure and export requests for applicants (no auth required)")
@SecurityRequirements
@RestController
@RequestMapping("/applicants/privacy")
class PrivacyController(
    private val requestDataErasureUseCase: RequestDataErasureUseCase,
    private val confirmDataErasureUseCase: ConfirmDataErasureUseCase,
    private val requestDataExportUseCase: RequestDataExportUseCase,
    private val downloadDataExportUseCase: DownloadDataExportUseCase,
    private val responseFactory: ResponseEntityFactory,
    private val logger: LoggerPort
) {

    @Operation(summary = "Request data erasure (GDPR right to be forgotten)")
    @PostMapping("/erasures")
    suspend fun createErasureRequest(@RequestBody request: PrivacyRequest): ResponseEntity<ApiResponse<Any>> {
        return try {
            when (requestDataErasureUseCase.execute(RequestDataErasureCommand(request.email, request.captchaToken))) {
                is RequestDataErasureResult.NoApplicantFound ->
                    responseFactory.success(
                        "If an account exists, you will receive a verification email.",
                        mapOf("message" to "If an account exists, you will receive a verification email.")
                    )
                is RequestDataErasureResult.VerificationEmailSent ->
                    responseFactory.success(
                        "Verification email sent. Check your inbox.",
                        mapOf("message" to "Verification email sent. Check your inbox.")
                    )
            }
        } catch (e: IllegalArgumentException) {
            responseFactory.badRequest(e.message ?: "Bad request")
        }
    }

    @Operation(summary = "Confirm data erasure via token")
    @PutMapping("/erasures/{token}")
    suspend fun confirmErasure(@PathVariable token: String): ResponseEntity<ApiResponse<Any>> {
        return when (val result = confirmDataErasureUseCase.execute(ConfirmDataErasureCommand(token))) {
            is ConfirmDataErasureResult.InvalidToken ->
                responseFactory.badRequest("Invalid or expired token")
            is ConfirmDataErasureResult.NotFound ->
                responseFactory.badRequest("No data found or already deleted for this email")
            is ConfirmDataErasureResult.AlreadyAnonymized ->
                responseFactory.badRequest("Data has already been anonymized")
            is ConfirmDataErasureResult.Anonymized ->
                responseFactory.success(
                    "Data successfully anonymized. ${result.count} record(s) processed.",
                    mapOf("message" to "Data successfully anonymized. ${result.count} record(s) processed.")
                )
        }
    }

    @Operation(summary = "Request a personal data export (GDPR right to data portability)")
    @PostMapping("/exports")
    suspend fun createExportRequest(@RequestBody request: PrivacyRequest): ResponseEntity<ApiResponse<Any>> {
        return try {
            when (requestDataExportUseCase.execute(RequestDataExportCommand(request.email, request.captchaToken))) {
                is RequestDataExportResult.NoApplicantFound ->
                    responseFactory.success(
                        "If an account exists, you will receive a download link.",
                        mapOf("message" to "If an account exists, you will receive a download link.")
                    )
                is RequestDataExportResult.DownloadEmailSent ->
                    responseFactory.success(
                        "Download link sent. Check your inbox.",
                        mapOf("message" to "Download link sent. Check your inbox.")
                    )
            }
        } catch (e: IllegalArgumentException) {
            responseFactory.badRequest(e.message ?: "Bad request")
        }
    }

    @Operation(summary = "Download exported personal data file via token")
    @GetMapping("/exports/{token}")
    suspend fun downloadExport(@PathVariable token: String): ResponseEntity<ByteArray> {
        return when (val result = downloadDataExportUseCase.execute(DownloadDataExportCommand(token))) {
            is DownloadDataExportResult.InvalidToken ->
                ResponseEntity.status(400)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\": \"Invalid or expired token\"}".toByteArray())
            is DownloadDataExportResult.DataNotAvailable -> {
                logger.warn("Download attempt for deleted/anonymized data via token")
                ResponseEntity.status(410)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\": \"Data has been deleted and is no longer available for download\"}".toByteArray())
            }
            is DownloadDataExportResult.Success ->
                ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${result.filename}\"")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result.data)
        }
    }
}
