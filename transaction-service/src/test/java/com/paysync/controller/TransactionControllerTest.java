package com.paysync.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paysync.dto.TransactionRequest;
import com.paysync.dto.TransactionResponse;
import com.paysync.exception.TransactionNotFoundException;
import com.paysync.model.TransactionStatus;
import com.paysync.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    @Test
    @WithMockUser
    void createTransaction_validInput_returns201() throws Exception {
        TransactionRequest request = TransactionRequest.builder()
                .senderId("sender-1")
                .receiverId("receiver-1")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .build();

        TransactionResponse response = TransactionResponse.builder()
                .id("txn-123")
                .senderId("sender-1")
                .receiverId("receiver-1")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .status(TransactionStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        when(transactionService.createTransaction(any(TransactionRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/transactions")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("txn-123"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser
    void createTransaction_invalidAmount_returns400() throws Exception {
        TransactionRequest request = TransactionRequest.builder()
                .senderId("sender-1")
                .receiverId("receiver-1")
                .amount(new BigDecimal("-1.00"))
                .currency("USD")
                .build();

        mockMvc.perform(post("/api/transactions")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void getTransaction_notFound_returns404() throws Exception {
        when(transactionService.getTransaction("nonexistent"))
                .thenThrow(new TransactionNotFoundException("nonexistent"));

        mockMvc.perform(get("/api/transactions/nonexistent"))
                .andExpect(status().isNotFound());
    }
}
