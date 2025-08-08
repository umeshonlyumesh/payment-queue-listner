package com.payment.queque.paymentqueuelistner.config;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import com.payment.queque.paymentqueuelistner.model.EnrichedPaymentData;

/**
 * Configuration class for AWS services (SQS and DynamoDB).
 */
@Configuration
public class AwsConfig {

    @Value("${aws.region}")
    private String region;

    @Value("${aws.credentials.access-key}")
    private String accessKey;

    @Value("${aws.credentials.secret-key}")
    private String secretKey;

    @Value("${aws.dynamodb.table-name}")
    private String dynamoDbTableName;

    /**
     * Creates an AWS credentials provider using the configured access key and secret key.
     */
    @Bean
    public StaticCredentialsProvider awsCredentialsProvider() {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey));
    }

    /**
     * Creates an SQS async client using the configured region and credentials.
     */
    @Bean
    public SqsAsyncClient sqsAsyncClient(StaticCredentialsProvider credentialsProvider) {
        return SqsAsyncClient.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .build();
    }

    /**
     * Creates an SQS template for sending and receiving messages.
     */
    @Bean
    public SqsTemplate sqsTemplate(SqsAsyncClient sqsAsyncClient) {
        return SqsTemplate.newTemplate(sqsAsyncClient);
    }

    /**
     * Creates a DynamoDB client using the configured region and credentials.
     */
    @Bean
    public DynamoDbClient dynamoDbClient(StaticCredentialsProvider credentialsProvider) {
        return DynamoDbClient.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .build();
    }

    /**
     * Creates a DynamoDB enhanced client for easier interaction with DynamoDB.
     */
    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
    }

    /**
     * Creates a TableSchema for the EnrichedPaymentData class.
     */
    @Bean
    public TableSchema<EnrichedPaymentData> enrichedPaymentDataTableSchema() {
        return TableSchema.fromBean(EnrichedPaymentData.class);
    }
}
