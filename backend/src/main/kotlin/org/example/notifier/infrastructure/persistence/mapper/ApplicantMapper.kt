package org.example.notifier.infrastructure.persistence.mapper

import org.example.notifier.infrastructure.persistence.DynamoEntity
import org.example.notifier.infrastructure.persistence.DynamoKeyPatterns
import org.example.notifier.domain.applicant.Applicant
import java.time.ZoneOffset

object ApplicantMapper {

    fun toDynamoEntity(applicant: Applicant): DynamoEntity {
        val entity = DynamoEntity(
            pk = DynamoKeyPatterns.applicantPk(applicant.id),
            sk = DynamoKeyPatterns.applicantSk(),
            type = DynamoEntity.TYPE_APPLICANT,
            gsi1pk = DynamoKeyPatterns.applicantPositionGsi1Pk(applicant.positionId),
            gsi1sk = DynamoKeyPatterns.applicantPositionGsi1Sk(applicant.id),
            gsi2pk = DynamoKeyPatterns.applicantGsi2Pk(),
            gsi2sk = DynamoKeyPatterns.applicantGsi2Sk(applicant.email),
            createdAt = applicant.createdAt.toInstant(ZoneOffset.UTC).toString(),
            updatedAt = applicant.updatedAt.toInstant(ZoneOffset.UTC).toString()
        )

        entity.data = JacksonMapperUtil.toMap(applicant)

        return entity
    }

    fun fromDynamoEntity(entity: DynamoEntity): Applicant {
        return JacksonMapperUtil.fromMap(entity.data, Applicant::class.java)
    }
}
