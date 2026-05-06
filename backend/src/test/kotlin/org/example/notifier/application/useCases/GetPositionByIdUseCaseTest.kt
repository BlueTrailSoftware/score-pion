package org.example.notifier.application.useCases

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.service.core.OpenPositionService
import org.example.notifier.application.useCases.getPositionById.GetPositionByIdCommand
import org.example.notifier.application.useCases.getPositionById.GetPositionByIdUseCase
import org.example.notifier.domain.position.OpenPosition
import org.example.notifier.domain.position.OpenPositionAssessment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class GetPositionByIdUseCaseTest {

    private lateinit var openPositionService: OpenPositionService
    private lateinit var useCase: GetPositionByIdUseCase

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
        useCase = GetPositionByIdUseCase(openPositionService)
    }

    @Test
    fun `execute should return null when position not found`() = runBlocking<Unit> {
        whenever(openPositionService.getPosition("pos-1")).thenReturn(null)

        val result = useCase.execute(GetPositionByIdCommand(positionId = "pos-1"))

        assertNull(result)
        verify(openPositionService, never()).getPositionAssessments("pos-1")
    }

    @Test
    fun `execute should map position fields to PositionResult`() = runBlocking<Unit> {
        whenever(openPositionService.getPosition("pos-1")).thenReturn(position)
        whenever(openPositionService.getPositionAssessments("pos-1")).thenReturn(emptyList())

        val result = useCase.execute(GetPositionByIdCommand(positionId = "pos-1"))

        assertNotNull(result)
        assertEquals("pos-1", result!!.id)
        assertEquals("Backend Engineer", result.title)
        assertEquals("Backend role", result.description)
        assertEquals(false, result.external)
        assertEquals(true, result.isActive)
        assertEquals("admin@example.com", result.createdBy)
    }

    @Test
    fun `execute should map assessments to PositionAssessmentItem`() = runBlocking<Unit> {
        val assessments = listOf(
            OpenPositionAssessment(openPositionId = "pos-1", assessmentId = "a-1", assessmentName = "Java Test"),
            OpenPositionAssessment(openPositionId = "pos-1", assessmentId = "a-2", assessmentName = "Kotlin Test")
        )
        whenever(openPositionService.getPosition("pos-1")).thenReturn(position)
        whenever(openPositionService.getPositionAssessments("pos-1")).thenReturn(assessments)

        val result = useCase.execute(GetPositionByIdCommand(positionId = "pos-1"))

        assertEquals(2, result!!.assessments.size)
        assertEquals("a-1", result.assessments[0].assessmentId)
        assertEquals("Java Test", result.assessments[0].assessmentName)
        assertEquals("a-2", result.assessments[1].assessmentId)
    }

    @Test
    fun `execute should return empty assessments when position has none`() = runBlocking<Unit> {
        whenever(openPositionService.getPosition("pos-1")).thenReturn(position)
        whenever(openPositionService.getPositionAssessments("pos-1")).thenReturn(emptyList())

        val result = useCase.execute(GetPositionByIdCommand(positionId = "pos-1"))

        assertEquals(0, result!!.assessments.size)
    }
}
