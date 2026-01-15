package com.core.bank.application.service;

import com.core.bank.domain.entity.Account;
import com.core.bank.domain.entity.Customer;
import com.core.bank.domain.repository.AccountRepository;
import com.core.bank.domain.repository.CustomerRepository;
import com.core.bank.infrastructure.exception.ResourceNotFoundException;
import com.core.bank.infrastructure.exception.ResourceAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountValidationServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private AccountValidationService accountValidationService;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(UUID.randomUUID().toString());
        customer.setName("Jose Lema");
        customer.setStatus(true);
    }

    @Test
    void testValidateAccountNumberNotExists() {
        String accountNumber = "478578";
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> accountValidationService.validateAccountNumberNotExists(accountNumber));
    }

    @Test
    void testValidateAccountNumberAlreadyExists() {
        String accountNumber = "478578";
        Account existingAccount = new Account();
        existingAccount.setAccountNumber(accountNumber);

        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(existingAccount));

        ResourceAlreadyExistsException exception = assertThrows(ResourceAlreadyExistsException.class,
                () -> accountValidationService.validateAccountNumberNotExists(accountNumber));

        assertTrue(exception.getMessage().contains("accountNumber"));
    }

    @Test
    void testValidateAndGetExistingActiveCustomer() {
        String customerId = customer.getId();
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        Customer retrievedCustomer = accountValidationService.validateAndGetCustomer(customerId);

        assertNotNull(retrievedCustomer);
        assertEquals(customer.getId(), retrievedCustomer.getId());
        assertEquals("Jose Lema", retrievedCustomer.getName());
    }

    @Test
    void testValidateCustomerNotFound() {
        String customerId = "non-existent-id";
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> accountValidationService.validateAndGetCustomer(customerId));

        assertTrue(exception.getMessage().contains("Customer"));
    }

    @Test
    void testValidateInactiveCustomer() {
        String customerId = customer.getId();
        customer.setStatus(false);
        when(customerRepository.findById(anyString())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> accountValidationService.validateAndGetCustomer(customerId));

        assertTrue(exception.getMessage().contains("Customer"));
    }

    @Test
    void testValidateMultipleDifferentAccountNumbers() {
        when(accountRepository.findByAccountNumber("478578")).thenReturn(Optional.empty());
        when(accountRepository.findByAccountNumber("225487")).thenReturn(Optional.empty());
        when(accountRepository.findByAccountNumber("452578")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> accountValidationService.validateAccountNumberNotExists("478578"));
        assertDoesNotThrow(() -> accountValidationService.validateAccountNumberNotExists("225487"));
        assertDoesNotThrow(() -> accountValidationService.validateAccountNumberNotExists("452578"));
    }
}
