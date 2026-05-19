package org.example.notifier.application.useCases

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.service.core.OpenPositionService
import org.example.notifier.application.service.file.FileService
import org.example.notifier.application.service.integration.AssessmentInfo
import org.example.notifier.application.service.integration.AssessmentPlatformService
import org.example.notifier.application.useCases.updatePosition.UpdatePositionCommand
import org.example.notifier.application.useCases.updatePosition.UpdatePositionUseCase
import org.example.notifier.domain.position.OpenPosition
import org.example.notifier.domain.position.OpenPositionAssessment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class UpdatePositionUseCaseTest {

    private lateinit var openPositionService: OpenPositionService
    private lateinit var assessmentPlatformService: AssessmentPlatformService
    private lateinit var fileService: FileService
    private lateinit var useCase: UpdatePositionUseCase

    private val existingPosition = OpenPosition(
        id = "pos-1",
        title = "Old Title",
        description = "Old description",
        createdBy = "admin@example.com",
        isActive = true,
        external = false,
        fileUrl = null
    )

    private val updatedPosition = OpenPosition(
        id = "pos-1",
        title = "Updated Title",
        description = "Updated description",
        createdBy = "admin@example.com",
        isActive = true,
        external = true,
        fileUrl = null
    )

    @BeforeEach
    fun setup() {
        openPositionService = mock(OpenPositionService::class.java)
        assessmentPlatformService = mock(AssessmentPlatformService::class.java)
        fileService = mock(FileService::class.java)
        useCase = UpdatePositionUseCase(openPositionService, assessmentPlatformService, fileService)
        runBlocking { whenever(openPositionService.getAllPositions()).thenReturn(listOf(existingPosition)) }
    }

    @Test
    fun `execute should return PositionResult with updated position fields`() = runBlocking<Unit> {
        whenever(openPositionService.getPosition("pos-1")).thenReturn(existingPosition)
        whenever(assessmentPlatformService.getAvailableAssessments()).thenReturn(emptyList())
        whenever(
            fileService.handleFileUpdate(
                currentFileUrl = null,
                newFilePart = null,
                deleteFile = false,
                entityType = "positions",
                entityId = "pos-1"
            )
        ).thenReturn(FileService.FileUpdateResult(null, false))
        whenever(
            openPositionService.updatePosition(
                id = "pos-1",
                title = "Updated Title",
                description = "Updated description",
                external = true,
                assessmentIds = emptyList(),
                assessmentNames = emptyMap(),
                fileUrl = null,
                isFileDeleted = false
            )
        ).thenReturn(updatedPosition)
        whenever(openPositionService.getPositionAssessments("pos-1")).thenReturn(emptyList())

        val result = useCase.execute(
            UpdatePositionCommand(
                positionId = "pos-1",
                title = "Updated Title",
                description = "Updated description",
                external = true,
                assessmentIds = emptyList()
            )
        )

        assertEquals("pos-1", result.id)
        assertEquals("Updated Title", result.title)
        assertEquals("Updated description", result.description)
        assertEquals(true, result.external)
    }

    @Test
    fun `execute should throw when position not found`() = runBlocking<Unit> {
        whenever(openPositionService.getPosition("missing")).thenReturn(null)

        assertThrows<IllegalArgumentException> {
            useCase.execute(
                UpdatePositionCommand(
                    positionId = "missing",
                    title = "T",
                    description = "D",
                    external = false,
                    assessmentIds = emptyList()
                )
            )
        }
    }

    @Test
    fun `execute should resolve assessment names from platform service`() = runBlocking<Unit> {
        val availableAssessments = listOf(
            AssessmentInfo(id = "a-1", title = "Java Test"),
            AssessmentInfo(id = "a-2", title = "Kotlin Test")
        )
        val assessments = listOf(
            OpenPositionAssessment(openPositionId = "pos-1", assessmentId = "a-1", assessmentName = "Java Test"),
            OpenPositionAssessment(openPositionId = "pos-1", assessmentId = "a-2", assessmentName = "Kotlin Test")
        )
        whenever(openPositionService.getPosition("pos-1")).thenReturn(existingPosition)
        whenever(assessmentPlatformService.getAvailableAssessments()).thenReturn(availableAssessments)
        whenever(
            fileService.handleFileUpdate(
                currentFileUrl = null,
                newFilePart = null,
                deleteFile = false,
                entityType = "positions",
                entityId = "pos-1"
            )
        ).thenReturn(FileService.FileUpdateResult(null, false))
        whenever(
            openPositionService.updatePosition(
                id = "pos-1",
                title = "Updated Title",
                description = "Updated description",
                external = true,
                assessmentIds = listOf("a-1", "a-2"),
                assessmentNames = mapOf("a-1" to "Java Test", "a-2" to "Kotlin Test"),
                fileUrl = null,
                isFileDeleted = false
            )
        ).thenReturn(updatedPosition)
        whenever(openPositionService.getPositionAssessments("pos-1")).thenReturn(assessments)

        val result = useCase.execute(
            UpdatePositionCommand(
                positionId = "pos-1",
                title = "Updated Title",
                description = "Updated description",
                external = true,
                assessmentIds = listOf("a-1", "a-2")
            )
        )

        assertEquals(2, result.assessments.size)
        assertEquals("a-1", result.assessments[0].assessmentId)
        assertEquals("Java Test", result.assessments[0].assessmentName)
    }

    @Test
    fun `execute should pass only matched assessmentNames to service`() = runBlocking<Unit> {
        val availableAssessments = listOf(
            AssessmentInfo(id = "a-1", title = "Java Test"),
            AssessmentInfo(id = "a-2", title = "Kotlin Test")
        )
        whenever(openPositionService.getPosition("pos-1")).thenReturn(existingPosition)
        whenever(assessmentPlatformService.getAvailableAssessments()).thenReturn(availableAssessments)
        whenever(
            fileService.handleFileUpdate(
                currentFileUrl = null,
                newFilePart = null,
                deleteFile = false,
                entityType = "positions",
                entityId = "pos-1"
            )
        ).thenReturn(FileService.FileUpdateResult(null, false))
        whenever(
            openPositionService.updatePosition(
                id = "pos-1",
                title = "Updated Title",
                description = "Updated description",
                external = true,
                assessmentIds = listOf("a-1"),
                assessmentNames = mapOf("a-1" to "Java Test"),
                fileUrl = null,
                isFileDeleted = false
            )
        ).thenReturn(updatedPosition)
        whenever(openPositionService.getPositionAssessments("pos-1")).thenReturn(emptyList())

        useCase.execute(
            UpdatePositionCommand(
                positionId = "pos-1",
                title = "Updated Title",
                description = "Updated description",
                external = true,
                assessmentIds = listOf("a-1")
            )
        )

        verify(openPositionService).updatePosition(
            id = "pos-1",
            title = "Updated Title",
            description = "Updated description",
            external = true,
            assessmentIds = listOf("a-1"),
            assessmentNames = mapOf("a-1" to "Java Test"),
            fileUrl = null,
            isFileDeleted = false
        )
    }

    @Test
    fun `execute should use Unknown Assessment when assessment id not found in platform`() = runBlocking<Unit> {
        whenever(openPositionService.getPosition("pos-1")).thenReturn(existingPosition)
        whenever(assessmentPlatformService.getAvailableAssessments()).thenReturn(emptyList())
        whenever(
            fileService.handleFileUpdate(
                currentFileUrl = null,
                newFilePart = null,
                deleteFile = false,
                entityType = "positions",
                entityId = "pos-1"
            )
        ).thenReturn(FileService.FileUpdateResult(null, false))
        whenever(
            openPositionService.updatePosition(
                id = "pos-1",
                title = "Updated Title",
                description = "Updated description",
                external = false,
                assessmentIds = listOf("unknown-id"),
                assessmentNames = emptyMap(),
                fileUrl = null,
                isFileDeleted = false
            )
        ).thenReturn(updatedPosition)
        whenever(openPositionService.getPositionAssessments("pos-1")).thenReturn(emptyList())

        useCase.execute(
            UpdatePositionCommand(
                positionId = "pos-1",
                title = "Updated Title",
                description = "Updated description",
                external = false,
                assessmentIds = listOf("unknown-id")
            )
        )

        verify(openPositionService).updatePosition(
            id = "pos-1",
            title = "Updated Title",
            description = "Updated description",
            external = false,
            assessmentIds = listOf("unknown-id"),
            assessmentNames = emptyMap(),
            fileUrl = null,
            isFileDeleted = false
        )
    }

    @Test
    fun `execute should pass resolved fileUrl from fileService to updatePosition`() = runBlocking<Unit> {
        val positionWithFile = existingPosition.copy(fileUrl = "https://s3/positions/pos-1/old.pdf")
        whenever(openPositionService.getPosition("pos-1")).thenReturn(positionWithFile)
        whenever(assessmentPlatformService.getAvailableAssessments()).thenReturn(emptyList())
        whenever(
            fileService.handleFileUpdate(
                currentFileUrl = "https://s3/positions/pos-1/old.pdf",
                newFilePart = null,
                deleteFile = true,
                entityType = "positions",
                entityId = "pos-1"
            )
        ).thenReturn(FileService.FileUpdateResult(null, true))
        whenever(
            openPositionService.updatePosition(
                id = "pos-1",
                title = "Updated Title",
                description = "Updated description",
                external = false,
                assessmentIds = emptyList(),
                assessmentNames = emptyMap(),
                fileUrl = null,
                isFileDeleted = true
            )
        ).thenReturn(updatedPosition.copy(fileUrl = null, isFileDeleted = true))
        whenever(openPositionService.getPositionAssessments("pos-1")).thenReturn(emptyList())

        val result = useCase.execute(
            UpdatePositionCommand(
                positionId = "pos-1",
                title = "Updated Title",
                description = "Updated description",
                external = false,
                assessmentIds = emptyList(),
                deleteFile = true
            )
        )

        verify(fileService).handleFileUpdate(
            currentFileUrl = "https://s3/positions/pos-1/old.pdf",
            newFilePart = null,
            deleteFile = true,
            entityType = "positions",
            entityId = "pos-1"
        )
        verify(openPositionService).updatePosition(
            id = "pos-1",
            title = "Updated Title",
            description = "Updated description",
            external = false,
            assessmentIds = emptyList(),
            assessmentNames = emptyMap(),
            fileUrl = null,
            isFileDeleted = true
        )
        assertEquals(true, result.isFileDeleted)
    }

    @Test
    fun `execute should reject update when another position has the same title`() = runBlocking<Unit> {
        val conflicting = OpenPosition(
            id = "pos-2",
            title = "updated title",
            description = "Other role",
            createdBy = "admin@example.com"
        )
        whenever(openPositionService.getPosition("pos-1")).thenReturn(existingPosition)
        whenever(openPositionService.getAllPositions()).thenReturn(listOf(existingPosition, conflicting))

        assertThrows<IllegalArgumentException> {
            useCase.execute(
                UpdatePositionCommand(
                    positionId = "pos-1",
                    title = "Updated Title",
                    description = "New desc",
                    external = false,
                    assessmentIds = emptyList()
                )
            )
        }
    }
}
