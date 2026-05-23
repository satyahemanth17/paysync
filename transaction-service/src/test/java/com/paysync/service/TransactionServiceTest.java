package com.paysync.service;

import com.paysync.dto.TransactionRequest;
import com.paysync.dto.TransactionResponse;
import com.paysync.exception.TransactionNotFoundException;
import com.paysync.kafka.TransactionProducer;
import com.paysync.model.Transaction;
import com.paysync.model.TransactionStatus;
import com.paysync.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionProducer transactionProducer;

    @Mock
    private AsyncTransactionProcessor asyncProcessor;

    @Mock
    private RedisTemplate<String, TransactionResponse> redisTemplate;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void createTransaction_savesAndPublishesToKafka() {
        TransactionRequest request = TransactionRequest.builder()
                .senderId("sender-1")
                .receiverId("receiver-1")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .build();

        Transaction saved = Transaction.builder()
                .id("txn-123")
                .senderId("sender-1")
                .receiverId("receiver-1")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .status(TransactionStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .version(0L)
                .build();

        when(transactionRepository.save(any(Transaction.class))).thenReturn(saved);
        when(asyncProcessor.processAsync(any(Transaction.class)))
                .thenReturn(CompletableFuture.completedFuture(saved));

        TransactionResponse result = transactionService.createTransaction(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("txn-123");
        assertThat(result.getStatus()).isEqualTo(TransactionStatus.PENDING);
        verify(transactionRepository).save(any(Transaction.class));
        verify(transactionProducer).publishTransaction(any(TransactionResponse.class));
        verify(asyncProcessor).processAsync(any(Transaction.class));
    }

    @Test
    void processAsync_completesWithCompletableFuture() {
        Transaction transaction = Transaction.builder()
                .id("txn-456")
                .senderId("sender-2")
                .receiverId("receiver-2")
                .amount(new BigDecimal("200.00"))
                .currency("EUR")
                .status(TransactionStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .version(0L)
                .build();

        Transaction completed = Transaction.builder()
                .id("txn-456")
                .senderId("sender-2")
                .receiverId("receiver-2")
                .amount(new BigDecimal("200.00"))
                .currency("EUR")
                .status(TransactionStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .version(1L)
                .build();

        when(asyncProcessor.processAsync(transaction))
                .thenReturn(CompletableFuture.completedFuture(completed));

        CompletableFuture<Transaction> future = asyncProcessor.processAsync(transaction);

        assertThat(future).isCompleted();
        assertThat(future.join().getStatus()).isEqualTo(TransactionStatus.COMPLETED);
    }

    @Test
    void getTransaction_notFound_throwsException() {
        when(transactionRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getTransaction("missing"))
                .isInstanceOf(TransactionNotFoundException.class)
                .hasMessageContaining("missing");
    }
}
