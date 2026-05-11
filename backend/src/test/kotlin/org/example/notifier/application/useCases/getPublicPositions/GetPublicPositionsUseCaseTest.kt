package org.example.notifier.application.useCases.getPublicPositions

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.service.core.OpenPositionService
import org.example.notifier.domain.position.OpenPosition
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import java.time.LocalDateTime

class GetPublicPositionsUseCaseTest {

    private lateinit var openPositionService: OpenPositionService
    private lateinit var useCase: GetPublicPositionsUseCase

    private val now = LocalDateTime.now()

    @BeforeEach
    fun setup() {
        openPositionService = mock(OpenPositionService::class.java)
        useCase = GetPublicPositionsUseCase(openPositionService)
    }

    @Test
    fun `execute returns only external active positions`() = runBlocking<Unit> {
        val external = buildPosition(id = "p-1", external = true)
        val internal = buildPosition(id = "p-2", external = false)
        whenever(openPositionService.getActivePositions()).thenReturn(listOf(external, internal))

        val result = useCase.execute()

        assertEquals(1, result.size)
        assertEquals("p-1", result[0].id)
    }

    @Test
    fun `execute returns empty list when no external positions exist`() = runBlocking<Unit> {
        val internal = buildPosition(id = "p-1", external = false)
        whenever(openPositionService.getActivePositions()).thenReturn(listOf(internal))

        val result = useCase.execute()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `execute returns empty list when no active positions exist`() = runBlocking<Unit> {
        whenever(openPositionService.getActivePositions()).thenReturn(emptyList())

        val result = useCase.execute()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `execute sorts positions by createdAt descending`() = runBlocking<Unit> {
        val older = buildPosition(id = "p-old", createdAt = now.minusDays(2))
        val newer = buildPosition(id = "p-new", createdAt = now)
        whenever(openPositionService.getActivePositions()).thenReturn(listOf(older, newer))

        val result = useCase.execute()

        assertEquals("p-new", result[0].id)
        assertEquals("p-old", result[1].id)
    }

    @Test
    fun `execute maps position fields to PublicPositionItem`() = runBlocking<Unit> {
        val position = buildPosition(
            id = "p-1",
            title = "Backend Engineer",
            description = "Kotlin job",
            fileUrl = "https://s3.example.com/jd.pdf"
        )
        whenever(openPositionService.getActivePositions()).thenReturn(listOf(position))

        val result = useCase.execute()

        with(result[0]) {
            assertEquals("p-1", id)
            assertEquals("Backend Engineer", title)
            assertEquals("Kotlin job", description)
            assertEquals("https://s3.example.com/jd.pdf", fileUrl)
            assertEquals(now, createdAt)
        }
    }

    @Test
    fun `execute maps null fileUrl correctly`() = runBlocking<Unit> {
        val position = buildPosition(id = "p-1", fileUrl = null)
        whenever(openPositionService.getActivePositions()).thenReturn(listOf(position))

        val result = useCase.execute()

        assertEquals(null, result[0].fileUrl)
    }

    private fun buildPosition(
        id: String = "p-1",
        title: String = "Position",
        description: String = "Description",
        external: Boolean = true,
        fileUrl: String? = null,
        createdAt: LocalDateTime = now
    ) = OpenPosition(
        id = id,
        title = title,
        description = description,
        external = external,
        fileUrl = fileUrl,
        createdBy = "admin@example.com",
        createdAt = createdAt
    )
}