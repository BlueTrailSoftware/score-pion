package org.example.notifier.infrastructure.persistence

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbIgnore
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey

@DynamoDbBean
data class DynamoEntity(
    @get:DynamoDbPartitionKey
    @get:DynamoDbAttribute("PK")
    var pk: String = "",

    @get:DynamoDbSortKey
    @get:DynamoDbAttribute("SK")
    var sk: String = "",

    @get:DynamoDbAttribute("Type")
    var type: String = "",

    @get:DynamoDbSecondaryPartitionKey(indexNames = ["GSI1"])
    @get:DynamoDbAttribute("GSI1PK")
    var gsi1pk: String? = null,

    @get:DynamoDbSecondarySortKey(indexNames = ["GSI1"])
    @get:DynamoDbAttribute("GSI1SK")
    var gsi1sk: String? = null,

    @get:DynamoDbSecondaryPartitionKey(indexNames = ["GSI2"])
    @get:DynamoDbAttribute("GSI2PK")
    var gsi2pk: String? = null,

    @get:DynamoDbSecondarySortKey(indexNames = ["GSI2"])
    @get:DynamoDbAttribute("GSI2SK")
    var gsi2sk: String? = null,

    @get:DynamoDbAttribute("data")
    var dataJson: String = "{}",

    @get:DynamoDbAttribute("emails")
    var emails: List<String>? = null,

    @get:DynamoDbAttribute("description")
    var description: String? = null,

    @get:DynamoDbAttribute("updatedBy")
    var updatedBy: String? = null,

    @get:DynamoDbAttribute("createdAt")
    var createdAt: String = "",

    @get:DynamoDbAttribute("updatedAt")
    var updatedAt: String? = null
) {

    // Convenience property to work with data as Map (not persisted directly to DynamoDB)
    @get:DynamoDbIgnore
    var data: MutableMap<String, Any?>
        get() = try {
            objectMapper.readValue(dataJson, Map::class.java) as MutableMap<String, Any?>
        } catch (e: Exception) {
            mutableMapOf()
        }
        set(value) {
            dataJson = objectMapper.writeValueAsString(value)
        }

    companion object {
        private val objectMapper = jacksonObjectMapper()

        const val TYPE_USER = "USER"
        const val TYPE_INVITATION = "INVITATION"
        const val TYPE_RECRUITER_INVITATION = "RECRUITER_INVITATION"
        const val TYPE_OPEN_POSITION = "OPEN_POSITION"
        const val TYPE_POSITION_ASSESSMENT = "POSITION_ASSESSMENT"
        const val TYPE_POSITION_ACCESS = "POSITION_ACCESS"
        const val TYPE_APPLICANT = "APPLICANT"
    }
}
