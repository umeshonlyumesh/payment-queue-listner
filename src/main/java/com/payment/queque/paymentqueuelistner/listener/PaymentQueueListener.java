package com.payment.queque.paymentqueuelistner.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.queque.paymentqueuelistner.model.PaymentData;
import com.payment.queque.paymentqueuelistner.service.PaymentEnrichmentService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Listener for SQS queues that processes payment messages.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentQueueListener {

    private final PaymentEnrichmentService paymentEnrichmentService;
    private final ObjectMapper objectMapper;

    @Value("${aws.sqs.queue1.url}")
    private String queue1Url;

    @Value("${aws.sqs.queue2.url}")
    private String queue2Url;

    /**
     * Listens to the first SQS queue and processes payment messages in batches.
     *
     * @param payloads the message payloads
     * @param messageIds the message IDs
     */
    @SqsListener("${aws.sqs.queue1.url}")
    public void receiveQueue1Messages(@Payload List<String> payloads, @Header("MessageId") List<String> messageIds) {
        log.info("Received batch of {} messages from queue1", payloads.size());

        for (int i = 0; i < payloads.size(); i++) {
            String payload = payloads.get(i);
            String messageId = messageIds.get(i);

            try {
                PaymentData paymentData = parsePaymentData(payload);
                paymentData.setSourceQueue("queue1");

                log.info("Processing payment from queue1 with ID: {}, MessageID: {}", 
                        paymentData.getId(), messageId);

                // Process asynchronously using virtual threads
                paymentEnrichmentService.processPaymentDataAsync(paymentData);
            } catch (Exception e) {
                log.error("Error processing message from queue1: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Listens to the second SQS queue and processes payment messages in batches.
     *
     * @param payloads the message payloads
     * @param messageIds the message IDs
     */
    @SqsListener("${aws.sqs.queue2.url}")
    public void receiveQueue2Messages(@Payload List<String> payloads, @Header("MessageId") List<String> messageIds) {
        log.info("Received batch of {} messages from queue2", payloads.size());

        for (int i = 0; i < payloads.size(); i++) {
            String payload = payloads.get(i);
            String messageId = messageIds.get(i);

            try {
                PaymentData paymentData = parsePaymentData(payload);
                paymentData.setSourceQueue("queue2");

                log.info("Processing payment from queue2 with ID: {}, MessageID: {}", 
                        paymentData.getId(), messageId);

                // Process asynchronously using virtual threads
                paymentEnrichmentService.processPaymentDataAsync(paymentData);
            } catch (Exception e) {
                log.error("Error processing message from queue2: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Parses a JSON payload into a PaymentData object.
     * If the payload is invalid or missing required fields, it creates a default PaymentData object.
     *
     * @param payload the JSON payload
     * @return the parsed PaymentData object
     */
    private PaymentData parsePaymentData(String payload) {
        try {
            PaymentData paymentData = objectMapper.readValue(payload, PaymentData.class);

            // If ID is missing, generate one
            if (paymentData.getId() == null || paymentData.getId().isEmpty()) {
                paymentData.setId(UUID.randomUUID().toString());
            }

            // If timestamp is missing, set current time
            if (paymentData.getTimestamp() == null) {
                paymentData.setTimestamp(LocalDateTime.now());
            }

            return paymentData;
        } catch (JsonProcessingException e) {
            log.error("Error parsing payment data: {}", e.getMessage(), e);

            // Create a default payment data object for error cases
            return PaymentData.builder()
                    .id(UUID.randomUUID().toString())
                    .transactionId("ERROR-" + UUID.randomUUID().toString())
                    .status("ERROR")
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }
}
