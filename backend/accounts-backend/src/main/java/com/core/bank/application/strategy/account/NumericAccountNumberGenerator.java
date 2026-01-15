package com.core.bank.application.strategy.account;

import com.core.bank.domain.repository.AccountRepository;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class NumericAccountNumberGenerator implements AccountNumberGenerator {
    
    private static final int ACCOUNT_NUMBER_LENGTH = 6;
    private static final long INITIAL_ACCOUNT_NUMBER = 100000L; 
    
    private final AccountRepository accountRepository;
    
    public NumericAccountNumberGenerator(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }
    
    @Override
    public String generateAccountNumber() {
        return accountRepository.findAll()
                .stream()
                .map(account -> {
                    try {
                        return Long.parseLong(account.getAccountNumber());
                    } catch (NumberFormatException e) {
                        return 0L;
                    }
                })
                .max(Long::compare)
                .map(maxNumber -> maxNumber + 1)
                .orElse(INITIAL_ACCOUNT_NUMBER)
                .toString();
    }
}
