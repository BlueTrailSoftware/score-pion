package org.example.notifier.infrastructure.persistence.mapper

import org.example.notifier.infrastructure.persistence.DynamoEntity
import org.example.notifier.infrastructure.persistence.DynamoKeyPatterns
import org.example.notifier.domain.globalRecipients.GlobalRecipients
import java.time.Instant

object GlobalRecipientsMapper {

    fun toDynamoEntity(recipients: GlobalRecipients): DynamoEntity {
        return DynamoEntity(
            pk = DynamoKeyPatterns.globalRecipientsPk(),
            sk = DynamoKeyPatterns.globalRecipientsSk(),
            emails = recipients.emails.toList(),
            description = recipients.description,
            updatedAt = recipients.updatedAt,
            updatedBy = recipients.updatedBy
        )
    }

    fun fromDynamoEntity(entity: DynamoEntity): GlobalRecipients {
        return GlobalRecipients(
            id = entity.pk,
            version = entity.sk,
            emails = entity.emails?.toMutableList() ?: mutableListOf(),
            description = entity.description ?: "Global email recipients list",
            updatedAt = entity.updatedAt ?: Instant.now().toString(),
            updatedBy = entity.updatedBy
        )
    }
}
