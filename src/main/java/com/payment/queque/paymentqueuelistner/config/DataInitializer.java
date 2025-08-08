package com.payment.queque.paymentqueuelistner.config;

import com.payment.queque.paymentqueuelistner.model.Transaction;
import com.payment.queque.paymentqueuelistner.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Configuration class for initializing test data.
 * This class creates sample transaction records in the database for testing purposes.
 * It only runs in the "dev" profile to avoid populating the database in production.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
@Profile("dev")
public class DataInitializer {

    private final TransactionRepository transactionRepository;

    /**
     * Initializes the database with sample transaction data.
     * This method creates several transaction records with different processing statuses.
     *
     * @return a CommandLineRunner that executes the initialization
     */
    @Bean
    public CommandLineRunner initializeData() {
        return args -> {
            log.info("Initializing sample transaction data");

            // Create unprocessed transactions
            for (int i = 0; i < 5; i++) {
                Transaction transaction = Transaction.builder()
                        .id(UUID.randomUUID().toString())
                        .transactionId("TXN-" + (1000 + i))
                        .amount(100.0 + (i * 10))
                        .currency("USD")
                        .paymentMethod("CREDIT_CARD")
                        .status("COMPLETED")
                        .customerId("CUST-" + (2000 + i))
                        .merchantId("MERCH-" + (3000 + i))
                        .timestamp(LocalDateTime.now().minusMinutes(i))
                        .processingStatus("UNPROCESSED")
                        .build();
                
                transactionRepository.save(transaction);
                log.info("Created unprocessed transaction with ID: {}", transaction.getId());
            }

            // Create some already processed transactions
            for (int i = 0; i < 3; i++) {
                Transaction transaction = Transaction.builder()
                        .id(UUID.randomUUID().toString())
                        .transactionId("TXN-" + (2000 + i))
                        .amount(200.0 + (i * 20))
                        .currency("EUR")
                        .paymentMethod("BANK_TRANSFER")
                        .status("COMPLETED")
                        .customerId("CUST-" + (3000 + i))
                        .merchantId("MERCH-" + (4000 + i))
                        .timestamp(LocalDateTime.now().minusHours(i + 1))
                        .processingStatus("PROCESSED")
                        .processedTimestamp(LocalDateTime.now().minusMinutes(30))
                        .build();
                
                transactionRepository.save(transaction);
                log.info("Created processed transaction with ID: {}", transaction.getId());
            }

            log.info("Sample data initialization completed");
        };
    }
}