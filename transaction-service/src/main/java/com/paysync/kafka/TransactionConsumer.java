package com.paysync.kafka;

import com.paysync.dto.TransactionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransactionConsumer.class);

    @KafkaListener(topics = "transaction-events", groupId = "transaction-service-group")
    public void consume(TransactionResponse transaction) {
        log.info("Received transaction event: id={}, status={}", transaction.getId(), transaction.getStatus());
    }
}
