package com.payment.queque.paymentqueuelistner.service;

import com.payment.queque.paymentqueuelistner.model.EnrichedPaymentData;
import com.payment.queque.paymentqueuelistner.model.PaymentData;
import com.payment.queque.paymentqueuelistner.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentEnrichmentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ExecutorService virtualThreadExecutor;

    @InjectMocks
    private PaymentEnrichmentService paymentEnrichmentService;

    private PaymentData testPaymentData;

    @BeforeEach
    void setUp() {
        // Create test payment data
        testPaymentData = PaymentData.builder()
                .id(UUID.randomUUID().toString())
                .transactionId(UUID.randomUUID().toString())
                .amount(1000.0)
                .currency("USD")
                .paymentMethod("CREDIT_CARD")
                .status("PENDING")
                .customerId("CUST123")
                .merchantId("MERCH456")
                .timestamp(LocalDateTime.now())
                .sourceQueue("test-queue")
                .build();
        
        // Setup repository mock
        when(paymentRepository.save(any(EnrichedPaymentData.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void enrichAndSavePaymentData_ShouldEnrichAndSaveData() {
        // Act
        EnrichedPaymentData result = paymentEnrichmentService.enrichAndSavePaymentData(testPaymentData);

        // Assert
        assertNotNull(result);
        assertEquals(testPaymentData.getId(), result.getId());
        assertEquals(testPaymentData.getTransactionId(), result.getTransactionId());
        assertEquals(testPaymentData.getAmount(), result.getAmount());
        assertEquals(testPaymentData.getCurrency(), result.getCurrency());
        assertEquals(testPaymentData.getPaymentMethod(), result.getPaymentMethod());
        
        // Verify enrichment fields
        assertNotNull(result.getEnrichmentId());
        assertNotNull(result.getAdditionalData());
        assertNotNull(result.getRiskScore());
        assertNotNull(result.getFraudStatus());
        assertNotNull(result.getEnrichmentTimestamp());
        assertEquals("COMPLETED", result.getProcessingStatus());
        
        // Verify repository was called
        verify(paymentRepository, times(1)).save(any(EnrichedPaymentData.class));
    }

    @Test
    void processPaymentDataAsync_ShouldSubmitTaskToExecutor() {
        // Setup
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(virtualThreadExecutor).submit(any(Runnable.class));

        // Act
        paymentEnrichmentService.processPaymentDataAsync(testPaymentData);

        // Assert
        verify(virtualThreadExecutor, times(1)).submit(any(Runnable.class));
        verify(paymentRepository, times(1)).save(any(EnrichedPaymentData.class));
    }

    @Test
    void calculateRiskScore_ShouldReturnHighForLargeAmount() {
        // Setup
        testPaymentData.setAmount(1500.0);
        
        // Act
        EnrichedPaymentData result = paymentEnrichmentService.enrichAndSavePaymentData(testPaymentData);
        
        // Assert
        assertEquals("HIGH", result.getRiskScore());
    }

    @Test
    void calculateRiskScore_ShouldReturnMediumForMediumAmount() {
        // Setup
        testPaymentData.setAmount(750.0);
        
        // Act
        EnrichedPaymentData result = paymentEnrichmentService.enrichAndSavePaymentData(testPaymentData);
        
        // Assert
        assertEquals("MEDIUM", result.getRiskScore());
    }

    @Test
    void calculateRiskScore_ShouldReturnLowForSmallAmount() {
        // Setup
        testPaymentData.setAmount(100.0);
        
        // Act
        EnrichedPaymentData result = paymentEnrichmentService.enrichAndSavePaymentData(testPaymentData);
        
        // Assert
        assertEquals("LOW", result.getRiskScore());
    }
}