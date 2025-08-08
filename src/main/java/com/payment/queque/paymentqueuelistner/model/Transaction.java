package com.payment.queque.paymentqueuelistner.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Entity class representing a transaction record in the database.
 * This table is updated by external applications and monitored by this application.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    private String id;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "currency")
    private String currency;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "status")
    private String status;

    @Column(name = "customer_id")
    private String customerId;

    @Column(name = "merchant_id")
    private String merchantId;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "processing_status")
    private String processingStatus;

    @Column(name = "processed_timestamp")
    private LocalDateTime processedTimestamp;
}
