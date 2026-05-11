package org.example.notifier.infrastructure.persistence.mapper

import org.example.notifier.infrastructure.persistence.DynamoEntity
import org.example.notifier.infrastructure.persistence.DynamoKeyPatterns
import org.example.notifier.domain.user.User
import java.time.Instant
import java.time.ZoneOffset

object UserMapper {

    fun toDynamoEntity(user: User): DynamoEntity {
        val entity = DynamoEntity(
            pk = DynamoKeyPatterns.userPk(user.id),
            sk = DynamoKeyPatterns.userSk(),
            type = DynamoEntity.TYPE_USER,
            gsi1pk = DynamoKeyPatterns.emailGsi1Pk(user.email.trim().lowercase()),
            gsi1sk = DynamoKeyPatterns.userGsi1Sk(),
            gsi2pk = user.googleId?.let { DynamoKeyPatterns.googleIdGsi2Pk(it) },
            gsi2sk = if (user.googleId != null) DynamoKeyPatterns.userGsi2Sk() else null,
            createdAt = user.createdAt.toInstant(ZoneOffset.UTC).toString(),
            updatedAt = user.updatedAt.toInstant(ZoneOffset.UTC).toString()
        )

        entity.data = JacksonMapperUtil.toMap(user)

        return entity
    }

    fun fromDynamoEntity(entity: DynamoEntity): User {
        return JacksonMapperUtil.fromMap(entity.data, User::class.java)
    }
}
