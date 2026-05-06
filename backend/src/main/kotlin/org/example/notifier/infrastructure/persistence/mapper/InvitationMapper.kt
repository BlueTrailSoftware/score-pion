package org.example.notifier.infrastructure.persistence.mapper

import org.example.notifier.infrastructure.persistence.DynamoEntity
import org.example.notifier.infrastructure.persistence.DynamoKeyPatterns
import org.example.notifier.domain.invitation.Invitation
import java.time.Instant
import java.time.ZoneOffset

object InvitationMapper {

    fun toDynamoEntity(invitation: Invitation): DynamoEntity {
        val entity = DynamoEntity(
            pk = DynamoKeyPatterns.invitationPk(invitation.id),
            sk = DynamoKeyPatterns.invitationSk(),
            type = DynamoEntity.TYPE_INVITATION,
            gsi1pk = DynamoKeyPatterns.candidateAssessmentGsi1Pk(invitation.candidateEmail, invitation.assessmentId),
            gsi1sk = DynamoKeyPatterns.invitationGsi1Sk(),
            gsi2pk = DynamoKeyPatterns.ALL_INVITATIONS_GSI2_PK,
            gsi2sk = invitation.id,
            createdAt = invitation.createdAt.toInstant(ZoneOffset.UTC).toString()
        )

        entity.data = JacksonMapperUtil.toMap(invitation)

        return entity
    }

    fun fromDynamoEntity(entity: DynamoEntity): Invitation {
        return JacksonMapperUtil.fromMap(entity.data, Invitation::class.java)
    }
}
