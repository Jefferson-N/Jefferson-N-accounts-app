package com.core.bank.application.service;

import com.core.bank.domain.entity.Account;
import com.core.bank.domain.entity.Customer;
import com.core.bank.infrastructure.exception.BusinessRuleException;
import com.core.bank.model.dto.TransactionDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TransactionStrategyServiceTest {

    @InjectMocks
    private TransactionStrategyService transactionStrategyService;

    private Account account;
    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(UUID.randomUUID().toString());
        customer.setName("Jose Lema");

        account = new Account();
        account.setId(UUID.randomUUID().toString());
        account.setAccountNumber("478578");
        account.setAccountType("AHORRO");
        account.setCurrentBalance(new BigDecimal("2000.00"));
        account.setCustomer(customer);
    }

    @Test
    void testCreditTransactionAddsToBalance() {
        BigDecimal initialBalance = account.getCurrentBalance();
        BigDecimal creditAmount = new BigDecimal("600.00");

        BigDecimal newBalance = transactionStrategyService
                .getStrategy(TransactionDTO.TransactionTypeEnum.CREDITO.getValue())
                .apply(initialBalance, creditAmount);

        BigDecimal expected = new BigDecimal("2600.00");
        assertEquals(expected, newBalance);
    }

    @Test
    void testDebitTransactionSubtractsFromBalance() {
        BigDecimal initialBalance = account.getCurrentBalance();
        BigDecimal debitAmount = new BigDecimal("575.00");

        BigDecimal newBalance = transactionStrategyService
                .getStrategy(TransactionDTO.TransactionTypeEnum.DEBITO.getValue())
                .apply(initialBalance, debitAmount);

        BigDecimal expected = new BigDecimal("1425.00");
        assertEquals(expected, newBalance);
    }

    @Test
    void testDebitEqualToBalanceResultsInZero() {
        BigDecimal initialBalance = new BigDecimal("2000.00");
        BigDecimal debitAmount = new BigDecimal("2000.00");

        BigDecimal newBalance = transactionStrategyService
                .getStrategy(TransactionDTO.TransactionTypeEnum.DEBITO.getValue())
                .apply(initialBalance, debitAmount);

        assertEquals(new BigDecimal("0.00"), newBalance);
    }

    @Test
    void testMultipleCreditsAccumulate() {
        BigDecimal balance = new BigDecimal("1000.00");
        BigDecimal credit1 = new BigDecimal("100.00");
        BigDecimal credit2 = new BigDecimal("200.00");

        BigDecimal newBalance = transactionStrategyService
                .getStrategy(TransactionDTO.TransactionTypeEnum.CREDITO.getValue())
                .apply(balance, credit1);

        newBalance = transactionStrategyService
                .getStrategy(TransactionDTO.TransactionTypeEnum.CREDITO.getValue())
                .apply(newBalance, credit2);

        BigDecimal expected = new BigDecimal("1300.00");
        assertEquals(expected, newBalance);
    }

    @Test
    void testMixedTransactions() {
        BigDecimal balance = new BigDecimal("2000.00");

        balance = transactionStrategyService
                .getStrategy(TransactionDTO.TransactionTypeEnum.DEBITO.getValue())
                .apply(balance, new BigDecimal("575.00"));
        assertEquals(new BigDecimal("1425.00"), balance);

        balance = transactionStrategyService
                .getStrategy(TransactionDTO.TransactionTypeEnum.CREDITO.getValue())
                .apply(balance, new BigDecimal("600.00"));
        assertEquals(new BigDecimal("2025.00"), balance);

        balance = transactionStrategyService
                .getStrategy(TransactionDTO.TransactionTypeEnum.DEBITO.getValue())
                .apply(balance, new BigDecimal("150.00"));
        assertEquals(new BigDecimal("1875.00"), balance);
    }

    @Test
    void testPrecisionWithDecimals() {
        BigDecimal initialBalance = new BigDecimal("1500.50");
        BigDecimal creditAmount = new BigDecimal("250.75");

        BigDecimal newBalance = transactionStrategyService
                .getStrategy(TransactionDTO.TransactionTypeEnum.CREDITO.getValue())
                .apply(initialBalance, creditAmount);

        BigDecimal expected = new BigDecimal("1751.25");
        assertEquals(expected, newBalance);
    }

    @Test
    void testInvalidTransactionType() {
        assertThrows(IllegalArgumentException.class, () -> {
            transactionStrategyService.getStrategy("INVALID");
        });
    }
}
