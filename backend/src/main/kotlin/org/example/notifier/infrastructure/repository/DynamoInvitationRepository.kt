package org.example.notifier.infrastructure.repository

import org.example.notifier.domain.port.InvitationRepository
import org.example.notifier.infrastructure.persistence.DynamoDbAdapter
import org.example.notifier.infrastructure.persistence.DynamoEntity
import org.example.notifier.infrastructure.persistence.DynamoKeyPatterns
import org.example.notifier.infrastructure.persistence.mapper.InvitationMapper
import org.example.notifier.domain.invitation.Invitation
import org.springframework.stereotype.Repository

@Repository
class DynamoInvitationRepository(
    private val dynamoDbAdapter: DynamoDbAdapter
) : InvitationRepository {

    override suspend fun save(invitation: Invitation): Invitation {
        val entity = InvitationMapper.toDynamoEntity(invitation)
        dynamoDbAdapter.save(entity)
        return invitation
    }

    override suspend fun findById(id: String): Invitation? {
        val entity = dynamoDbAdapter.findByPkAndSk(
            DynamoKeyPatterns.invitationPk(id),
            DynamoKeyPatterns.invitationSk()
        )
        return entity?.let { InvitationMapper.fromDynamoEntity(it) }
    }

    override suspend fun  findByCandidateEmailAndAssessmentId(
        candidateEmail: String,
        assessmentId: String
    ): Invitation? {
        val entities = dynamoDbAdapter.queryGsi1(
            DynamoKeyPatterns.candidateAssessmentGsi1Pk(candidateEmail, assessmentId),
            DynamoKeyPatterns.invitationGsi1Sk()
        )
        return entities.firstOrNull()?.let { InvitationMapper.fromDynamoEntity(it) }
    }

    override suspend fun findAll(): List<Invitation> {
        val entities = dynamoDbAdapter.scanByType(DynamoEntity.TYPE_INVITATION)
        return entities.map { InvitationMapper.fromDynamoEntity(it) }
    }

    override suspend fun findByRecruiterId(recruiterId: String): List<Invitation> {
        return findAll().filter { it.recruiterId == recruiterId }
    }

    override suspend fun delete(invitation: Invitation) {
        dynamoDbAdapter.delete(
            DynamoKeyPatterns.invitationPk(invitation.id),
            DynamoKeyPatterns.invitationSk()
        )
    }
}
