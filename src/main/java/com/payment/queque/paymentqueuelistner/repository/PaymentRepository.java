package com.payment.queque.paymentqueuelistner.repository;

import com.payment.queque.paymentqueuelistner.model.EnrichedPaymentData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.Optional;

/**
 * Repository for storing and retrieving payment data from DynamoDB.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class PaymentRepository {

    private final DynamoDbEnhancedClient dynamoDbEnhancedClient;
    private final TableSchema<EnrichedPaymentData> tableSchema;

    @Value("${aws.dynamodb.table-name}")
    private String tableName;

    /**
     * Saves an enriched payment data item to DynamoDB.
     *
     * @param paymentData the enriched payment data to save
     * @return the saved enriched payment data
     */
    public EnrichedPaymentData save(EnrichedPaymentData paymentData) {
        try {
            DynamoDbTable<EnrichedPaymentData> table = getTable();
            table.putItem(paymentData);
            log.info("Successfully saved payment data with ID: {}", paymentData.getId());
            return paymentData;
        } catch (Exception e) {
            log.error("Error saving payment data to DynamoDB: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save payment data to DynamoDB", e);
        }
    }

    /**
     * Retrieves an enriched payment data item from DynamoDB by ID.
     *
     * @param id the ID of the payment data to retrieve
     * @return an Optional containing the enriched payment data if found, or empty if not found
     */
    public Optional<EnrichedPaymentData> findById(String id, String transactionId) {
        try {
            DynamoDbTable<EnrichedPaymentData> table = getTable();
            Key key = Key.builder()
                    .partitionValue(id)
                    .sortValue(transactionId)
                    .build();
            
            EnrichedPaymentData result = table.getItem(key);
            return Optional.ofNullable(result);
        } catch (Exception e) {
            log.error("Error retrieving payment data from DynamoDB: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve payment data from DynamoDB", e);
        }
    }

    /**
     * Gets the DynamoDB table for enriched payment data.
     *
     * @return the DynamoDB table
     */
    private DynamoDbTable<EnrichedPaymentData> getTable() {
        return dynamoDbEnhancedClient.table(tableName, tableSchema);
    }
}