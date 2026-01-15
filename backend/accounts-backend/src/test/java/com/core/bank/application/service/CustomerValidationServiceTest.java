package com.core.bank.application.service;

import com.core.bank.domain.entity.Customer;
import com.core.bank.domain.repository.CustomerRepository;
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
class CustomerValidationServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerValidationService customerValidationService;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(UUID.randomUUID().toString());
        customer.setName("Jose Lema");
        customer.setIdentification("1234567890");
        customer.setStatus(true);
    }

    @Test
    void testValidateIdentificationNotExists() {
        String identification = "1234567890";
        when(customerRepository.findByIdentification(identification)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> customerValidationService.validateIdentificationNotExists(identification));
    }

    @Test
    void testValidateIdentificationAlreadyExists() {
        String identification = "1234567890";
        Customer existingCustomer = new Customer();
        existingCustomer.setIdentification(identification);

        when(customerRepository.findByIdentification(identification)).thenReturn(Optional.of(existingCustomer));

        ResourceAlreadyExistsException exception = assertThrows(ResourceAlreadyExistsException.class,
                () -> customerValidationService.validateIdentificationNotExists(identification));

        assertTrue(exception.getMessage().contains("identification"));
    }

    @Test
    void testValidateMultipleDifferentIdentifications() {
        when(customerRepository.findByIdentification("1234567890")).thenReturn(Optional.empty());
        when(customerRepository.findByIdentification("0987654321")).thenReturn(Optional.empty());
        when(customerRepository.findByIdentification("1122334455")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> customerValidationService.validateIdentificationNotExists("1234567890"));
        assertDoesNotThrow(() -> customerValidationService.validateIdentificationNotExists("0987654321"));
        assertDoesNotThrow(() -> customerValidationService.validateIdentificationNotExists("1122334455"));
    }

    @Test
    void testValidateSpecialCharactersInIdentification() {
        String identification = "123-456-789-0";
        when(customerRepository.findByIdentification(identification)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> customerValidationService.validateIdentificationNotExists(identification));
    }

    @Test
    void testValidateDuplicateIdentificationDifferentFormat() {
        String identification = "1234567890";
        Customer existingCustomer = new Customer();
        existingCustomer.setIdentification(identification);

        when(customerRepository.findByIdentification(identification)).thenReturn(Optional.of(existingCustomer));

        ResourceAlreadyExistsException exception = assertThrows(ResourceAlreadyExistsException.class,
                () -> customerValidationService.validateIdentificationNotExists(identification));

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("identification"));
    }

    @Test
    void testValidateNumericIdentification() {
        String identification = "1234567890";
        when(customerRepository.findByIdentification(identification)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> customerValidationService.validateIdentificationNotExists(identification));
    }
}
