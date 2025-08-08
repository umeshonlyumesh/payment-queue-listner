package com.payment.queque.paymentqueuelistner.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.queque.paymentqueuelistner.model.Transaction;
import com.payment.queque.paymentqueuelistner.repository.TransactionRepository;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * Service for processing transactions from the transaction table.
 * This service is responsible for fetching unprocessed transactions,
 * sending them to SQS, and updating their status.
 */
@Service
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final SqsTemplate sqsTemplate;
    private final ObjectMapper objectMapper;
    private final ExecutorService executorService;

    @Value("${aws.sqs.transaction-queue.url}")
    private String transactionQueueUrl;

    @Value("${app.use-native-query:false}")
    private boolean useNativeQuery;

    public TransactionService(
            TransactionRepository transactionRepository,
            SqsTemplate sqsTemplate,
            ObjectMapper objectMapper,
            @Qualifier("virtualThreadExecutor") ExecutorService executorService) {
        this.transactionRepository = transactionRepository;
        this.sqsTemplate = sqsTemplate;
        this.objectMapper = objectMapper;
        this.executorService = executorService;
    }

    /**
     * Processes all unprocessed transactions.
     * This method fetches all transactions with a processing status of "UNPROCESSED",
     * sends them to SQS, and updates their status to "PROCESSED".
     * 
     * It can use either JPA or native SQL queries based on configuration.
     *
     * @return the number of transactions processed
     */
    @Transactional
    public int processUnprocessedTransactions() {
        log.info("Fetching unprocessed transactions using {}", 
                useNativeQuery ? "native SQL query" : "JPA query");

        List<Transaction> unprocessedTransactions;
        if (useNativeQuery) {
            unprocessedTransactions = transactionRepository.findByProcessingStatusNative("UNPROCESSED");
        } else {
            unprocessedTransactions = transactionRepository.findByProcessingStatus("UNPROCESSED");
        }

        if (unprocessedTransactions.isEmpty()) {
            log.info("No unprocessed transactions found");
            return 0;
        }

        log.info("Found {} unprocessed transactions", unprocessedTransactions.size());

        // Process transactions in parallel using virtual threads
        List<CompletableFuture<Boolean>> futures = unprocessedTransactions.stream()
                .map(transaction -> CompletableFuture.supplyAsync(() -> 
                    processTransaction(transaction), executorService))
                .collect(Collectors.toList());

        // Wait for all tasks to complete and count successful ones
        int processedCount = (int) futures.stream()
                .map(CompletableFuture::join)
                .filter(Boolean::booleanValue)
                .count();

        log.info("Successfully processed {} transactions", processedCount);
        return processedCount;
    }

    /**
     * Processes a single transaction by sending it to SQS and updating its status.
     * This method is designed to be executed in parallel by multiple threads.
     *
     * @param transaction the transaction to process
     * @return true if the transaction was processed successfully, false otherwise
     */
    private boolean processTransaction(Transaction transaction) {
        try {
            // Convert transaction to JSON
            String transactionJson = objectMapper.writeValueAsString(transaction);

            // Send to SQS
            sqsTemplate.send(transactionQueueUrl, transactionJson);
            log.info("Sent transaction with ID {} to SQS", transaction.getId());

            // Update transaction status
            LocalDateTime now = LocalDateTime.now();
            int updatedRows = transactionRepository.updateProcessingStatus(
                    transaction.getId(), "PROCESSED", now);

            if (updatedRows > 0) {
                log.info("Updated transaction status to PROCESSED for ID {}", transaction.getId());
                return true;
            } else {
                log.warn("Failed to update transaction status for ID {}", transaction.getId());
                return false;
            }
        } catch (JsonProcessingException e) {
            log.error("Error serializing transaction with ID {}: {}", 
                    transaction.getId(), e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("Error processing transaction with ID {}: {}", 
                    transaction.getId(), e.getMessage(), e);
            return false;
        }
    }
}
