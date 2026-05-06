package org.example.notifier.infrastructure.repository

import org.example.notifier.domain.port.RecruiterInvitationRepository
import org.example.notifier.infrastructure.persistence.DynamoDbAdapter
import org.example.notifier.infrastructure.persistence.DynamoKeyPatterns
import org.example.notifier.infrastructure.persistence.mapper.RecruiterInvitationMapper
import org.example.notifier.domain.invitation.RecruiterInvitation
import org.springframework.stereotype.Repository

@Repository
class DynamoRecruiterInvitationRepository(
    private val dynamoDbAdapter: DynamoDbAdapter
) : RecruiterInvitationRepository {

    override suspend fun save(invitation: RecruiterInvitation): RecruiterInvitation {
        val entity = RecruiterInvitationMapper.toDynamoEntity(invitation)
        dynamoDbAdapter.save(entity)
        return invitation
    }

    override suspend fun findById(id: String): RecruiterInvitation? {
        val entity = dynamoDbAdapter.findByPkAndSk(
            DynamoKeyPatterns.recruiterInvPk(id),
            DynamoKeyPatterns.recruiterInvSk()
        )
        return entity?.let { RecruiterInvitationMapper.fromDynamoEntity(it) }
    }

    override suspend fun findByEmail(email: String): RecruiterInvitation? {
        val entities = dynamoDbAdapter.queryGsi1(
            DynamoKeyPatterns.emailGsi1Pk(email.trim().lowercase()),
            DynamoKeyPatterns.recruiterInvGsi1Sk()
        )
        return entities.firstOrNull()?.let { RecruiterInvitationMapper.fromDynamoEntity(it) }
    }

    override suspend fun findAll(): List<RecruiterInvitation> {
        val entities = dynamoDbAdapter.queryGsi2(
            DynamoKeyPatterns.recruiterInvGsi2Pk()
        )

        return entities.map { RecruiterInvitationMapper.fromDynamoEntity(it) }
    }

    override suspend fun delete(invitation: RecruiterInvitation) {
        dynamoDbAdapter.delete(
            DynamoKeyPatterns.recruiterInvPk(invitation.id),
            DynamoKeyPatterns.recruiterInvSk()
        )
    }
}
