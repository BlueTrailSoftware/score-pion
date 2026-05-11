package org.example.notifier.application.useCases

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.service.core.OpenPositionService
import org.example.notifier.application.useCases.updatePositionActiveStatus.UpdatePositionActiveStatusCommand
import org.example.notifier.application.useCases.updatePositionActiveStatus.UpdatePositionActiveStatusUseCase
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

class UpdatePositionActiveStatusUseCaseTest {

    private lateinit var openPositionService: OpenPositionService
    private lateinit var useCase: UpdatePositionActiveStatusUseCase

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
        useCase = UpdatePositionActiveStatusUseCase(openPositionService)
    }

    @Test
    fun `execute should return null when updatePositionActiveStatus returns false`() = runBlocking<Unit> {
        whenever(openPositionService.updatePositionActiveStatus("pos-1", true)).thenReturn(false)

        val result = useCase.execute(UpdatePositionActiveStatusCommand(positionId = "pos-1", isActive = true))

        assertNull(result)
        verify(openPositionService, never()).getPosition("pos-1")
    }

    @Test
    fun `execute should return null when getPosition returns null after update`() = runBlocking<Unit> {
        whenever(openPositionService.updatePositionActiveStatus("pos-1", true)).thenReturn(true)
        whenever(openPositionService.getPosition("pos-1")).thenReturn(null)

        val result = useCase.execute(UpdatePositionActiveStatusCommand(positionId = "pos-1", isActive = true))

        assertNull(result)
    }

    @Test
    fun `execute should return PositionResult when update succeeds`() = runBlocking<Unit> {
        whenever(openPositionService.updatePositionActiveStatus("pos-1", true)).thenReturn(true)
        whenever(openPositionService.getPosition("pos-1")).thenReturn(position)
        whenever(openPositionService.getPositionAssessments("pos-1")).thenReturn(emptyList())

        val result = useCase.execute(UpdatePositionActiveStatusCommand(positionId = "pos-1", isActive = true))

        assertNotNull(result)
        assertEquals("pos-1", result!!.id)
        assertEquals("Backend Engineer", result.title)
        assertEquals(true, result.isActive)
    }

    @Test
    fun `execute should map assessments to PositionResult`() = runBlocking<Unit> {
        val assessments = listOf(
            OpenPositionAssessment(openPositionId = "pos-1", assessmentId = "a-1", assessmentName = "Java Test")
        )
        whenever(openPositionService.updatePositionActiveStatus("pos-1", false)).thenReturn(true)
        whenever(openPositionService.getPosition("pos-1")).thenReturn(position.copy(isActive = false))
        whenever(openPositionService.getPositionAssessments("pos-1")).thenReturn(assessments)

        val result = useCase.execute(UpdatePositionActiveStatusCommand(positionId = "pos-1", isActive = false))

        assertEquals(1, result!!.assessments.size)
        assertEquals("a-1", result.assessments[0].assessmentId)
        assertEquals("Java Test", result.assessments[0].assessmentName)
    }
}
