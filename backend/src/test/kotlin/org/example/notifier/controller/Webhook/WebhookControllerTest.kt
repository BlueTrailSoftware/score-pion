package org.example.notifier.controller.Webhook

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.useCases.processAssessmentCompleted.ProcessAssessmentCompletedCommand
import org.example.notifier.application.useCases.processAssessmentCompleted.ProcessAssessmentCompletedUseCase
import org.example.notifier.application.useCases.processAssessmentJoined.ProcessAssessmentJoinedCommand
import org.example.notifier.application.useCases.processAssessmentJoined.ProcessAssessmentJoinedUseCase
import org.example.notifier.infrastructure.controller.CoderbyteWebhookController
import org.example.notifier.infrastructure.external.coderbyte.CoderbyteWebhook
import org.example.notifier.infrastructure.logging.LoggerPort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.*
import org.springframework.http.HttpStatus

class WebhookControllerTest {

    private lateinit var processAssessmentJoinedUseCase: ProcessAssessmentJoinedUseCase
    private lateinit var processAssessmentCompletedUseCase: ProcessAssessmentCompletedUseCase
    private lateinit var logger: LoggerPort
    private lateinit var controller: CoderbyteWebhookController

    @BeforeEach
    fun setUp() {
        processAssessmentJoinedUseCase = mock(ProcessAssessmentJoinedUseCase::class.java)
        processAssessmentCompletedUseCase = mock(ProcessAssessmentCompletedUseCase::class.java)
        logger = mock(LoggerPort::class.java)
        controller = CoderbyteWebhookController(processAssessmentJoinedUseCase, processAssessmentCompletedUseCase, logger)
    }

    private fun buildPayload() = CoderbyteWebhook(
        operation = "assessment_completed",
        organizationId = "org-1",
        email = "candidate@test.com",
        reportUrl = "https://coderbyte.com/report/123",
        assessmentId = "assessment-1",
        reportReady = true,
        timeExpired = false
    )

    @Test
    fun `onAssessmentJoined returns 200 on success`() = runBlocking<Unit> {
        val response = controller.onAssessmentJoined(buildPayload())
        assertEquals(HttpStatus.OK, response.statusCode)
        verify(processAssessmentJoinedUseCase).execute(argThat { command ->
            command.candidateEmail == "candidate@test.com" && command.assessmentId == "assessment-1"
        })
    }

    @Test
    fun `onAssessmentJoined returns 500 on exception`() = runBlocking<Unit> {
        whenever(processAssessmentJoinedUseCase.execute(any())).thenThrow(RuntimeException("Use case error"))

        val response = controller.onAssessmentJoined(buildPayload())
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
    }

    @Test
    fun `onAssessmentCompleted returns 200 on success`() = runBlocking<Unit> {
        val response = controller.onAssessmentCompleted(buildPayload())
        assertEquals(HttpStatus.OK, response.statusCode)
        verify(processAssessmentCompletedUseCase).execute(argThat { command ->
            command.candidateEmail == "candidate@test.com" && command.assessmentId == "assessment-1" &&
            command.isReportReady && !command.wasTimeExpired
        })
    }

    @Test
    fun `onAssessmentCompleted returns 500 on exception`() = runBlocking<Unit> {
        whenever(processAssessmentCompletedUseCase.execute(any())).thenThrow(RuntimeException("Use case error"))

        val response = controller.onAssessmentCompleted(buildPayload())
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
    }

    @Test
    fun `legacy onCoderbyteEvent returns 200 on success`() = runBlocking<Unit> {
        val response = controller.onCoderbyteEvent(buildPayload())
        assertEquals(HttpStatus.OK, response.statusCode)
        verify(processAssessmentCompletedUseCase).execute(argThat { command ->
            command.candidateEmail == "candidate@test.com" && command.assessmentId == "assessment-1"
        })
    }

    @Test
    fun `legacy onCoderbyteEvent returns 500 on exception`() = runBlocking<Unit> {
        whenever(processAssessmentCompletedUseCase.execute(any())).thenThrow(RuntimeException("Use case error"))

        val response = controller.onCoderbyteEvent(buildPayload())
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
    }
}
