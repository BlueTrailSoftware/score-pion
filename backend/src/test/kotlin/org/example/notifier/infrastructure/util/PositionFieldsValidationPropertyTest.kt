package org.example.notifier.infrastructure.util

import org.example.notifier.application.util.validatePositionExtendedFields
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.random.Random

class PositionFieldsValidationPropertyTest {

    private val validWorkModes = listOf("Onsite", "Remote", "Hybrid")
    private val validJobTypes = listOf("Full Time", "Part Time", "Contract", "Internship")
    private val charPool = ('a'..'z') + ('A'..'Z') + ('0'..'9') + listOf(' ', '-', '_')

    private fun randomString(minLen: Int = 1, maxLen: Int = 50, rng: Random = Random): String {
        val len = rng.nextInt(minLen, maxLen + 1)
        return (1..len).map { charPool[rng.nextInt(charPool.size)] }.joinToString("")
    }

    private fun randomNonBlankSkills(rng: Random): List<String> {
        val count = rng.nextInt(0, 6)
        val skills = mutableSetOf<String>()
        repeat(count) { skills.add(randomString(1, 20, rng)) }
        return skills.toList()
    }

    // Feature: position-extended-fields, Property 1: Valid extended fields pass through validation
    // Validates: Requirements 1.1, 1.3, 1.5, 1.7, 1.9
    @Test
    fun `Property 1 - valid extended fields pass through validation`() {
        repeat(100) {
            val rng = Random(it)
            val workMode = validWorkModes[rng.nextInt(validWorkModes.size)]
            val location = randomString(0, 200, rng)
            val jobType = if (rng.nextBoolean()) validJobTypes[rng.nextInt(validJobTypes.size)] else null
            val experienceMin = if (rng.nextBoolean()) rng.nextInt(0, 21) else null
            val experienceMax = when {
                experienceMin != null -> if (rng.nextBoolean()) rng.nextInt(experienceMin, 21) else null
                else -> if (rng.nextBoolean()) rng.nextInt(0, 21) else null
            }
            val skills = randomNonBlankSkills(rng)

            assertDoesNotThrow {
                validatePositionExtendedFields(workMode, location, jobType, experienceMin, experienceMax, skills)
            }
        }
    }

    // Feature: position-extended-fields, Property 2: Invalid enum values are rejected
    // Validates: Requirements 2.1, 2.2
    @Test
    fun `Property 2 - invalid enum values are rejected`() {
        repeat(100) {
            val rng = Random(it)
            val invalidString = randomString(1, 30, rng)

            // Test invalid workMode (skip if accidentally valid)
            if (invalidString !in validWorkModes) {
                val ex = assertThrows(IllegalArgumentException::class.java) {
                    validatePositionExtendedFields(
                        workMode = invalidString,
                        location = "Test",
                        jobType = null,
                        experienceMin = null,
                        experienceMax = null,
                        skills = null
                    )
                }
                assertTrue(ex.message!!.contains("workMode"))
            }

            // Test invalid jobType (skip if accidentally valid)
            if (invalidString !in validJobTypes) {
                val ex = assertThrows(IllegalArgumentException::class.java) {
                    validatePositionExtendedFields(
                        workMode = "Onsite",
                        location = "Test",
                        jobType = invalidString,
                        experienceMin = null,
                        experienceMax = null,
                        skills = null
                    )
                }
                assertTrue(ex.message!!.contains("jobType"))
            }
        }
    }

    // Feature: position-extended-fields, Property 3: Out-of-range experience values are rejected
    // Validates: Requirements 2.3, 2.4
    @Test
    fun `Property 3 - out-of-range experience values are rejected`() {
        repeat(100) {
            val rng = Random(it)
            // Generate a value outside 0..20
            val outOfRange = if (rng.nextBoolean()) rng.nextInt(-100, 0) else rng.nextInt(21, 200)

            // Test experienceMin out of range
            assertThrows(IllegalArgumentException::class.java) {
                validatePositionExtendedFields(
                    workMode = "Onsite",
                    location = "Test",
                    jobType = null,
                    experienceMin = outOfRange,
                    experienceMax = null,
                    skills = null
                )
            }

            // Test experienceMax out of range
            assertThrows(IllegalArgumentException::class.java) {
                validatePositionExtendedFields(
                    workMode = "Onsite",
                    location = "Test",
                    jobType = null,
                    experienceMin = null,
                    experienceMax = outOfRange,
                    skills = null
                )
            }
        }
    }

    // Feature: position-extended-fields, Property 4: Experience min greater than max is rejected
    // Validates: Requirements 2.5
    @Test
    fun `Property 4 - experience min greater than max is rejected`() {
        repeat(100) {
            val rng = Random(it)
            val max = rng.nextInt(0, 20) // 0..19 so min can be strictly greater
            val min = rng.nextInt(max + 1, 21)

            assertThrows(IllegalArgumentException::class.java) {
                validatePositionExtendedFields(
                    workMode = "Onsite",
                    location = "Test",
                    jobType = null,
                    experienceMin = min,
                    experienceMax = max,
                    skills = null
                )
            }
        }
    }

    // Feature: position-extended-fields, Property 5: Location exceeding 200 characters is rejected
    // Validates: Requirements 2.6
    @Test
    fun `Property 5 - location exceeding 200 characters is rejected`() {
        repeat(100) {
            val rng = Random(it)
            val longLocation = "a".repeat(rng.nextInt(201, 500))

            assertThrows(IllegalArgumentException::class.java) {
                validatePositionExtendedFields(
                    workMode = "Onsite",
                    location = longLocation,
                    jobType = null,
                    experienceMin = null,
                    experienceMax = null,
                    skills = null
                )
            }
        }
    }

    // Feature: position-extended-fields, Property 6: Skills with blank or duplicate entries are rejected
    // Validates: Requirements 2.7, 2.8
    @Test
    fun `Property 6 - skills with blank or duplicate entries are rejected`() {
        repeat(100) {
            val rng = Random(it)

            // Test blank entries
            val blankSkills = listOf("Kotlin", if (rng.nextBoolean()) "" else " ".repeat(rng.nextInt(1, 5)), "Java")
            assertThrows(IllegalArgumentException::class.java) {
                validatePositionExtendedFields(
                    workMode = "Onsite",
                    location = "Test",
                    jobType = null,
                    experienceMin = null,
                    experienceMax = null,
                    skills = blankSkills
                )
            }

            // Test duplicate entries
            val base = randomString(1, 15, rng)
            val duplicateSkills = listOf(base, "Other", base)
            assertThrows(IllegalArgumentException::class.java) {
                validatePositionExtendedFields(
                    workMode = "Onsite",
                    location = "Test",
                    jobType = null,
                    experienceMin = null,
                    experienceMax = null,
                    skills = duplicateSkills
                )
            }
        }
    }
}
