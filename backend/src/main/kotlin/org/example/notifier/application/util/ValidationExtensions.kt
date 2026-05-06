package org.example.notifier.application.util

/**
 * Extension function to validate email format
 */
fun String.isValidEmail(): Boolean {
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
    return this.matches(emailRegex)
}

/**
 * Extension function to validate phone format
 */
fun String.isValidPhone(): Boolean {
    val phoneRegex = "^[+]?[0-9\\s()-]{7,20}$".toRegex()
    return this.matches(phoneRegex)
}

/**
 * Extension function to validate LinkedIn profile URL format
 */
fun String.isValidLinkedInUrl(): Boolean {
    val linkedinRegex = "^https?://(www\\.)?linkedin\\.com/.*$".toRegex()
    return this.matches(linkedinRegex)
}

private val VALID_WORK_MODES = setOf("Onsite", "Remote", "Hybrid")
private val VALID_JOB_TYPES = setOf("Full Time", "Part Time", "Contract", "Internship")

/**
 * Validates the extended position fields shared by create and update flows.
 * Throws [IllegalArgumentException] with a descriptive message on the first validation failure.
 */
fun validatePositionExtendedFields(
    workMode: String,
    location: String,
    jobType: String?,
    experienceMin: Int?,
    experienceMax: Int?,
    skills: List<String>?
) {
    require(workMode in VALID_WORK_MODES) {
        "Invalid workMode: '$workMode'. Must be one of: ${VALID_WORK_MODES.joinToString(", ")}"
    }

    if (jobType != null) {
        require(jobType in VALID_JOB_TYPES) {
            "Invalid jobType: '$jobType'. Must be one of: ${VALID_JOB_TYPES.joinToString(", ")}"
        }
    }

    if (experienceMin != null) {
        require(experienceMin in 0..20) {
            "experienceMin must be between 0 and 20, got $experienceMin"
        }
    }

    if (experienceMax != null) {
        require(experienceMax in 0..20) {
            "experienceMax must be between 0 and 20, got $experienceMax"
        }
    }

    if (experienceMin != null && experienceMax != null) {
        require(experienceMin <= experienceMax) {
            "experienceMin ($experienceMin) must be <= experienceMax ($experienceMax)"
        }
    }

    require(location.length <= 200) {
        "location must not exceed 200 characters, got ${location.length}"
    }

    if (skills != null) {
        require(skills.none { it.isBlank() }) {
            "skills must not contain blank entries"
        }
        require(skills.size == skills.toSet().size) {
            "skills must not contain duplicates"
        }
    }
}
