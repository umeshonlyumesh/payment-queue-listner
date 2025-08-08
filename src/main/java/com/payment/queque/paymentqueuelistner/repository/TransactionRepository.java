package com.payment.queque.paymentqueuelistner.repository;

import com.payment.queque.paymentqueuelistner.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for accessing transaction data in the database.
 * This repository provides methods for finding unprocessed transactions and updating their status.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    /**
     * Finds all transactions with the specified processing status.
     * This method uses JPA's method naming convention for query generation.
     *
     * @param processingStatus the processing status to search for
     * @return a list of transactions with the specified processing status
     */
    List<Transaction> findByProcessingStatus(String processingStatus);

    /**
     * Finds all transactions with the specified processing status using a native SQL query.
     * This method may provide better performance for large datasets.
     *
     * @param processingStatus the processing status to search for
     * @return a list of transactions with the specified processing status
     */
    @Query(value = "SELECT * FROM transactions WHERE processing_status = :processingStatus", nativeQuery = true)
    List<Transaction> findByProcessingStatusNative(@Param("processingStatus") String processingStatus);

    /**
     * Updates the processing status and processed timestamp of a transaction.
     *
     * @param id the ID of the transaction to update
     * @param processingStatus the new processing status
     * @param processedTimestamp the timestamp when the transaction was processed
     * @return the number of rows affected
     */
    @Modifying
    @Query("UPDATE Transaction t SET t.processingStatus = :processingStatus, t.processedTimestamp = :processedTimestamp WHERE t.id = :id")
    int updateProcessingStatus(@Param("id") String id, @Param("processingStatus") String processingStatus, @Param("processedTimestamp") LocalDateTime processedTimestamp);
}
