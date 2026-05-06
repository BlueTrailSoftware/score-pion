package org.example.notifier.application.useCases

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.service.core.OpenPositionService
import org.example.notifier.application.useCases.getRecruiterPositions.GetRecruiterPositionsCommand
import org.example.notifier.application.useCases.getRecruiterPositions.GetRecruiterPositionsUseCase
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

class GetRecruiterPositionsUseCaseTest {

    private lateinit var openPositionService: OpenPositionService
    private lateinit var useCase: GetRecruiterPositionsUseCase

    private val position = OpenPosition(
        id = "pos-1",
        title = "Dev",
        description = "desc",
        createdBy = "admin-1",
        isActive = true
    )
    private val assessment = OpenPositionAssessment(
        id = "a-1",
        openPositionId = "pos-1",
        assessmentId = "test-1",
        assessmentName = "Java Test"
    )

    @BeforeEach
    fun setup() {
        openPositionService = mock(OpenPositionService::class.java)
        useCase = GetRecruiterPositionsUseCase(openPositionService)
    }

    @Test
    fun `execute should return recruiter positions with assessment counts`() = runBlocking<Unit> {
        whenever(openPositionService.getRecruiterPositions("rec-1")).thenReturn(listOf(position))
        whenever(openPositionService.getPositionAssessments("pos-1")).thenReturn(listOf(assessment))

        val result = useCase.execute(GetRecruiterPositionsCommand("rec-1"))

        assertEquals(1, result.items.size)
        assertEquals(1, result.total)
        assertEquals("pos-1", result.items[0].id)
        assertEquals(1, result.items[0].assessmentsCount)
    }

    @Test
    fun `execute should return empty list when recruiter has no positions`() = runBlocking<Unit> {
        whenever(openPositionService.getRecruiterPositions("rec-1")).thenReturn(emptyList())

        val result = useCase.execute(GetRecruiterPositionsCommand("rec-1"))

        assertTrue(result.items.isEmpty())
        assertEquals(0, result.total)
        verify(openPositionService, never()).getPositionAssessments("pos-1")
    }

    @Test
    fun `execute should return zero assessmentsCount when position has no assessments`() = runBlocking<Unit> {
        whenever(openPositionService.getRecruiterPositions("rec-1")).thenReturn(listOf(position))
        whenever(openPositionService.getPositionAssessments("pos-1")).thenReturn(emptyList())

        val result = useCase.execute(GetRecruiterPositionsCommand("rec-1"))

        assertEquals(0, result.items[0].assessmentsCount)
    }
}
