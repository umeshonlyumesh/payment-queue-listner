package com.payment.queque.paymentqueuelistner.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Enriched payment data model that will be stored in DynamoDB.
 * Extends the base PaymentData class and adds additional enriched fields.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@DynamoDbBean
public class EnrichedPaymentData extends PaymentData {
    
    private String enrichmentId;
    private Map<String, String> additionalData;
    private String riskScore;
    private String fraudStatus;
    private LocalDateTime enrichmentTimestamp;
    private String processingStatus;
    private Long processingTimeMs;
    
    // Override to add DynamoDB partition key annotation
    @Override
    @DynamoDbPartitionKey
    public String getId() {
        return super.getId();
    }
    
    // Add sort key for efficient querying
    @DynamoDbSortKey
    public String getTransactionId() {
        return super.getTransactionId();
    }
}