package org.example.notifier.infrastructure.persistence

import kotlinx.coroutines.future.await
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import software.amazon.awssdk.core.async.SdkPublisher
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable
import software.amazon.awssdk.enhanced.dynamodb.Expression
import software.amazon.awssdk.enhanced.dynamodb.Key
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional
import software.amazon.awssdk.enhanced.dynamodb.model.Page
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

@Component
class DynamoDbAdapter(
    private val dynamoDbEnhancedAsyncClient: DynamoDbEnhancedAsyncClient,
    @Value("\${dynamodb.table.name}") private val tableName: String
) {
    private val table: DynamoDbAsyncTable<DynamoEntity> = dynamoDbEnhancedAsyncClient.table(
        tableName,
        TableSchema.fromBean(DynamoEntity::class.java)
    )

    suspend fun save(entity: DynamoEntity): DynamoEntity {
        table.putItem(entity).await()
        return entity
    }

    suspend fun findByPkAndSk(pk: String, sk: String): DynamoEntity? {
        val key = Key.builder()
            .partitionValue(pk)
            .sortValue(sk)
            .build()
        return table.getItem(key).await()
    }

    suspend fun queryByPk(pk: String): List<DynamoEntity> {
        val query = QueryConditional.keyEqualTo(
            Key.builder().partitionValue(pk).build()
        )
        return collectQueryResults(table.query(query))
    }

    suspend fun queryByPkStartsWith(pk: String, skPrefix: String): List<DynamoEntity> {
        val query = QueryConditional.sortBeginsWith(
            Key.builder()
                .partitionValue(pk)
                .sortValue(skPrefix)
                .build()
        )
        return collectQueryResults(table.query(query))
    }

    suspend fun queryGsi1(gsi1pk: String, gsi1sk: String? = null): List<DynamoEntity> {
        val index = table.index("GSI1")
        val keyBuilder = Key.builder().partitionValue(gsi1pk)
        gsi1sk?.let { keyBuilder.sortValue(it) }

        val query = QueryConditional.keyEqualTo(keyBuilder.build())
        return collectQueryResults(index.query(query))
    }

    suspend fun queryGsi1StartsWith(gsi1pk: String, gsi1skPrefix: String): List<DynamoEntity> {
        val index = table.index("GSI1")
        val query = QueryConditional.sortBeginsWith(
            Key.builder()
                .partitionValue(gsi1pk)
                .sortValue(gsi1skPrefix)
                .build()
        )
        return collectQueryResults(index.query(query))
    }

    suspend fun queryGsi2(gsi2pk: String, gsi2sk: String? = null): List<DynamoEntity> {
        val index = table.index("GSI2")
        val keyBuilder = Key.builder().partitionValue(gsi2pk)
        gsi2sk?.let { keyBuilder.sortValue(it) }

        val query = QueryConditional.keyEqualTo(keyBuilder.build())
        return collectQueryResults(index.query(query))
    }

    /**
     * Helper function to collect all items from a DynamoDB query result publisher
     */
    private suspend fun collectQueryResults(publisher: SdkPublisher<Page<DynamoEntity>>): List<DynamoEntity> {
        return Flux.from(publisher)
            .flatMapIterable { page -> page.items() }
            .collectList()
            .awaitSingle()
    }

    suspend fun scanByType(type: String): List<DynamoEntity> {
        val filterExpression = Expression.builder()
            .expression("#t = :type")
            .putExpressionName("#t", "Type")
            .putExpressionValue(":type", AttributeValue.builder().s(type).build())
            .build()
        val scanRequest = ScanEnhancedRequest.builder()
            .filterExpression(filterExpression)
            .build()
        return Flux.from(table.scan(scanRequest))
            .flatMapIterable { page -> page.items() }
            .collectList()
            .awaitSingle()
    }

    suspend fun delete(pk: String, sk: String) {
        val key = Key.builder()
            .partitionValue(pk)
            .sortValue(sk)
            .build()
        table.deleteItem(key).await()
    }
}
