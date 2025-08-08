package com.payment.queque.paymentqueuelistner.service;

import com.payment.queque.paymentqueuelistner.model.EnrichedPaymentData;
import com.payment.queque.paymentqueuelistner.model.PaymentData;
import com.payment.queque.paymentqueuelistner.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

/**
 * Service for enriching payment data with additional information.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEnrichmentService {

    private final PaymentRepository paymentRepository;
    private final ExecutorService virtualThreadExecutor;

    /**
     * Enriches payment data with additional information and stores it in DynamoDB.
     * This method uses virtual threads for parallel processing.
     *
     * @param paymentData the payment data to enrich
     * @return the enriched payment data
     */
    public EnrichedPaymentData enrichAndSavePaymentData(PaymentData paymentData) {
        long startTime = System.currentTimeMillis();
        log.info("Starting enrichment process for payment with ID: {}", paymentData.getId());

        try {
            // Create enriched payment data
            EnrichedPaymentData enrichedData = new EnrichedPaymentData();
            // Copy base fields from payment data
            enrichedData.setId(paymentData.getId());
            enrichedData.setTransactionId(paymentData.getTransactionId());
            enrichedData.setAmount(paymentData.getAmount());
            enrichedData.setCurrency(paymentData.getCurrency());
            enrichedData.setPaymentMethod(paymentData.getPaymentMethod());
            enrichedData.setStatus(paymentData.getStatus());
            enrichedData.setCustomerId(paymentData.getCustomerId());
            enrichedData.setMerchantId(paymentData.getMerchantId());
            enrichedData.setTimestamp(paymentData.getTimestamp());
            enrichedData.setSourceQueue(paymentData.getSourceQueue());

            // Set enrichment fields
            enrichedData.setEnrichmentId(UUID.randomUUID().toString());
            enrichedData.setAdditionalData(generateAdditionalData(paymentData));
            enrichedData.setRiskScore(calculateRiskScore(paymentData));
            enrichedData.setFraudStatus(determineFraudStatus(paymentData));
            enrichedData.setEnrichmentTimestamp(LocalDateTime.now());
            enrichedData.setProcessingStatus("COMPLETED");

            // Save to DynamoDB
            EnrichedPaymentData savedData = paymentRepository.save(enrichedData);

            // Calculate processing time
            long processingTime = System.currentTimeMillis() - startTime;
            savedData.setProcessingTimeMs(processingTime);

            log.info("Completed enrichment process for payment with ID: {} in {}ms", 
                    paymentData.getId(), processingTime);

            return savedData;
        } catch (Exception e) {
            log.error("Error enriching payment data: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to enrich payment data", e);
        }
    }

    /**
     * Processes payment data asynchronously using virtual threads.
     *
     * @param paymentData the payment data to process
     */
    public void processPaymentDataAsync(PaymentData paymentData) {
        virtualThreadExecutor.submit(() -> {
            try {
                enrichAndSavePaymentData(paymentData);
            } catch (Exception e) {
                log.error("Error in async processing of payment data: {}", e.getMessage(), e);
            }
        });
    }

    /**
     * Generates additional data for the payment based on the original payment data.
     * In a real-world scenario, this might involve calling external services or databases.
     *
     * @param paymentData the original payment data
     * @return a map of additional data
     */
    private Map<String, String> generateAdditionalData(PaymentData paymentData) {
        // Simulate some processing time
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Map<String, String> additionalData = new HashMap<>();
        additionalData.put("processingTimestamp", LocalDateTime.now().toString());
        additionalData.put("paymentChannel", determinePaymentChannel(paymentData));
        additionalData.put("customerCategory", determineCustomerCategory(paymentData.getCustomerId()));
        additionalData.put("merchantCategory", determineMerchantCategory(paymentData.getMerchantId()));

        return additionalData;
    }

    /**
     * Calculates a risk score for the payment.
     * This is a simplified example - in a real-world scenario, this would involve
     * complex risk assessment algorithms.
     *
     * @param paymentData the payment data
     * @return a risk score as a string
     */
    private String calculateRiskScore(PaymentData paymentData) {
        // Simulate some processing time
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Simple risk calculation based on amount
        double amount = paymentData.getAmount();
        if (amount > 1000) {
            return "HIGH";
        } else if (amount > 500) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    /**
     * Determines the fraud status of the payment.
     * This is a simplified example - in a real-world scenario, this would involve
     * complex fraud detection algorithms.
     *
     * @param paymentData the payment data
     * @return a fraud status as a string
     */
    private String determineFraudStatus(PaymentData paymentData) {
        // Simulate some processing time
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Simple fraud detection based on amount and currency
        double amount = paymentData.getAmount();
        String currency = paymentData.getCurrency();

        if (amount > 5000 && "USD".equals(currency)) {
            return "REVIEW_REQUIRED";
        } else if (amount > 10000) {
            return "SUSPICIOUS";
        } else {
            return "CLEAR";
        }
    }

    /**
     * Determines the payment channel based on the payment data.
     *
     * @param paymentData the payment data
     * @return the payment channel
     */
    private String determinePaymentChannel(PaymentData paymentData) {
        String paymentMethod = paymentData.getPaymentMethod();
        if (paymentMethod == null) {
            return "UNKNOWN";
        }

        switch (paymentMethod.toUpperCase()) {
            case "CREDIT_CARD":
            case "DEBIT_CARD":
                return "CARD";
            case "BANK_TRANSFER":
            case "ACH":
                return "BANK";
            case "PAYPAL":
            case "VENMO":
                return "DIGITAL_WALLET";
            default:
                return "OTHER";
        }
    }

    /**
     * Determines the customer category based on the customer ID.
     * In a real-world scenario, this would involve looking up customer information
     * in a database or calling a customer service.
     *
     * @param customerId the customer ID
     * @return the customer category
     */
    private String determineCustomerCategory(String customerId) {
        // Simulate some processing time
        try {
            Thread.sleep(30);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Simple customer categorization based on customer ID
        if (customerId == null) {
            return "UNKNOWN";
        }

        if (customerId.startsWith("VIP")) {
            return "VIP";
        } else if (customerId.startsWith("BIZ")) {
            return "BUSINESS";
        } else {
            return "REGULAR";
        }
    }

    /**
     * Determines the merchant category based on the merchant ID.
     * In a real-world scenario, this would involve looking up merchant information
     * in a database or calling a merchant service.
     *
     * @param merchantId the merchant ID
     * @return the merchant category
     */
    private String determineMerchantCategory(String merchantId) {
        // Simulate some processing time
        try {
            Thread.sleep(30);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Simple merchant categorization based on merchant ID
        if (merchantId == null) {
            return "UNKNOWN";
        }

        if (merchantId.startsWith("RETAIL")) {
            return "RETAIL";
        } else if (merchantId.startsWith("FOOD")) {
            return "FOOD_AND_BEVERAGE";
        } else if (merchantId.startsWith("TRAVEL")) {
            return "TRAVEL";
        } else {
            return "OTHER";
        }
    }
}
