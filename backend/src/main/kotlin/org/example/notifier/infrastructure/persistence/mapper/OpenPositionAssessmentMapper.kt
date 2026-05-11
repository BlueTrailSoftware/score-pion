package org.example.notifier.infrastructure.persistence.mapper

import org.example.notifier.infrastructure.persistence.DynamoEntity
import org.example.notifier.infrastructure.persistence.DynamoKeyPatterns
import org.example.notifier.domain.position.OpenPositionAssessment
import java.time.Instant
import java.time.ZoneOffset

object OpenPositionAssessmentMapper {

    fun toDynamoEntity(assessment: OpenPositionAssessment): DynamoEntity {
        val entity = DynamoEntity(
            pk = DynamoKeyPatterns.positionPk(assessment.openPositionId),
            sk = DynamoKeyPatterns.assessmentSk(assessment.assessmentId),
            type = DynamoEntity.TYPE_POSITION_ASSESSMENT,
            createdAt = assessment.addedAt.toInstant(ZoneOffset.UTC).toString()
        )

        entity.data = JacksonMapperUtil.toMap(assessment)

        return entity
    }

    fun fromDynamoEntity(entity: DynamoEntity): OpenPositionAssessment {
        return JacksonMapperUtil.fromMap(entity.data, OpenPositionAssessment::class.java)
    }
}
