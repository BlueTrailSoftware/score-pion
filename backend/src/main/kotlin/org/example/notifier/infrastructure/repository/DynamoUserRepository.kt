package org.example.notifier.infrastructure.repository

import org.example.notifier.domain.port.UserRepository
import org.example.notifier.infrastructure.persistence.DynamoDbAdapter
import org.example.notifier.infrastructure.persistence.DynamoEntity
import org.example.notifier.infrastructure.persistence.DynamoKeyPatterns
import org.example.notifier.infrastructure.persistence.mapper.UserMapper
import org.example.notifier.domain.user.User
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
class DynamoUserRepository(
    private val dynamoDbAdapter: DynamoDbAdapter
) : UserRepository {

    companion object {
        private const val USER_ROLE_INDEX_TYPE = "USER_ROLE_INDEX"
        private const val USER_ID_KEY = "userId"
        private const val ROLE_KEY = "role"
    }

    override suspend fun save(user: User): User {
        // Save main user entity
        val entity = UserMapper.toDynamoEntity(user)
        dynamoDbAdapter.save(entity)

        // Save role index entity for efficient querying by role
        val roleIndexEntity = DynamoEntity(
            pk = DynamoKeyPatterns.userRoleIndexPk(user.role),
            sk = DynamoKeyPatterns.userRoleIndexSk(user.id),
            type = USER_ROLE_INDEX_TYPE,
            gsi1pk = DynamoKeyPatterns.userRoleIndexPk(user.role),
            gsi1sk = DynamoKeyPatterns.userRoleIndexSk(user.id),
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
        roleIndexEntity.data = mutableMapOf<String, Any?>(USER_ID_KEY to user.id, ROLE_KEY to user.role)
        dynamoDbAdapter.save(roleIndexEntity)

        return user
    }

    override suspend fun findById(id: String): User? {
        val entity = dynamoDbAdapter.findByPkAndSk(
            DynamoKeyPatterns.userPk(id),
            DynamoKeyPatterns.userSk()
        )
        return entity?.let { UserMapper.fromDynamoEntity(it) }
    }

    override suspend fun findByEmail(email: String): User? {
        val entities = dynamoDbAdapter.queryGsi1(
            DynamoKeyPatterns.emailGsi1Pk(email.trim().lowercase()),
            DynamoKeyPatterns.userGsi1Sk()
        )
        return entities.firstOrNull()?.let { UserMapper.fromDynamoEntity(it) }
    }

    override suspend fun findByGoogleId(googleId: String): User? {
        val entities = dynamoDbAdapter.queryGsi2(
            DynamoKeyPatterns.googleIdGsi2Pk(googleId),
            DynamoKeyPatterns.userGsi2Sk()
        )
        return entities.firstOrNull()?.let { UserMapper.fromDynamoEntity(it) }
    }

    override suspend fun existsByEmail(email: String): Boolean {
        return findByEmail(email) != null
    }

    override suspend fun findAllByRole(role: String): List<User> {
        // Query GSI1 using role index pattern
        val roleIndexEntities = dynamoDbAdapter.queryGsi1(
            DynamoKeyPatterns.userRoleIndexPk(role)
        )

        // Extract user IDs from role index entities and fetch full user data
        return roleIndexEntities.mapNotNull { roleIndexEntity ->
            val userId = roleIndexEntity.data[USER_ID_KEY] as? String
            userId?.let { findById(it) }
        }
    }

    override suspend fun delete(user: User) {
        // Delete main user entity
        dynamoDbAdapter.delete(
            DynamoKeyPatterns.userPk(user.id),
            DynamoKeyPatterns.userSk()
        )

        // Delete role index entity
        dynamoDbAdapter.delete(
            DynamoKeyPatterns.userRoleIndexPk(user.role),
            DynamoKeyPatterns.userRoleIndexSk(user.id)
        )
    }

    override suspend fun findAll(): List<User> {
        val entities = dynamoDbAdapter.queryByPkStartsWith(
            DynamoKeyPatterns.userPkPrefix(),
            DynamoKeyPatterns.userSk()
        )
        return entities.map { UserMapper.fromDynamoEntity(it) }
    }
}
