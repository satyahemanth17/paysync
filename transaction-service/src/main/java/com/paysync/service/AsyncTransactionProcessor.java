package com.paysync.service;

import com.paysync.model.Transaction;
import com.paysync.model.TransactionStatus;
import com.paysync.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;

@Service
public class AsyncTransactionProcessor {

    private static final Logger log = LoggerFactory.getLogger(AsyncTransactionProcessor.class);

    private final TransactionRepository transactionRepository;

    public AsyncTransactionProcessor(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Async("taskExecutor")
    public CompletableFuture<Transaction> processAsync(Transaction transaction) {
        log.info("Processing transaction {} asynchronously", transaction.getId());
        try {
            transaction.setStatus(TransactionStatus.PROCESSING);
            transactionRepository.save(transaction);
            Thread.sleep(100);
            transaction.setStatus(TransactionStatus.COMPLETED);
            Transaction saved = transactionRepository.save(transaction);
            log.info("Transaction {} completed", transaction.getId());
            return CompletableFuture.completedFuture(saved);
        } catch (Exception e) {
            log.error("Failed to process transaction {}: {}", transaction.getId(), e.getMessage());
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            return CompletableFuture.failedFuture(e);
        }
    }
}
