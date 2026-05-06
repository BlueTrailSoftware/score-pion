package org.example.notifier.application.useCases

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.service.core.OpenPositionService
import org.example.notifier.application.service.file.FileService
import org.example.notifier.application.service.integration.AssessmentInfo
import org.example.notifier.application.service.integration.AssessmentPlatformService
import org.example.notifier.application.service.notification.NotificationOrchestrator
import org.example.notifier.application.useCases.createPosition.CreatePositionCommand
import org.example.notifier.application.useCases.createPosition.CreatePositionUseCase
import org.example.notifier.domain.position.OpenPosition
import org.example.notifier.domain.position.OpenPositionAssessment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class CreatePositionUseCaseTest {

    private lateinit var openPositionService: OpenPositionService
    private lateinit var assessmentPlatformService: AssessmentPlatformService
    private lateinit var notificationOrchestrator: NotificationOrchestrator
    private lateinit var fileService: FileService
    private lateinit var useCase: CreatePositionUseCase

    private val position = OpenPosition(
        id = "pos-1",
        title = "Backend Engineer",
        description = "Backend role",
        createdBy = "admin@example.com",
        isActive = true,
        external = false,
        workMode = "Remote",
        location = "Berlin",
        jobType = "Full Time",
        experienceMin = 2,
        experienceMax = 5,
        skills = listOf("Kotlin", "Spring")
    )

    @BeforeEach
    fun setup() {
        openPositionService = mock(OpenPositionService::class.java)
        assessmentPlatformService = mock(AssessmentPlatformService::class.java)
        notificationOrchestrator = mock(NotificationOrchestrator::class.java)
        fileService = mock(FileService::class.java)
        useCase = CreatePositionUseCase(openPositionService, assessmentPlatformService, notificationOrchestrator, fileService)
    }

    @Test
    fun `execute should return PositionResult with created position fields`() = runBlocking<Unit> {
        whenever(assessmentPlatformService.getAvailableAssessments()).thenReturn(emptyList())
        whenever(openPositionService.createPosition(any(), eq(emptyMap()))).thenReturn(position)
        whenever(openPositionService.getPositionAssessments("pos-1")).thenReturn(emptyList())

        val result = useCase.execute(
            CreatePositionCommand(
                title = "Backend Engineer",
                description = "Backend role",
                external = false,
                assessmentIds = emptyList(),
                createdByEmail = "admin@example.com",
                workMode = "Remote",
                location = "Berlin",
                jobType = "Full Time",
                experienceMin = 2,
                experienceMax = 5,
                skills = listOf("Kotlin", "Spring")
            )
        )

        assertEquals("pos-1", result.id)
        assertEquals("Backend Engineer", result.title)
        assertEquals("Backend role", result.description)
        assertEquals(false, result.external)
        assertEquals("admin@example.com", result.createdBy)
        assertEquals("Remote", result.workMode)
        assertEquals("Berlin", result.location)
        assertEquals("Full Time", result.jobType)
        assertEquals(2, result.experienceMin)
        assertEquals(5, result.experienceMax)
        assertEquals(listOf("Kotlin", "Spring"), result.skills)
    }

    @Test
    fun `execute should resolve assessment names from platform service`() = runBlocking<Unit> {
        val availableAssessments = listOf(
            AssessmentInfo(id = "a-1", title = "Java Test"),
            AssessmentInfo(id = "a-2", title = "Kotlin Test")
        )
        val assessments = listOf(
            OpenPositionAssessment(openPositionId = "pos-1", assessmentId = "a-1", assessmentName = "Java Test")
        )
        whenever(assessmentPlatformService.getAvailableAssessments()).thenReturn(availableAssessments)
        whenever(openPositionService.createPosition(any(), eq(mapOf("a-1" to "Java Test")))).thenReturn(position)
        whenever(openPositionService.getPositionAssessments("pos-1")).thenReturn(assessments)

        val result = useCase.execute(
            CreatePositionCommand(
                title = "Backend Engineer",
                description = "Backend role",
                external = false,
                assessmentIds = listOf("a-1"),
                createdByEmail = "admin@example.com",
                workMode = "Remote",
                location = "Berlin",
                jobType = "Full Time",
                experienceMin = 2,
                experienceMax = 5,
                skills = listOf("Kotlin", "Spring")
            )
        )

        assertEquals(1, result.assessments.size)
        assertEquals("a-1", result.assessments[0].assessmentId)
        assertEquals("Java Test", result.assessments[0].assessmentName)
    }

    @Test
    fun `execute should pass assessmentNames map only with matched ids`() = runBlocking<Unit> {
        val availableAssessments = listOf(
            AssessmentInfo(id = "a-1", title = "Java Test"),
            AssessmentInfo(id = "a-2", title = "Kotlin Test")
        )
        whenever(assessmentPlatformService.getAvailableAssessments()).thenReturn(availableAssessments)
        whenever(openPositionService.createPosition(any(), any())).thenReturn(position)
        whenever(openPositionService.getPositionAssessments("pos-1")).thenReturn(emptyList())

        useCase.execute(
            CreatePositionCommand(
                title = "Backend Engineer",
                description = "Backend role",
                external = false,
                assessmentIds = listOf("a-1"),
                createdByEmail = "admin@example.com",
                workMode = "Remote",
                location = "Berlin"
            )
        )

        val namesCaptor = argumentCaptor<Map<String, String>>()
        verify(openPositionService).createPosition(any(), namesCaptor.capture())
        assertEquals(mapOf("a-1" to "Java Test"), namesCaptor.firstValue)
    }

    @Test
    fun `execute should trigger position created notification`() = runBlocking<Unit> {
        whenever(assessmentPlatformService.getAvailableAssessments()).thenReturn(emptyList())
        whenever(openPositionService.createPosition(any(), eq(emptyMap()))).thenReturn(position)
        whenever(openPositionService.getPositionAssessments("pos-1")).thenReturn(emptyList())

        useCase.execute(
            CreatePositionCommand(
                title = "Backend Engineer",
                description = "Backend role",
                external = false,
                assessmentIds = emptyList(),
                createdByEmail = "admin@example.com",
                workMode = "Remote",
                location = "Berlin"
            )
        )

        verify(notificationOrchestrator).notifyPositionCreated(
            createdBy = "admin@example.com",
            position = position,
            assessmentNames = emptyList()
        )
    }
}