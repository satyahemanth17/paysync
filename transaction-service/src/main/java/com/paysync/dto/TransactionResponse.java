package com.paysync.dto;

import com.paysync.model.TransactionStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponse {

    private String id;
    private String senderId;
    private String receiverId;
    private BigDecimal amount;
    private String currency;
    private TransactionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TransactionResponse() {}

    private TransactionResponse(Builder b) {
        this.id = b.id;
        this.senderId = b.senderId;
        this.receiverId = b.receiverId;
        this.amount = b.amount;
        this.currency = b.currency;
        this.status = b.status;
        this.createdAt = b.createdAt;
        this.updatedAt = b.updatedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String id;
        private String senderId;
        private String receiverId;
        private BigDecimal amount;
        private String currency;
        private TransactionStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder senderId(String s) { this.senderId = s; return this; }
        public Builder receiverId(String r) { this.receiverId = r; return this; }
        public Builder amount(BigDecimal a) { this.amount = a; return this; }
        public Builder currency(String c) { this.currency = c; return this; }
        public Builder status(TransactionStatus s) { this.status = s; return this; }
        public Builder createdAt(LocalDateTime t) { this.createdAt = t; return this; }
        public Builder updatedAt(LocalDateTime t) { this.updatedAt = t; return this; }
        public TransactionResponse build() { return new TransactionResponse(this); }
    }
}
