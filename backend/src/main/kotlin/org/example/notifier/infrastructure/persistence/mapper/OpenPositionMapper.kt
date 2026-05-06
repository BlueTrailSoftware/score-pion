package org.example.notifier.infrastructure.persistence.mapper

import org.example.notifier.infrastructure.persistence.DynamoEntity
import org.example.notifier.infrastructure.persistence.DynamoKeyPatterns
import org.example.notifier.domain.position.OpenPosition
import java.time.Instant
import java.time.ZoneOffset

object OpenPositionMapper {

    fun toDynamoEntity(position: OpenPosition): DynamoEntity {
        val entity = DynamoEntity(
            pk = DynamoKeyPatterns.positionPk(position.id),
            sk = DynamoKeyPatterns.positionSk(),
            type = DynamoEntity.TYPE_OPEN_POSITION,
            gsi1pk = DynamoKeyPatterns.ALL_POSITIONS_GSI1_PK,
            gsi1sk = DynamoKeyPatterns.positionPk(position.id),
            createdAt = position.createdAt.toInstant(ZoneOffset.UTC).toString(),
            updatedAt = position.updatedAt.toInstant(ZoneOffset.UTC).toString()
        )

        entity.data = JacksonMapperUtil.toMap(position)

        return entity
    }

    fun fromDynamoEntity(entity: DynamoEntity): OpenPosition {
        return JacksonMapperUtil.fromMap(entity.data, OpenPosition::class.java)
    }
}
