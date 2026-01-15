package com.core.bank.application.service;

import com.core.bank.domain.entity.Account;
import com.core.bank.domain.entity.Customer;
import com.core.bank.domain.repository.TransactionRepository;
import com.core.bank.infrastructure.exception.BusinessRuleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class DebitValidationServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private DebitValidationService debitValidationService;

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

        ReflectionTestUtils.setField(debitValidationService, "dailyWithdrawalLimit", 1000.0);
    }

    @Test
    void testValidateSuccessfulDebit() {
        BigDecimal debitAmount = new BigDecimal("100.00");
        when(transactionRepository.sumDebitsForDay(eq(account.getId()), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.ZERO);

        assertDoesNotThrow(() -> debitValidationService.validate(account, debitAmount));
    }

    @Test
    void testValidateZeroBalance() {
        account.setCurrentBalance(BigDecimal.ZERO);
        BigDecimal debitAmount = new BigDecimal("100.00");

        BusinessRuleException exception = assertThrows(BusinessRuleException.class,
                () -> debitValidationService.validate(account, debitAmount));

        assertEquals("Saldo no disponible", exception.getMessage());
    }

    @Test
    void testValidateNegativeBalance() {
        account.setCurrentBalance(new BigDecimal("-100.00"));
        BigDecimal debitAmount = new BigDecimal("100.00");

        BusinessRuleException exception = assertThrows(BusinessRuleException.class,
                () -> debitValidationService.validate(account, debitAmount));

        assertEquals("Saldo no disponible", exception.getMessage());
    }

    @Test
    void testValidateInsufficientBalance() {
        account.setCurrentBalance(new BigDecimal("500.00"));
        BigDecimal debitAmount = new BigDecimal("600.00");

    }

    @Test
    void testValidateDailyWithdrawalLimitExceeded() {
        account.setCurrentBalance(new BigDecimal("2000.00"));
        BigDecimal debitAmount = new BigDecimal("100.00");
        BigDecimal dailyDebits = new BigDecimal("950.00");

        when(transactionRepository.sumDebitsForDay(eq(account.getId()), any(LocalDateTime.class)))
                .thenReturn(dailyDebits);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class,
                () -> debitValidationService.validate(account, debitAmount));

        assertEquals("Cupo diario Excedido", exception.getMessage());
    }

    @Test
    void testValidateAtExactlyDailyLimit() {
        account.setCurrentBalance(new BigDecimal("2000.00"));
        BigDecimal debitAmount = new BigDecimal("1000.00");
        when(transactionRepository.sumDebitsForDay(eq(account.getId()), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.ZERO);

        assertDoesNotThrow(() -> debitValidationService.validate(account, debitAmount));
    }

    @Test
    void testValidateMultipleDebitsUnderLimit() {
        account.setCurrentBalance(new BigDecimal("2000.00"));
        BigDecimal debitAmount = new BigDecimal("400.00");
        BigDecimal dailyDebits = new BigDecimal("500.00");

        when(transactionRepository.sumDebitsForDay(eq(account.getId()), any(LocalDateTime.class)))
                .thenReturn(dailyDebits);

        assertDoesNotThrow(() -> debitValidationService.validate(account, debitAmount));
    }
}
