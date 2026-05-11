package org.example.notifier.infrastructure.persistence.mapper

import org.example.notifier.domain.position.OpenPosition
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.random.Random

class OpenPositionMapperPropertyTest {

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

    private fun randomOpenPosition(rng: Random): OpenPosition {
        // Truncate to millis — Jackson JavaTimeModule round-trips at millisecond precision
        val now = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
        val expMin = if (rng.nextBoolean()) rng.nextInt(0, 11) else null
        val expMax = when {
            expMin != null -> if (rng.nextBoolean()) rng.nextInt(expMin, 21) else null
            else -> if (rng.nextBoolean()) rng.nextInt(0, 21) else null
        }
        return OpenPosition(
            id = randomString(rng),
            title = randomString(rng),
            description = randomString(rng),
            createdBy = randomString(rng),
            isActive = rng.nextBoolean(),
            external = rng.nextBoolean(),
            fileUrl = if (rng.nextBoolean()) randomString(rng) else null,
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

    // Feature: position-extended-fields, Property 7: OpenPosition serialization round-trip
    // Validates: Requirements 3.2, 3.3
    @Test
    fun `Property 7 - OpenPosition serialization round-trip`() {
        repeat(100) {
            val rng = Random(it)
            val original = randomOpenPosition(rng)

            val entity = OpenPositionMapper.toDynamoEntity(original)
            val restored = OpenPositionMapper.fromDynamoEntity(entity)

            assertEquals(original, restored, "Round-trip failed at seed $it")
        }
    }

    // Unit test: backward compatibility with legacy data (Requirements 3.4)
    @Test
    fun `legacy DynamoDB entity without new fields deserializes with defaults`() {
        // Simulate a legacy entity that only has the original fields
        val legacyData = mapOf(
            "id" to "pos-legacy-123",
            "title" to "Legacy Position",
            "description" to "A position from before the extended fields feature",
            "createdBy" to "admin-1",
            "isActive" to true,
            "external" to false,
            "fileUrl" to null,
            "createdAt" to "2024-01-15T10:30:00",
            "updatedAt" to "2024-01-15T10:30:00",
            "isFileDeleted" to false
            // No workMode, location, jobType, experienceMin, experienceMax, skills
        )

        val entity = org.example.notifier.infrastructure.persistence.DynamoEntity(
            pk = "POSITION#pos-legacy-123",
            sk = "METADATA",
            type = "OPEN_POSITION"
        )
        entity.data = legacyData.toMutableMap() as MutableMap<String, Any?>

        val position = OpenPositionMapper.fromDynamoEntity(entity)

        assertEquals("Onsite", position.workMode, "Default workMode should be Onsite")
        assertEquals("", position.location, "Default location should be empty string")
        assertEquals(null, position.jobType, "Default jobType should be null")
        assertEquals(null, position.experienceMin, "Default experienceMin should be null")
        assertEquals(null, position.experienceMax, "Default experienceMax should be null")
        assertEquals(emptyList<String>(), position.skills, "Default skills should be empty list")
    }
}
