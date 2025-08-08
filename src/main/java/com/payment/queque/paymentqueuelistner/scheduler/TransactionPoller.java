package com.payment.queque.paymentqueuelistner.scheduler;

import com.payment.queque.paymentqueuelistner.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled task that polls the transaction table for unprocessed records.
 * This component runs at regular intervals to check for new transactions,
 * process them, and send them to SQS.
 */
@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class TransactionPoller {

    private final TransactionService transactionService;
    
    @Value("${app.transaction-poller.enabled:true}")
    private boolean pollerEnabled;

    /**
     * Polls the transaction table for unprocessed records at regular intervals.
     * The polling interval is configured using a cron expression.
     * By default, it runs every 10 seconds.
     */
    @Scheduled(cron = "${app.transaction-poller.cron:*/10 * * * * *}")
    public void pollTransactions() {
        if (!pollerEnabled) {
            log.debug("Transaction poller is disabled");
            return;
        }
        
        log.info("Starting transaction polling");
        try {
            int processedCount = transactionService.processUnprocessedTransactions();
            log.info("Transaction polling completed. Processed {} transactions", processedCount);
        } catch (Exception e) {
            log.error("Error during transaction polling: {}", e.getMessage(), e);
        }
    }
}