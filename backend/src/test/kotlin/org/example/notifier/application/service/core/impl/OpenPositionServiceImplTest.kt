package org.example.notifier.application.service.core.impl

import kotlinx.coroutines.runBlocking
import org.example.notifier.domain.position.OpenPosition
import org.example.notifier.domain.position.OpenPositionAssessment
import org.example.notifier.domain.position.OpenPositionRecruiterAccess
import org.example.notifier.domain.port.OpenPositionAssessmentRepository
import org.example.notifier.domain.port.OpenPositionRecruiterAccessRepository
import org.example.notifier.domain.port.OpenPositionRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

class OpenPositionServiceImplTest {

    private lateinit var positionRepository: OpenPositionRepository
    private lateinit var assessmentRepository: OpenPositionAssessmentRepository
    private lateinit var accessRepository: OpenPositionRecruiterAccessRepository
    private lateinit var service: OpenPositionServiceImpl

    @BeforeEach
    fun setup() {
        positionRepository = mock(OpenPositionRepository::class.java)
        assessmentRepository = mock(OpenPositionAssessmentRepository::class.java)
        accessRepository = mock(OpenPositionRecruiterAccessRepository::class.java)
        service = OpenPositionServiceImpl(
            positionRepository,
            assessmentRepository,
            accessRepository
        )
    }

    // --- getPosition ---

    @Test
    fun `getPosition returns position when found`() = runBlocking<Unit> {
        val position = buildPosition("p-1")
        whenever(positionRepository.findById("p-1")).thenReturn(position)

        val result = service.getPosition("p-1")

        assertEquals(position, result)
    }

    @Test
    fun `getPosition returns null when not found`() = runBlocking<Unit> {
        whenever(positionRepository.findById("unknown")).thenReturn(null)

        val result = service.getPosition("unknown")

        assertNull(result)
    }

    // --- getPositionAssessments ---

    @Test
    fun `getPositionAssessments returns assessments for the given position`() = runBlocking<Unit> {
        val assessments = listOf(
            OpenPositionAssessment(openPositionId = "p-1", assessmentId = "a-1", assessmentName = "Java Test"),
            OpenPositionAssessment(openPositionId = "p-1", assessmentId = "a-2", assessmentName = "SQL Test")
        )
        whenever(assessmentRepository.findByPositionId("p-1")).thenReturn(assessments)

        val result = service.getPositionAssessments("p-1")

        assertEquals(2, result.size)
        assertEquals("a-1", result[0].assessmentId)
        assertEquals("a-2", result[1].assessmentId)
    }

    @Test
    fun `getPositionAssessments returns empty list when position has no assessments`() = runBlocking<Unit> {
        whenever(assessmentRepository.findByPositionId("p-1")).thenReturn(emptyList())

        val result = service.getPositionAssessments("p-1")

        assertTrue(result.isEmpty())
    }

    // --- getRecruiterPositions ---

    @Test
    fun `getRecruiterPositions returns active positions for active accesses`() = runBlocking<Unit> {
        val access = OpenPositionRecruiterAccess(openPositionId = "p-1", recruiterId = "r-1", grantedBy = "admin-1", isActive = true)
        val position = OpenPosition(id = "p-1", title = "Dev", description = "", createdBy = "admin@example.com", isActive = true)

        whenever(accessRepository.findByRecruiterId("r-1")).thenReturn(listOf(access))
        whenever(positionRepository.findById("p-1")).thenReturn(position)

        val result = service.getRecruiterPositions("r-1")

        assertEquals(1, result.size)
        assertEquals("p-1", result[0].id)
    }

    @Test
    fun `getRecruiterPositions excludes inactive accesses`() = runBlocking<Unit> {
        val inactiveAccess = OpenPositionRecruiterAccess(
            openPositionId = "p-1", recruiterId = "r-1", grantedBy = "admin-1", isActive = false
        )

        whenever(accessRepository.findByRecruiterId("r-1")).thenReturn(listOf(inactiveAccess))

        val result = service.getRecruiterPositions("r-1")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getRecruiterPositions excludes inactive positions even when access is active`() = runBlocking<Unit> {
        val access = OpenPositionRecruiterAccess(openPositionId = "p-1", recruiterId = "r-1", grantedBy = "admin-1", isActive = true)
        val inactivePosition = OpenPosition(id = "p-1", title = "Dev", description = "", createdBy = "admin@example.com", isActive = false)

        whenever(accessRepository.findByRecruiterId("r-1")).thenReturn(listOf(access))
        whenever(positionRepository.findById("p-1")).thenReturn(inactivePosition)

        val result = service.getRecruiterPositions("r-1")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getRecruiterPositions returns empty list when recruiter has no accesses`() = runBlocking<Unit> {
        whenever(accessRepository.findByRecruiterId("r-1")).thenReturn(emptyList())

        val result = service.getRecruiterPositions("r-1")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getRecruiterPositions returns only positions with active accesses when mix exists`() = runBlocking<Unit> {
        val activeAccess = OpenPositionRecruiterAccess(openPositionId = "p-1", recruiterId = "r-1", grantedBy = "admin-1", isActive = true)
        val inactiveAccess = OpenPositionRecruiterAccess(openPositionId = "p-2", recruiterId = "r-1", grantedBy = "admin-1", isActive = false)
        val position1 = OpenPosition(id = "p-1", title = "Dev", description = "", createdBy = "admin@example.com", isActive = true)

        whenever(accessRepository.findByRecruiterId("r-1")).thenReturn(listOf(activeAccess, inactiveAccess))
        whenever(positionRepository.findById("p-1")).thenReturn(position1)

        val result = service.getRecruiterPositions("r-1")

        assertEquals(1, result.size)
        assertEquals("p-1", result[0].id)
    }

    private fun buildPosition(id: String) = OpenPosition(
        id = id,
        title = "Senior Developer",
        description = "A great position",
        createdBy = "admin@example.com",
        isActive = true
    )
}