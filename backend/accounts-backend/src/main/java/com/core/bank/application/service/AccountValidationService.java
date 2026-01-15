package com.core.bank.application.service;

import com.core.bank.domain.entity.Customer;
import com.core.bank.domain.repository.AccountRepository;
import com.core.bank.domain.repository.CustomerRepository;
import com.core.bank.infrastructure.exception.ResourceAlreadyExistsException;
import com.core.bank.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountValidationService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    public void validateAccountNumberNotExists(String accountNumber) {
        accountRepository.findByAccountNumber(accountNumber)
                .ifPresent(a -> {
                    throw new ResourceAlreadyExistsException("Account", "accountNumber", accountNumber);
                });
    }

    public Customer validateAndGetCustomer(String customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));
    }
}
