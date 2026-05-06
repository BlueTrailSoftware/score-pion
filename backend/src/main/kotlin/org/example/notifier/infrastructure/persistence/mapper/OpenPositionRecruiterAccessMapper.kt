package org.example.notifier.infrastructure.persistence.mapper

import org.example.notifier.infrastructure.persistence.DynamoEntity
import org.example.notifier.infrastructure.persistence.DynamoKeyPatterns
import org.example.notifier.domain.position.OpenPositionRecruiterAccess
import java.time.Instant
import java.time.ZoneOffset

object OpenPositionRecruiterAccessMapper {

    fun toDynamoEntity(access: OpenPositionRecruiterAccess): DynamoEntity {
        val entity = DynamoEntity(
            pk = DynamoKeyPatterns.positionPk(access.openPositionId),
            sk = DynamoKeyPatterns.recruiterSk(access.recruiterId),
            type = DynamoEntity.TYPE_POSITION_ACCESS,
            gsi1pk = DynamoKeyPatterns.recruiterGsi1Pk(access.recruiterId),
            gsi1sk = DynamoKeyPatterns.positionPk(access.openPositionId),
            createdAt = access.grantedAt.toInstant(ZoneOffset.UTC).toString()
        )

        entity.data = JacksonMapperUtil.toMap(access)

        return entity
    }

    fun fromDynamoEntity(entity: DynamoEntity): OpenPositionRecruiterAccess {
        return JacksonMapperUtil.fromMap(entity.data, OpenPositionRecruiterAccess::class.java)
    }
}
