package org.example.notifier.infrastructure.dto.mapper

import org.example.notifier.application.model.position.PositionAssessmentItem
import org.example.notifier.application.model.position.PositionResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import kotlin.random.Random

class PositionMappersPropertyTest {

    private val validWorkModes = listOf("Onsite", "Remote", "Hybrid")
    private val validJobTypes = listOf("Full Time", "Part Time", "Contract", "Internship")
    private val charPool = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    private fun randomString(rng: Random, maxLen: Int = 30): String {
        val len = rng.nextInt(1, maxLen + 1)
        return (1..len).map { charPool[rng.nextInt(charPool.size)] }.joinToString("")
    }

    private fun randomSkills(rng: Random): List<String> {
        val count = rng.nextInt(0, 6)
        val skills = mutableSetOf<String>()
        repeat(count) { skills.add(randomString(rng, 15)) }
        return skills.toList()
    }

    private fun randomPositionResult(rng: Random): PositionResult {
        val now = LocalDateTime.now()
        val assessments = (0 until rng.nextInt(0, 4)).map {
            PositionAssessmentItem(
                assessmentId = randomString(rng),
                assessmentName = randomString(rng),
                addedAt = now
            )
        }
        val expMin = if (rng.nextBoolean()) rng.nextInt(0, 11) else null
        val expMax = when {
            expMin != null -> if (rng.nextBoolean()) rng.nextInt(expMin, 21) else null
            else -> if (rng.nextBoolean()) rng.nextInt(0, 21) else null
        }
        return PositionResult(
            id = randomString(rng),
            title = randomString(rng),
            description = randomString(rng),
            external = rng.nextBoolean(),
            assessments = assessments,
            fileUrl = if (rng.nextBoolean()) randomString(rng) else null,
            createdBy = randomString(rng),
            isActive = rng.nextBoolean(),
            createdAt = now,
            updatedAt = now,
            isFileDeleted = rng.nextBoolean(),
            workMode = validWorkModes[rng.nextInt(validWorkModes.size)],
            location = randomString(rng, 200),
            jobType = if (rng.nextBoolean()) validJobTypes[rng.nextInt(validJobTypes.size)] else null,
            experienceMin = expMin,
            experienceMax = expMax,
            skills = randomSkills(rng)
        )
    }

    // Feature: position-extended-fields, Property 8: Response mapping preserves all extended fields
    // Validates: Requirements 4.1
    @Test
    fun `Property 8 - response mapping preserves all extended fields`() {
        repeat(100) {
            val rng = Random(it)
            val result = randomPositionResult(rng)
            val response = result.toResponse()

            assertEquals(result.workMode, response.workMode, "workMode mismatch at seed $it")
            assertEquals(result.location, response.location, "location mismatch at seed $it")
            assertEquals(result.jobType, response.jobType, "jobType mismatch at seed $it")
            assertEquals(result.experienceMin, response.experienceMin, "experienceMin mismatch at seed $it")
            assertEquals(result.experienceMax, response.experienceMax, "experienceMax mismatch at seed $it")
            assertEquals(result.skills, response.skills, "skills mismatch at seed $it")
        }
    }
}
