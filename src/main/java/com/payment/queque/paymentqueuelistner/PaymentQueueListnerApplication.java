package com.payment.queque.paymentqueuelistner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main application class for the Payment Queue Listener service.
 * This service listens to two AWS SQS queues for payment data,
 * enriches the data with additional information, and stores it in AWS DynamoDB.
 * 
 * Features:
 * - Batch processing of SQS messages
 * - Asynchronous processing using Java 21 virtual threads
 * - Data enrichment with additional information
 * - Storage in DynamoDB for persistence
 * - Error handling and logging
 */
@SpringBootApplication
@EnableAsync
public class PaymentQueueListnerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentQueueListnerApplication.class, args);
    }

}
