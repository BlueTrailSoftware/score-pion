package org.example.notifier.infrastructure.repository

import org.example.notifier.domain.port.ApplicantRepository
import org.example.notifier.infrastructure.persistence.DynamoDbAdapter
import org.example.notifier.infrastructure.persistence.DynamoKeyPatterns
import org.example.notifier.infrastructure.persistence.mapper.ApplicantMapper
import org.example.notifier.domain.applicant.Applicant
import org.springframework.stereotype.Repository

@Repository
class DynamoApplicantRepository(
    private val dynamoDbAdapter: DynamoDbAdapter
) : ApplicantRepository {

    override suspend fun save(applicant: Applicant): Applicant {
        val entity = ApplicantMapper.toDynamoEntity(applicant)
        dynamoDbAdapter.save(entity)
        return applicant
    }

    override suspend fun findById(id: String): Applicant? {
        val entity = dynamoDbAdapter.findByPkAndSk(
            DynamoKeyPatterns.applicantPk(id),
            DynamoKeyPatterns.applicantSk()
        )
        return entity?.let { ApplicantMapper.fromDynamoEntity(it) }
    }

    override suspend fun findAll(): List<Applicant> {
        val entities = dynamoDbAdapter.queryGsi2(
            DynamoKeyPatterns.applicantGsi2Pk()
        )
        return entities.map { ApplicantMapper.fromDynamoEntity(it) }
    }

    override suspend fun findByPositionId(positionId: String): List<Applicant> {
        val entities = dynamoDbAdapter.queryGsi1StartsWith(
            DynamoKeyPatterns.applicantPositionGsi1Pk(positionId),
            DynamoKeyPatterns.applicantPkPrefix()
        )
        return entities.map { ApplicantMapper.fromDynamoEntity(it) }
    }

    override suspend fun findByEmail(email: String): List<Applicant> {
        val entities = dynamoDbAdapter.queryGsi2(
            DynamoKeyPatterns.applicantGsi2Pk(),
            DynamoKeyPatterns.applicantGsi2Sk(email)
        )
        return entities
            .map { ApplicantMapper.fromDynamoEntity(it) }
            .filter { it.status != org.example.notifier.domain.applicant.ApplicantStatus.ANONYMIZED }
    }

    override suspend fun findByEmailAndPositionId(email: String, positionId: String): Applicant? {
        val applicantsForPosition = findByPositionId(positionId)
        return applicantsForPosition.firstOrNull { it.email.equals(email, ignoreCase = true) }
    }

    override suspend fun delete(applicant: Applicant) {
        dynamoDbAdapter.delete(
            DynamoKeyPatterns.applicantPk(applicant.id),
            DynamoKeyPatterns.applicantSk()
        )
    }
}
