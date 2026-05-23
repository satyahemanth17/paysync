package com.paysync.notification.listener;

import com.paysync.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);

    private final NotificationService notificationService;

    public NotificationListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(
        topics = "transaction-events",
        groupId = "notification-service-group"
    )
    public void onTransactionEvent(TransactionEvent event) {
        log.info("Received transaction event: {}", event.getId());
        try {
            notificationService.processTransactionEvent(event);
        } catch (Exception e) {
            log.error("Failed to process notification for transaction {}: {}",
                    event.getId(), e.getMessage());
        }
    }
}
