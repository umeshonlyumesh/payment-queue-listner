package com.payment.queque.paymentqueuelistner.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Base model class for payment data received from SQS queues.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentData {
    private String id;
    private String transactionId;
    private Double amount;
    private String currency;
    private String paymentMethod;
    private String status;
    private String customerId;
    private String merchantId;
    private LocalDateTime timestamp;
    private String sourceQueue;
    
    // Additional fields can be added as needed
}