
package org.example.notifier.infrastructure.config

import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.smithy.kotlin.runtime.net.url.Url
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import kotlinx.coroutines.runBlocking

@Configuration
class AwsConfig {

    @Value("\${aws.region}")
    private lateinit var region: String

    @Value("\${aws.access-key-id:}")
    private lateinit var accessKeyId: String

    @Value("\${aws.secret-access-key:}")
    private lateinit var secretAccessKey: String

    @Value("\${aws.s3.path-style-access:false}")
    private var pathStyleAccess: Boolean = false

    @Value("\${aws.s3.endpoint:}")
    private lateinit var s3Endpoint: String

    @Bean
    fun s3Client(): S3Client = runBlocking {
         S3Client.fromEnvironment {
            region = this@AwsConfig.region
            forcePathStyle = pathStyleAccess
            if (s3Endpoint.isNotEmpty()) {
                endpointUrl = Url.parse(s3Endpoint)
            }
            if (accessKeyId.isNotEmpty() && secretAccessKey.isNotEmpty()) {
                credentialsProvider = StaticCredentialsProvider {
                    this.accessKeyId = this@AwsConfig.accessKeyId
                    this.secretAccessKey = this@AwsConfig.secretAccessKey
                }
            }
        }
    }
}
