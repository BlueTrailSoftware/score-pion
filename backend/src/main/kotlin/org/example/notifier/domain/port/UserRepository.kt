package org.example.notifier.domain.port

import org.example.notifier.domain.user.User

interface UserRepository {
    suspend fun save(user: User): User
    suspend fun findById(id: String): User?
    suspend fun findByEmail(email: String): User?
    suspend fun findByGoogleId(googleId: String): User?
    suspend fun existsByEmail(email: String): Boolean
    suspend fun findAllByRole(role: String): List<User>
    suspend fun findAll(): List<User>
    suspend fun delete(user: User)
}
