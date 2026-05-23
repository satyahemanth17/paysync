package com.paysync.kafka;

import com.paysync.dto.TransactionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class TransactionProducer {

    private static final Logger log = LoggerFactory.getLogger(TransactionProducer.class);
    private static final String TOPIC = "transaction-events";

    private final KafkaTemplate<String, TransactionResponse> kafkaTemplate;

    public TransactionProducer(KafkaTemplate<String, TransactionResponse> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishTransaction(TransactionResponse transaction) {
        log.info("Publishing transaction {} to topic {}", transaction.getId(), TOPIC);
        kafkaTemplate.send(TOPIC, transaction.getId(), transaction);
    }
}
