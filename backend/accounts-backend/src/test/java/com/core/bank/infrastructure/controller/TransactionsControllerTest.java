package com.core.bank.infrastructure.controller;

import com.core.bank.application.service.TransactionService;
import com.core.bank.application.mapper.TransactionMapper;
import com.core.bank.infrastructure.exception.BusinessRuleException;
import com.core.bank.model.dto.TransactionCreate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


@WebMvcTest(TransactionsController.class)
class TransactionsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private TransactionMapper transactionMapper;

    @Test
    @WithMockUser(username = "test", roles = {"USER"})
    void testCreateDebitTransactionWithInsufficientBalanceEndpoint() throws Exception {
        TransactionCreate transactionCreate = new TransactionCreate();
        transactionCreate.setAccountId(UUID.randomUUID());
        transactionCreate.setTransactionType(TransactionCreate.TransactionTypeEnum.DEBITO);
        transactionCreate.setAmount(500.0);
        transactionCreate.setDescription("Test transaction");

        when(transactionService.create(any()))
            .thenThrow(new BusinessRuleException("Saldo no disponible"));

        mockMvc.perform(post("/api/movimientos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transactionCreate))
                .with(csrf()))
            .andExpect(status().isBadRequest());
    }
}
