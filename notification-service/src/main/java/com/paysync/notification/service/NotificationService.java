package com.paysync.notification.service;

import com.paysync.notification.listener.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public void processTransactionEvent(TransactionEvent event) {
        log.info("Processing notification for transaction: id={}, status={}, sender={}, amount={} {}",
                event.getId(), event.getStatus(), event.getSenderId(),
                event.getAmount(), event.getCurrency());
        sendNotification(event);
    }

    private void sendNotification(TransactionEvent event) {
        switch (event.getStatus()) {
            case "COMPLETED" ->
                log.info("SUCCESS notification: Transaction {} completed for sender {}",
                        event.getId(), event.getSenderId());
            case "FAILED" ->
                log.warn("FAILURE notification: Transaction {} failed for sender {}",
                        event.getId(), event.getSenderId());
            case "PROCESSING" ->
                log.info("PROCESSING notification: Transaction {} is being processed", event.getId());
            default ->
                log.debug("Status update for transaction {}: {}", event.getId(), event.getStatus());
        }
    }
}
