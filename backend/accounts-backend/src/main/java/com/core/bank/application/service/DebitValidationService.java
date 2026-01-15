package com.core.bank.application.service;

import com.core.bank.domain.entity.Account;
import com.core.bank.domain.repository.TransactionRepository;
import com.core.bank.infrastructure.exception.BusinessRuleException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DebitValidationService {

    private final TransactionRepository transactionRepository;

    @Value("${app.daily-withdrawal-limit:1000}")
    private Double dailyWithdrawalLimit;

    public void validate(Account account, BigDecimal amount) {
        validateSufficientBalance(account);
        validateAmountDoesNotExceedBalance(account, amount);
        validateDailyWithdrawalLimit(account, amount);
    }

    private void validateSufficientBalance(Account account) {
        if (account.getCurrentBalance().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Saldo no disponible");
        }
    }

    private void validateAmountDoesNotExceedBalance(Account account, BigDecimal amount) {
        if (account.getCurrentBalance().compareTo(amount) < 0) {
            throw new BusinessRuleException("Saldo no disponible");
        }
    }

    private void validateDailyWithdrawalLimit(Account account, BigDecimal amount) {
        BigDecimal dailyDebits = transactionRepository.sumDebitsForDay(account.getId(), LocalDateTime.now());
        BigDecimal totalDebits = (dailyDebits != null ? dailyDebits : BigDecimal.ZERO).add(amount);
        
        if (totalDebits.compareTo(BigDecimal.valueOf(dailyWithdrawalLimit)) > 0) {
            throw new BusinessRuleException("Cupo diario Excedido");
        }
    }
}
