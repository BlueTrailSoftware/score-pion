package org.example.notifier.application.useCases

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.service.core.OpenPositionService
import org.example.notifier.application.useCases.getAllPositions.GetAllPositionsCommand
import org.example.notifier.application.useCases.getAllPositions.GetAllPositionsUseCase
import org.example.notifier.domain.position.OpenPosition
import org.example.notifier.domain.position.OpenPositionAssessment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class GetAllPositionsUseCaseTest {

    private lateinit var openPositionService: OpenPositionService
    private lateinit var useCase: GetAllPositionsUseCase

    private val position = OpenPosition(
        id = "pos-1",
        title = "Backend Engineer",
        description = "Backend role",
        createdBy = "admin@example.com",
        isActive = true,
        external = false
    )

    @BeforeEach
    fun setup() {
        openPositionService = mock(OpenPositionService::class.java)
        useCase = GetAllPositionsUseCase(openPositionService)
    }

    @Test
    fun `execute should call getAllPositions when activeOnly is false`() = runBlocking<Unit> {
        whenever(openPositionService.getAllPositions()).thenReturn(listOf(position))
        whenever(openPositionService.getPositionAssessments("pos-1")).thenReturn(emptyList())

        useCase.execute(GetAllPositionsCommand(activeOnly = false))

        verify(openPositionService).getAllPositions()
        verify(openPositionService, never()).getActivePositions()
    }

    @Test
    fun `execute should call getActivePositions when activeOnly is true`() = runBlocking<Unit> {
        whenever(openPositionService.getActivePositions()).thenReturn(listOf(position))
        whenever(openPositionService.getPositionAssessments("pos-1")).thenReturn(emptyList())

        useCase.execute(GetAllPositionsCommand(activeOnly = true))

        verify(openPositionService).getActivePositions()
        verify(openPositionService, never()).getAllPositions()
    }

    @Test
    fun `execute should map position fields to PositionSummaryItem`() = runBlocking<Unit> {
        whenever(openPositionService.getAllPositions()).thenReturn(listOf(position))
        whenever(openPositionService.getPositionAssessments("pos-1")).thenReturn(emptyList())

        val result = useCase.execute(GetAllPositionsCommand(activeOnly = false))

        assertEquals(1, result.items.size)
        assertEquals(1, result.total)
        with(result.items[0]) {
            assertEquals("pos-1", id)
            assertEquals("Backend Engineer", title)
            assertEquals("Backend role", description)
            assertEquals(false, external)
            assertEquals(true, isActive)
        }
    }

    @Test
    fun `execute should count assessments per position`() = runBlocking<Unit> {
        val assessments = listOf(
            OpenPositionAssessment(openPositionId = "pos-1", assessmentId = "a-1", assessmentName = "Java Test"),
            OpenPositionAssessment(openPositionId = "pos-1", assessmentId = "a-2", assessmentName = "Kotlin Test")
        )
        whenever(openPositionService.getAllPositions()).thenReturn(listOf(position))
        whenever(openPositionService.getPositionAssessments("pos-1")).thenReturn(assessments)

        val result = useCase.execute(GetAllPositionsCommand(activeOnly = false))

        assertEquals(2, result.items[0].assessmentsCount)
    }

    @Test
    fun `execute should return empty list when no positions exist`() = runBlocking<Unit> {
        whenever(openPositionService.getAllPositions()).thenReturn(emptyList())

        val result = useCase.execute(GetAllPositionsCommand(activeOnly = false))

        assertTrue(result.items.isEmpty())
        assertEquals(0, result.total)
    }
}
