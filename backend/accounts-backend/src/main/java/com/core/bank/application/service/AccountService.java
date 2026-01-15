package com.core.bank.application.service;

import com.core.bank.application.dto.PaginationMetadata;
import com.core.bank.application.utils.PaginationUtil;
import com.core.bank.application.mapper.AccountMapper;
import com.core.bank.application.strategy.account.AccountNumberGenerator;
import com.core.bank.domain.entity.Account;
import com.core.bank.domain.entity.Customer;
import com.core.bank.domain.repository.AccountRepository;
import com.core.bank.domain.repository.AccountRepositoryCustom;
import com.core.bank.domain.repository.TransactionRepository;
import com.core.bank.infrastructure.exception.ResourceNotFoundException;
import com.core.bank.infrastructure.exception.BusinessRuleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class AccountService {

    private final AccountRepository accountRepository;
    @Qualifier("AccountRepositoryImpl")
    private final AccountRepositoryCustom accountRepositoryCustom;
    private final AccountValidationService validationService;
    private final AccountNumberGenerator accountNumberGenerator;
    private final TransactionRepository transactionRepository;

    public AccountService(AccountRepository accountRepository,
                         @Qualifier("AccountRepositoryImpl") AccountRepositoryCustom accountRepositoryCustom,
                         AccountValidationService validationService,
                         AccountMapper accountMapper,
                         AccountNumberGenerator accountNumberGenerator,
                         TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.accountRepositoryCustom = accountRepositoryCustom;
        this.validationService = validationService;
        this.accountNumberGenerator = accountNumberGenerator;
        this.transactionRepository = transactionRepository;
    }

    public Account create(Account account) {
        String generatedAccountNumber = generateUniqueAccountNumber();
        account.setAccountNumber(generatedAccountNumber);
        
        Customer customer = validationService.validateAndGetCustomer(account.getCustomer().getId());
        
        account.setCustomer(customer);
        return accountRepository.save(account);
    }
    
    private String generateUniqueAccountNumber() {
        int maxRetries = 10;
        for (int i = 0; i < maxRetries; i++) {
            String accountNumber = accountNumberGenerator.generateAccountNumber();
            if (!accountRepository.existsByAccountNumber(accountNumber)) {
                log.info("Número de cuenta generado: {}", accountNumber);
                return accountNumber;
            }
        }
        throw new BusinessRuleException("No se pudo generar un número de cuenta único después de " + maxRetries + " intentos");
    }

    
    public Account getById(String id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));
    }

    public Account patch(String id, Account accountPatch) {
        Account account = getById(id);

        return accountRepository.save(account);
    }

    public void delete(String id) {
        Account account = getById(id);
        
        long transactionCount = transactionRepository.countByAccountId(id);
        if (transactionCount > 0) {
            throw new BusinessRuleException("No se puede eliminar la cuenta porque tiene " + transactionCount + " movimiento(s) asociado(s)");
        }
        
        accountRepository.delete(account);
    }

    
    public List<Account> findAccounts(UUID customerId, Integer offset, Integer size, String search) {
        String customerIdStr = customerId != null ? customerId.toString() : null;
        return accountRepositoryCustom.findAllWithFiltersPaginated(customerIdStr, search, offset, size);
    }

    public PaginationMetadata buildMetadata(Integer page, Integer size, UUID customerId, String search) {
        String customerIdStr = customerId != null ? customerId.toString() : null;
        long total = countAccounts(customerIdStr, search);
        return PaginationUtil.buildMetadata(page, size, total);
    }

    private long countAccounts(String customerId, String search) {
        List<Account> accounts = accountRepositoryCustom.findAllWithFiltersPaginated(customerId, search, 0, Integer.MAX_VALUE);
        return accounts.size();
    }
}
