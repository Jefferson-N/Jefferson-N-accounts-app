package com.core.bank.application.service;

import com.core.bank.application.dto.PaginationMetadata;
import com.core.bank.application.utils.PaginationUtil;
import com.core.bank.application.mapper.CustomerMapper;
import com.core.bank.domain.entity.Customer;
import com.core.bank.domain.repository.CustomerRepository;
import com.core.bank.domain.repository.CustomerRepositoryCustom;
import com.core.bank.domain.repository.AccountRepository;
import com.core.bank.infrastructure.exception.ResourceNotFoundException;
import com.core.bank.infrastructure.exception.ResourceAlreadyExistsException;
import com.core.bank.infrastructure.exception.BusinessRuleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    @Qualifier("CustomerRepositoryImpl")
    private final CustomerRepositoryCustom customerRepositoryCustom;
    private final CustomerValidationService validationService;
    private final AccountRepository accountRepository;

    public CustomerService(CustomerRepository customerRepository,
                           @Qualifier("CustomerRepositoryImpl") CustomerRepositoryCustom customerRepositoryCustom,
                           CustomerValidationService validationService,
                           CustomerMapper customerMapper,
                           AccountRepository accountRepository) {
        this.customerRepository = customerRepository;
        this.customerRepositoryCustom = customerRepositoryCustom;
        this.validationService = validationService;
        this.accountRepository = accountRepository;
    }

    public Customer create(Customer customer) {
        validationService.validateIdentificationNotExists(customer.getIdentification());
        return customerRepository.save(customer);
    }

    public Customer getById(String id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
    }

    public Customer update(String id, Customer customerDetails) {
        Customer customer = getById(id);

        if (!customer.getIdentification().equals(customerDetails.getIdentification())) {
            customerRepository.findByIdentification(customerDetails.getIdentification())
                    .ifPresent(c -> {
                        throw new ResourceAlreadyExistsException("Customer", "identification",
                                customerDetails.getIdentification());
                    });
        }

        customer.setName(customerDetails.getName());
        customer.setGender(customerDetails.getGender());
        customer.setAge(customerDetails.getAge());
        customer.setIdentification(customerDetails.getIdentification());
        customer.setAddress(customerDetails.getAddress());
        customer.setPhone(customerDetails.getPhone());
        customer.setStatus(customerDetails.getStatus());

        return customerRepository.save(customer);
    }

    public Customer patch(String id, Customer customerPatch) {
        Customer customer = getById(id);

        Optional.ofNullable(customerPatch.getIdentification())
                .filter(newId -> !newId.equals(customer.getIdentification()))
                .ifPresent(newId -> {
                    customerRepository.findByIdentification(newId)
                            .ifPresent(c -> {
                                throw new ResourceAlreadyExistsException("Customer", "identification", newId);
                            });
                });

        return customerRepository.save(customer);
    }

    public void delete(String id) {
        Customer customer = getById(id);
        
        long accountCount = accountRepository.countByCustomerId(id);
        if (accountCount > 0) {
            throw new BusinessRuleException("No se puede eliminar el cliente porque tiene " + accountCount + " cuenta(s) asociada(s)");
        }
        
        customerRepository.delete(customer);
    }

    public List<Customer> findCustomers(Integer offset, Integer size, String search) {

        return customerRepositoryCustom.findAllWithSearchPaginated(search, offset, size);


    }

    public PaginationMetadata buildMetadata(Integer page, Integer size) {

        return PaginationUtil.buildMetadata(page, size, customerRepository.count());

    }
}
