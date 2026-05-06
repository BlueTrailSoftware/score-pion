package org.example.notifier.domain.port

import org.example.notifier.domain.applicant.Applicant

interface ApplicantRepository {
    suspend fun save(applicant: Applicant): Applicant
    suspend fun findById(id: String): Applicant?
    suspend fun findAll(): List<Applicant>
    suspend fun findByPositionId(positionId: String): List<Applicant>
    suspend fun findByEmail(email: String): List<Applicant>
    suspend fun findByEmailAndPositionId(email: String, positionId: String): Applicant?
    suspend fun delete(applicant: Applicant)
}
