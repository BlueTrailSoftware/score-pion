package org.example.notifier.infrastructure.repository

import org.example.notifier.infrastructure.persistence.DynamoDbAdapter
import org.example.notifier.infrastructure.persistence.DynamoKeyPatterns
import org.example.notifier.infrastructure.persistence.mapper.OpenPositionRecruiterAccessMapper
import org.example.notifier.domain.position.OpenPositionRecruiterAccess
import org.springframework.stereotype.Repository

import org.example.notifier.domain.port.OpenPositionRecruiterAccessRepository

@Repository
class DynamoOpenPositionRecruiterAccessRepository(
    private val dynamoDbAdapter: DynamoDbAdapter
) : OpenPositionRecruiterAccessRepository {

    override suspend fun save(access: OpenPositionRecruiterAccess): OpenPositionRecruiterAccess {
        val entity = OpenPositionRecruiterAccessMapper.toDynamoEntity(access)
        dynamoDbAdapter.save(entity)
        return access
    }

    override suspend fun findByRecruiterId(recruiterId: String): List<OpenPositionRecruiterAccess> {
        val entities = dynamoDbAdapter.queryGsi1(
            DynamoKeyPatterns.recruiterGsi1Pk(recruiterId)
        )
        return entities.map { OpenPositionRecruiterAccessMapper.fromDynamoEntity(it) }
    }

    override suspend fun findByRecruiterIdAndPositionId(
        recruiterId: String,
        positionId: String
    ): OpenPositionRecruiterAccess? {
        val entity = dynamoDbAdapter.findByPkAndSk(
            DynamoKeyPatterns.positionPk(positionId),
            DynamoKeyPatterns.recruiterSk(recruiterId)
        )
        return entity?.let { OpenPositionRecruiterAccessMapper.fromDynamoEntity(it) }
    }

    override suspend fun findByPositionId(positionId: String): List<OpenPositionRecruiterAccess> {
        val entities = dynamoDbAdapter.queryByPkStartsWith(
            DynamoKeyPatterns.positionPk(positionId),
            DynamoKeyPatterns.recruiterSkPrefix()
        )
        return entities.map { OpenPositionRecruiterAccessMapper.fromDynamoEntity(it) }
    }

    override suspend fun delete(positionId: String, recruiterId: String) {
        dynamoDbAdapter.delete(
            DynamoKeyPatterns.positionPk(positionId),
            DynamoKeyPatterns.recruiterSk(recruiterId)
        )
    }
}
