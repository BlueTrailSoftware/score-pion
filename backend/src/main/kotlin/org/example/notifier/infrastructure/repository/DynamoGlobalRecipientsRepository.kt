package org.example.notifier.infrastructure.repository

import org.example.notifier.domain.port.GlobalRecipientsRepository
import org.example.notifier.infrastructure.persistence.DynamoDbAdapter
import org.example.notifier.infrastructure.persistence.DynamoKeyPatterns
import org.example.notifier.infrastructure.persistence.mapper.GlobalRecipientsMapper
import org.example.notifier.infrastructure.logging.LoggerPort
import org.example.notifier.domain.globalRecipients.GlobalRecipients
import org.example.notifier.application.util.isValidEmail
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class DynamoGlobalRecipientsRepository(
    private val dynamoDbAdapter: DynamoDbAdapter,
    private val logger: LoggerPort
) : GlobalRecipientsRepository {

    override suspend fun getRecipients(): GlobalRecipients? {
        return try {
            val entity = dynamoDbAdapter.findByPkAndSk(
                DynamoKeyPatterns.globalRecipientsPk(),
                DynamoKeyPatterns.globalRecipientsSk()
            )
            entity?.let { GlobalRecipientsMapper.fromDynamoEntity(it) }
        } catch (e: Exception) {
            logger.error("Failed to get global recipients: {}", e.message, e)
            null
        }
    }

    override suspend fun addEmail(email: String, updatedBy: String): GlobalRecipients {
        require(email.isValidEmail()) { "Invalid email format: $email" }

        try {
            val current = getRecipients() ?: GlobalRecipients()

            if (current.emails.contains(email)) {
                throw IllegalArgumentException("Email already exists in recipients list")
            }

            val updatedEmails = current.emails.toMutableList().apply { add(email) }

            val updated = current.copy(
                emails = updatedEmails,
                updatedAt = Instant.now().toString(),
                updatedBy = updatedBy
            )

            val entity = GlobalRecipientsMapper.toDynamoEntity(updated)
            dynamoDbAdapter.save(entity)

            logger.info("Email {} added to global recipients by user {}", email, updatedBy)
            return updated

        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            logger.error("Failed to add email to global recipients: {}", e.message, e)
            throw IllegalStateException("Failed to add email: ${e.message}", e)
        }
    }

    override suspend fun removeEmail(email: String, updatedBy: String): GlobalRecipients {
        try {
            val current = getRecipients()
                ?: throw IllegalStateException("Global recipients not found")

            if (!current.emails.contains(email)) {
                throw IllegalArgumentException("Email not found in recipients list")
            }

            val updatedEmails = current.emails.toMutableList().apply { remove(email) }

            val updated = current.copy(
                emails = updatedEmails,
                updatedAt = Instant.now().toString(),
                updatedBy = updatedBy
            )

            val entity = GlobalRecipientsMapper.toDynamoEntity(updated)
            dynamoDbAdapter.save(entity)

            logger.info("Email {} removed from global recipients by user {}", email, updatedBy)
            return updated

        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            logger.error("Failed to remove email from global recipients: {}", e.message, e)
            throw IllegalStateException("Failed to remove email: ${e.message}", e)
        }
    }

    override suspend fun updateEmail(
        oldEmail: String,
        newEmail: String,
        updatedBy: String
    ): GlobalRecipients {
        require(newEmail.isValidEmail()) { "Invalid email format: $newEmail" }

        try {
            val current = getRecipients()
                ?: throw IllegalStateException("Global recipients not found")

            if (!current.emails.contains(oldEmail)) {
                throw IllegalArgumentException("Email $oldEmail not found in recipients list")
            }

            if (current.emails.contains(newEmail) && oldEmail != newEmail) {
                throw IllegalArgumentException("Email $newEmail already exists in recipients list")
            }

            val updatedEmails = current.emails.toMutableList().apply {
                val index = indexOf(oldEmail)
                set(index, newEmail)
            }

            val updated = current.copy(
                emails = updatedEmails,
                updatedAt = Instant.now().toString(),
                updatedBy = updatedBy
            )

            val entity = GlobalRecipientsMapper.toDynamoEntity(updated)
            dynamoDbAdapter.save(entity)

            logger.info("Email updated from {} to {} by user {}", oldEmail, newEmail, updatedBy)
            return updated

        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            logger.error("Failed to update email in global recipients: {}", e.message, e)
            throw IllegalStateException("Failed to update email: ${e.message}", e)
        }
    }

    override suspend fun getAllEmails(): List<String> {
        return getRecipients()?.emails?.toList() ?: emptyList()
    }

    override suspend fun hasRecipients(): Boolean {
        return getAllEmails().isNotEmpty()
    }
}
