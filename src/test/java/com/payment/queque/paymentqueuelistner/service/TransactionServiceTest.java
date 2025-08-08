package com.payment.queque.paymentqueuelistner.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.queque.paymentqueuelistner.model.Transaction;
import com.payment.queque.paymentqueuelistner.repository.TransactionRepository;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private SqsTemplate sqsTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ExecutorService executorService;

    @InjectMocks
    private TransactionService transactionService;

    private Transaction transaction1;
    private Transaction transaction2;
    private List<Transaction> transactions;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        // Create test transactions
        transaction1 = Transaction.builder()
                .id(UUID.randomUUID().toString())
                .transactionId(UUID.randomUUID().toString())
                .amount(1000.0)
                .currency("USD")
                .paymentMethod("CREDIT_CARD")
                .status("COMPLETED")
                .customerId("CUST123")
                .merchantId("MERCH456")
                .timestamp(LocalDateTime.now())
                .processingStatus("UNPROCESSED")
                .build();

        transaction2 = Transaction.builder()
                .id(UUID.randomUUID().toString())
                .transactionId(UUID.randomUUID().toString())
                .amount(500.0)
                .currency("EUR")
                .paymentMethod("DEBIT_CARD")
                .status("COMPLETED")
                .customerId("CUST789")
                .merchantId("MERCH012")
                .timestamp(LocalDateTime.now())
                .processingStatus("UNPROCESSED")
                .build();

        transactions = Arrays.asList(transaction1, transaction2);

        // Setup mocks
        when(transactionRepository.findByProcessingStatus("UNPROCESSED")).thenReturn(transactions);
        when(objectMapper.writeValueAsString(any(Transaction.class))).thenReturn("{}");
        when(transactionRepository.updateProcessingStatus(anyString(), eq("PROCESSED"), any(LocalDateTime.class))).thenReturn(1);

        // Setup CompletableFuture.supplyAsync to execute immediately with the provided executor
        doAnswer(invocation -> {
            return CompletableFuture.completedFuture(true);
        }).when(executorService).execute(any(Runnable.class));
    }

    @Test
    void processUnprocessedTransactions_ShouldProcessAllTransactions() {
        // Act
        int result = transactionService.processUnprocessedTransactions();

        // Assert
        assertEquals(2, result);
        verify(transactionRepository, times(1)).findByProcessingStatus("UNPROCESSED");
        verify(executorService, times(2)).execute(any(Runnable.class));
    }

    @Test
    void processUnprocessedTransactions_ShouldHandleEmptyList() {
        // Setup
        when(transactionRepository.findByProcessingStatus("UNPROCESSED")).thenReturn(Collections.emptyList());

        // Act
        int result = transactionService.processUnprocessedTransactions();

        // Assert
        assertEquals(0, result);
        verify(transactionRepository, times(1)).findByProcessingStatus("UNPROCESSED");
        verify(executorService, never()).execute(any(Runnable.class));
    }

    @Test
    void processUnprocessedTransactions_ShouldHandleFailures() throws JsonProcessingException {
        // Setup
        when(transactionRepository.updateProcessingStatus(eq(transaction1.getId()), eq("PROCESSED"), any(LocalDateTime.class))).thenReturn(1);
        when(transactionRepository.updateProcessingStatus(eq(transaction2.getId()), eq("PROCESSED"), any(LocalDateTime.class))).thenReturn(0);

        // Setup CompletableFuture behavior for success and failure
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(executorService).execute(any(Runnable.class));

        // Act
        int result = transactionService.processUnprocessedTransactions();

        // Assert
        assertEquals(1, result);
        verify(transactionRepository, times(1)).findByProcessingStatus("UNPROCESSED");
        verify(sqsTemplate, times(2)).send(anyString(), anyString());
        verify(transactionRepository, times(2)).updateProcessingStatus(anyString(), eq("PROCESSED"), any(LocalDateTime.class));
    }

    @Test
    void processUnprocessedTransactions_ShouldUseNativeQueryWhenConfigured() {
        // Setup
        when(transactionRepository.findByProcessingStatusNative("UNPROCESSED")).thenReturn(transactions);

        // Set useNativeQuery to true using reflection
        try {
            java.lang.reflect.Field field = TransactionService.class.getDeclaredField("useNativeQuery");
            field.setAccessible(true);
            field.set(transactionService, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Act
        int result = transactionService.processUnprocessedTransactions();

        // Assert
        assertEquals(2, result);
        verify(transactionRepository, times(1)).findByProcessingStatusNative("UNPROCESSED");
        verify(transactionRepository, never()).findByProcessingStatus("UNPROCESSED");
    }
}
