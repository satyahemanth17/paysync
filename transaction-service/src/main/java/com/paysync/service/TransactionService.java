package com.paysync.service;

import com.paysync.dto.TransactionRequest;
import com.paysync.dto.TransactionResponse;
import com.paysync.exception.TransactionNotFoundException;
import com.paysync.kafka.TransactionProducer;
import com.paysync.model.Transaction;
import com.paysync.model.TransactionStatus;
import com.paysync.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final TransactionProducer transactionProducer;
    private final AsyncTransactionProcessor asyncProcessor;
    private final RedisTemplate<String, TransactionResponse> redisTemplate;

    public TransactionService(TransactionRepository transactionRepository,
                              TransactionProducer transactionProducer,
                              AsyncTransactionProcessor asyncProcessor,
                              RedisTemplate<String, TransactionResponse> redisTemplate) {
        this.transactionRepository = transactionRepository;
        this.transactionProducer = transactionProducer;
        this.asyncProcessor = asyncProcessor;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        Transaction transaction = Transaction.builder()
                .senderId(request.getSenderId())
                .receiverId(request.getReceiverId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(TransactionStatus.PENDING)
                .build();

        Transaction saved = transactionRepository.save(transaction);
        TransactionResponse response = toResponse(saved);
        transactionProducer.publishTransaction(response);
        asyncProcessor.processAsync(saved);
        log.info("Created transaction {}", saved.getId());
        return response;
    }

    @Cacheable(value = "transactions", key = "#id")
    public TransactionResponse getTransaction(String id) {
        return transactionRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new TransactionNotFoundException(id));
    }

    private TransactionResponse toResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .senderId(t.getSenderId())
                .receiverId(t.getReceiverId())
                .amount(t.getAmount())
                .currency(t.getCurrency())
                .status(t.getStatus())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}
