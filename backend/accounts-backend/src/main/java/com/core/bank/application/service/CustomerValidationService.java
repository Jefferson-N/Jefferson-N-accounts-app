package com.core.bank.application.service;

import com.core.bank.domain.repository.CustomerRepository;
import com.core.bank.infrastructure.exception.ResourceAlreadyExistsException;
import com.core.bank.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerValidationService {

    private final CustomerRepository customerRepository;

    public void validateIdentificationNotExists(String identification) {
        customerRepository.findByIdentification(identification)
                .ifPresent(c -> {
                    throw new ResourceAlreadyExistsException("Customer", "identification", identification);
                });
    }

    public void validateCustomerExists(String customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer", "id", customerId);
        }
    }
}
