package org.example.notifier.infrastructure.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import java.net.URI

@Configuration
class DynamoDBConfig {

    @Value("\${aws.region}")
    private lateinit var region: String

    @Value("\${aws.dynamodb.endpoint:}")
    private lateinit var endpoint: String

    @Value("\${aws.access-key-id:}")
    private lateinit var accessKeyId: String

    @Value("\${aws.secret-access-key:}")
    private lateinit var secretAccessKey: String

    @Bean
    fun dynamoDbAsyncClient(): DynamoDbAsyncClient {
        val builder = DynamoDbAsyncClient.builder()
            .region(Region.of(region))

        if (endpoint.isNotEmpty()) {
            builder.endpointOverride(URI.create(endpoint))
        }

        if (accessKeyId.isNotEmpty() && secretAccessKey.isNotEmpty()) {
            val credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey)
            builder.credentialsProvider(StaticCredentialsProvider.create(credentials))
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.create())
        }

        return builder.build()
    }

    @Bean
    fun dynamoDbEnhancedAsyncClient(dynamoDbAsyncClient: DynamoDbAsyncClient): DynamoDbEnhancedAsyncClient {
        return DynamoDbEnhancedAsyncClient.builder()
            .dynamoDbClient(dynamoDbAsyncClient)
            .build()
    }
}
