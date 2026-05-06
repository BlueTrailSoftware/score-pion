package org.example.notifier.application.useCases

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.service.integration.AssessmentInfo
import org.example.notifier.application.service.integration.AssessmentPlatformService
import org.example.notifier.application.useCases.getAvailableAssessments.GetAvailableAssessmentsUseCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

class GetAvailableAssessmentsUseCaseTest {

    private lateinit var assessmentPlatformService: AssessmentPlatformService
    private lateinit var useCase: GetAvailableAssessmentsUseCase

    @BeforeEach
    fun setup() {
        assessmentPlatformService = mock(AssessmentPlatformService::class.java)
        useCase = GetAvailableAssessmentsUseCase(assessmentPlatformService)
    }

    @Test
    fun `execute should map AssessmentInfo to AssessmentItem`() = runBlocking<Unit> {
        whenever(assessmentPlatformService.getAvailableAssessments()).thenReturn(
            listOf(AssessmentInfo(id = "java-101", title = "Java Test"))
        )

        val result = useCase.execute()

        assertEquals(1, result.size)
        assertEquals("Java Test", result[0].displayName)
        assertEquals("java-101", result[0].testId)
    }

    @Test
    fun `execute should return all items from service`() = runBlocking<Unit> {
        val assessments = listOf(
            AssessmentInfo(id = "java-101", title = "Java Test"),
            AssessmentInfo(id = "kotlin-202", title = "Kotlin Test"),
            AssessmentInfo(id = "sql-303", title = "SQL Test")
        )
        whenever(assessmentPlatformService.getAvailableAssessments()).thenReturn(assessments)

        val result = useCase.execute()

        assertEquals(3, result.size)
    }

    @Test
    fun `execute should return empty list when no assessments available`() = runBlocking<Unit> {
        whenever(assessmentPlatformService.getAvailableAssessments()).thenReturn(emptyList())

        val result = useCase.execute()

        assertTrue(result.isEmpty())
    }
}
