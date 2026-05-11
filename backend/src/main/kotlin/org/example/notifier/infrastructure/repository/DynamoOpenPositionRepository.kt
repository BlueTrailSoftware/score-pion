package org.example.notifier.infrastructure.repository

import org.example.notifier.infrastructure.persistence.DynamoDbAdapter
import org.example.notifier.infrastructure.persistence.DynamoKeyPatterns
import org.example.notifier.infrastructure.persistence.mapper.OpenPositionMapper
import org.example.notifier.domain.position.OpenPosition
import org.springframework.stereotype.Repository

import org.example.notifier.domain.port.OpenPositionRepository

@Repository
class DynamoOpenPositionRepository(
    private val dynamoDbAdapter: DynamoDbAdapter
) : OpenPositionRepository {

    override suspend fun save(position: OpenPosition): OpenPosition {
        val entity = OpenPositionMapper.toDynamoEntity(position)
        dynamoDbAdapter.save(entity)
        return position
    }

    override suspend fun findByIdsBatch(ids: List<String>): List<OpenPosition> {
        if (ids.isEmpty()) return emptyList()

        val entities = ids.mapNotNull { id ->
            dynamoDbAdapter.findByPkAndSk(
                DynamoKeyPatterns.positionPk(id),
                DynamoKeyPatterns.positionSk()
            )
        }

        return entities.map { OpenPositionMapper.fromDynamoEntity(it) }
    }

    override suspend fun findById(id: String): OpenPosition? {
        val entity = dynamoDbAdapter.findByPkAndSk(
            DynamoKeyPatterns.positionPk(id),
            DynamoKeyPatterns.positionSk()
        )
        return entity?.let { OpenPositionMapper.fromDynamoEntity(it) }
    }

    override suspend fun findAll(): List<OpenPosition> {
        val entities = dynamoDbAdapter.queryGsi1(DynamoKeyPatterns.ALL_POSITIONS_GSI1_PK)
        return entities.map { OpenPositionMapper.fromDynamoEntity(it) }
    }

    override suspend fun findAllActive(): List<OpenPosition> {
        return findAll().filter { it.isActive }
    }

    override suspend fun delete(position: OpenPosition) {
        dynamoDbAdapter.delete(
            DynamoKeyPatterns.positionPk(position.id),
            DynamoKeyPatterns.positionSk()
        )
    }
}
