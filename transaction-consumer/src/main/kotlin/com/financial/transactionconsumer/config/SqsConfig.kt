package com.financial.transactionconsumer.config

import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import java.net.URI
import java.time.Duration

@Configuration
class SqsConfig(
    @Value("\${aws.endpoint}") private val endpoint: String,
    @Value("\${aws.region}") private val region: String,
    @Value("\${aws.access-key}") private val accessKey: String,
    @Value("\${aws.secret-key}") private val secretKey: String,
) {

    @Bean
    fun sqsAsyncClient(): SqsAsyncClient = SqsAsyncClient.builder()
        .region(Region.of(region))
        .endpointOverride(URI.create(endpoint))
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)
            )
        )
        .build()

    @Bean
    fun defaultSqsListenerContainerFactory(
        sqsAsyncClient: SqsAsyncClient,
        @Value("\${sqs.consumer-threads:4}") consumerThreads: Int,
        @Value("\${sqs.max-messages:10}") maxMessages: Int,
        @Value("\${sqs.wait-time-seconds:20}") waitTimeSeconds: Int,
    ): SqsMessageListenerContainerFactory<Any> =
        SqsMessageListenerContainerFactory.builder<Any>()
            .configure { opts ->
                opts
                    .maxConcurrentMessages(consumerThreads * maxMessages)
                    .maxMessagesPerPoll(maxMessages)
                    .pollTimeout(Duration.ofSeconds(waitTimeSeconds.toLong()))
            }
            .sqsAsyncClient(sqsAsyncClient)
            .build()
}

