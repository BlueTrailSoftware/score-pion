package org.example.notifier.application.service.core

import org.example.notifier.domain.applicant.Applicant
import org.example.notifier.domain.applicant.AnonymizationResult
import org.example.notifier.domain.applicant.CandidatePositionKey
import org.example.notifier.domain.user.User

interface ApplicantService {
    suspend fun findByEmailAndPositionId(email: String, positionId: String): Applicant?
    suspend fun createApplicant(applicant: Applicant): Applicant
    suspend fun getAllApplicants(status: String? = null, positionId: String? = null, search: String? = null): List<Applicant>
    suspend fun getApplicantsForRecruiter(invitedKeys: Set<CandidatePositionKey>, status: String? = null, positionId: String? = null, search: String? = null): List<Applicant>
    suspend fun getApplicantById(id: String): Applicant?
    suspend fun updateApplicantStatus(id: String, newStatus: String, reviewedBy: User, statusNote: String? = null): Applicant
    suspend fun updateApplicant(id: String, name: String? = null, email: String? = null, phone: String? = null, fileUrl: String? = null, isFileDeleted: Boolean = false): Applicant
    suspend fun getApplicantsByEmail(email: String): List<Applicant>
    suspend fun anonymizeApplicantsByEmail(email: String): Int
    suspend fun anonymizeSingleApplicant(applicant: Applicant): AnonymizationResult
    suspend fun exportApplicantData(email: String): ByteArray
}
