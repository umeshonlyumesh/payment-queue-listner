package com.payment.queque.paymentqueuelistner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for the Payment Queue Listener service.
 * This service listens to two AWS SQS queues for payment data,
 * enriches the data with additional information, and stores it in AWS DynamoDB.
 * It also monitors a transaction table for unprocessed records, sends them to SQS,
 * and marks them as processed.
 * 
 * Features:
 * - Batch processing of SQS messages
 * - Asynchronous processing using Java 21 virtual threads
 * - Data enrichment with additional information
 * - Storage in DynamoDB for persistence
 * - Transaction table monitoring and processing
 * - Scheduled tasks for polling the transaction table
 * - Error handling and logging
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class PaymentQueueListnerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentQueueListnerApplication.class, args);
    }

}
