package org.example.notifier.infrastructure.persistence.mapper

import org.example.notifier.infrastructure.persistence.DynamoEntity
import org.example.notifier.infrastructure.persistence.DynamoKeyPatterns
import org.example.notifier.domain.invitation.RecruiterInvitation
import java.time.Instant
import java.time.ZoneOffset

object RecruiterInvitationMapper {

    fun toDynamoEntity(invitation: RecruiterInvitation): DynamoEntity {
        val entity = DynamoEntity(
            pk = DynamoKeyPatterns.recruiterInvPk(invitation.id),
            sk = DynamoKeyPatterns.recruiterInvSk(),
            type = DynamoEntity.TYPE_RECRUITER_INVITATION,
            gsi1pk = DynamoKeyPatterns.emailGsi1Pk(invitation.email),
            gsi1sk = DynamoKeyPatterns.recruiterInvGsi1Sk(),
            gsi2pk = DynamoKeyPatterns.recruiterInvGsi2Pk(),
            gsi2sk = DynamoKeyPatterns.recruiterInvGsi2Sk(invitation.id),
            createdAt = invitation.createdAt.toInstant(ZoneOffset.UTC).toString()
        )

        entity.data = JacksonMapperUtil.toMap(invitation)

        return entity
    }

    fun fromDynamoEntity(entity: DynamoEntity): RecruiterInvitation {
        return JacksonMapperUtil.fromMap(entity.data, RecruiterInvitation::class.java)
    }
}
