package org.example.notifier.application.useCases

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.service.core.OpenPositionService
import org.example.notifier.application.service.core.UserService
import org.example.notifier.application.useCases.getRecruiterDetail.GetRecruiterDetailCommand
import org.example.notifier.application.useCases.getRecruiterDetail.GetRecruiterDetailUseCase
import org.example.notifier.domain.position.OpenPosition
import org.example.notifier.domain.position.OpenPositionAssessment
import org.example.notifier.domain.user.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDateTime

class GetRecruiterDetailUseCaseTest {

    private lateinit var userService: UserService
    private lateinit var openPositionService: OpenPositionService
    private lateinit var useCase: GetRecruiterDetailUseCase

    private val fixedNow = LocalDateTime.of(2026, 3, 20, 10, 0)

    @BeforeEach
    fun setup() {
        userService = mock(UserService::class.java)
        openPositionService = mock(OpenPositionService::class.java)
        useCase = GetRecruiterDetailUseCase(userService, openPositionService)
    }

    @Test
    fun `execute should return null when recruiter does not exist`() = runBlocking<Unit> {
        whenever(userService.findById("unknown-id")).thenReturn(null)

        val result = useCase.execute(GetRecruiterDetailCommand("unknown-id"))

        assertNull(result)
        verify(openPositionService, never()).getRecruiterPositions("unknown-id")
    }

    @Test
    fun `execute should return detail with empty positions when recruiter has no positions`() = runBlocking<Unit> {
        val recruiter = aRecruiter(id = "r-1")
        whenever(userService.findById("r-1")).thenReturn(recruiter)
        whenever(openPositionService.getRecruiterPositions("r-1")).thenReturn(emptyList())

        val result = useCase.execute(GetRecruiterDetailCommand("r-1"))!!

        assertEquals("r-1", result.id)
        assertEquals(recruiter.email, result.email)
        assertEquals(recruiter.name, result.name)
        assertEquals(recruiter.role, result.role)
        assertEquals(recruiter.isActive, result.isActive)
        assertEquals(0, result.positionsCount)
        assertEquals(emptyList<Any>(), result.positions)
    }

    @Test
    fun `execute should return detail with positions and assessments count`() = runBlocking<Unit> {
        val recruiter = aRecruiter(id = "r-1")
        val pos1 = aPosition(id = "pos-1", title = "Backend Engineer")
        val pos2 = aPosition(id = "pos-2", title = "Frontend Engineer")

        whenever(userService.findById("r-1")).thenReturn(recruiter)
        whenever(openPositionService.getRecruiterPositions("r-1")).thenReturn(listOf(pos1, pos2))
        whenever(openPositionService.getPositionAssessments("pos-1")).thenReturn(listOf(anAssessment("pos-1"), anAssessment("pos-1")))
        whenever(openPositionService.getPositionAssessments("pos-2")).thenReturn(listOf(anAssessment("pos-2")))

        val result = useCase.execute(GetRecruiterDetailCommand("r-1"))!!

        assertEquals(2, result.positionsCount)
        assertEquals(2, result.positions.size)

        val firstPosition = result.positions.first { it.id == "pos-1" }
        assertEquals("Backend Engineer", firstPosition.title)
        assertEquals(2, firstPosition.assessmentsCount)

        val secondPosition = result.positions.first { it.id == "pos-2" }
        assertEquals("Frontend Engineer", secondPosition.title)
        assertEquals(1, secondPosition.assessmentsCount)
    }

    @Test
    fun `execute should map all position fields correctly`() = runBlocking<Unit> {
        val recruiter = aRecruiter(id = "r-1")
        val position = OpenPosition(
            id = "pos-1",
            title = "QA Engineer",
            description = "Quality assurance role",
            createdBy = "admin@test.com",
            isActive = false,
            external = true,
            createdAt = fixedNow
        )

        whenever(userService.findById("r-1")).thenReturn(recruiter)
        whenever(openPositionService.getRecruiterPositions("r-1")).thenReturn(listOf(position))
        whenever(openPositionService.getPositionAssessments("pos-1")).thenReturn(emptyList())

        val result = useCase.execute(GetRecruiterDetailCommand("r-1"))!!

        val item = result.positions.single()
        assertEquals("pos-1", item.id)
        assertEquals("QA Engineer", item.title)
        assertEquals("Quality assurance role", item.description)
        assertEquals(true, item.external)
        assertEquals(false, item.isActive)
        assertEquals(0, item.assessmentsCount)
        assertEquals(fixedNow, item.createdAt)
    }

    @Test
    fun `execute should map all recruiter fields correctly`() = runBlocking<Unit> {
        val recruiter = User(
            id = "r-99",
            email = "alice@company.com",
            name = "Alice",
            role = "RECRUITER",
            isActive = true,
            createdAt = fixedNow
        )

        whenever(userService.findById("r-99")).thenReturn(recruiter)
        whenever(openPositionService.getRecruiterPositions("r-99")).thenReturn(emptyList())

        val result = useCase.execute(GetRecruiterDetailCommand("r-99"))!!

        assertEquals("r-99", result.id)
        assertEquals("alice@company.com", result.email)
        assertEquals("Alice", result.name)
        assertEquals("RECRUITER", result.role)
        assertEquals(true, result.isActive)
        assertEquals(fixedNow, result.createdAt)
    }

    // --- helpers ---

    private fun aRecruiter(id: String) = User(
        id = id,
        email = "$id@company.com",
        name = "Recruiter $id",
        role = "RECRUITER",
        isActive = true,
        createdAt = fixedNow
    )

    private fun aPosition(id: String, title: String = "Position $id") = OpenPosition(
        id = id,
        title = title,
        description = "Description for $id",
        createdBy = "admin@test.com",
        createdAt = fixedNow
    )

    private fun anAssessment(positionId: String) = OpenPositionAssessment(
        openPositionId = positionId,
        assessmentId = "assess-1",
        assessmentName = "Coding Test",
        addedAt = fixedNow
    )
}