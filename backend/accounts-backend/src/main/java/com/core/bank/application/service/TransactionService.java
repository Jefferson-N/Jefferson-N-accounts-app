package com.core.bank.application.service;

import com.core.bank.application.dto.PaginationMetadata;
import com.core.bank.application.utils.PaginationUtil;
import com.core.bank.domain.entity.Account;
import com.core.bank.domain.entity.Transaction;
import com.core.bank.domain.repository.AccountRepository;
import com.core.bank.domain.repository.TransactionRepository;
import com.core.bank.domain.repository.TransactionRepositoryCustom;
import com.core.bank.infrastructure.exception.ResourceNotFoundException;
import com.core.bank.model.dto.TransactionDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    @Qualifier("TransactionRepositoryImpl")
    private final TransactionRepositoryCustom transactionRepositoryCustom;
    private final AccountRepository accountRepository;
    private final DebitValidationService debitValidationService;
    private final TransactionStrategyService strategyService;

    public TransactionService(TransactionRepository transactionRepository,
                              @Qualifier("TransactionRepositoryImpl")
                              TransactionRepositoryCustom transactionRepositoryCustom,
                              AccountRepository accountRepository,
                              DebitValidationService debitValidationService,
                              TransactionStrategyService strategyService) {
        this.transactionRepository = transactionRepository;
        this.transactionRepositoryCustom = transactionRepositoryCustom;
        this.accountRepository = accountRepository;
        this.debitValidationService = debitValidationService;
        this.strategyService = strategyService;
    }

    public Transaction create(Transaction transaction) {
        Account account = accountRepository.findById(transaction.getAccount().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", transaction.getAccount().getId()));

        transaction.setAccount(account);


        if (TransactionDTO.TransactionTypeEnum.DEBITO.getValue().equalsIgnoreCase(transaction.getType())) {
            debitValidationService.validate(account, transaction.getAmount());
        }

        BigDecimal newBalance = strategyService.getStrategy(transaction.getType())
                .apply(account.getCurrentBalance(), transaction.getAmount());
        transaction.setBalance(newBalance);

        account.setCurrentBalance(newBalance);

        Transaction saved = transactionRepository.save(transaction);
        accountRepository.save(account);

        return saved;
    }

    public Transaction getById(String id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));
    }

    public void delete(String id) {
        Transaction transaction = getById(id);
        Account account = transaction.getAccount();

        BigDecimal reversedBalance = strategyService.getStrategy(transaction.getType())
                .apply(account.getCurrentBalance(), transaction.getAmount().negate());
        account.setCurrentBalance(reversedBalance);

        transactionRepository.delete(transaction);
        accountRepository.save(account);
    }

    public PaginationMetadata buildMetadata(Integer page, Integer size, String accountId, LocalDateTime fromDateTime, LocalDateTime toDateTime) {
        long total = countTransactions(accountId, fromDateTime, toDateTime);
        return PaginationUtil.buildMetadata(page, size, total);
    }

    private long countTransactions(String accountId, LocalDateTime fromDateTime, LocalDateTime toDateTime) {
        List<Transaction> transactions = transactionRepositoryCustom.findAllWithFiltersPaginated(
                accountId, fromDateTime, toDateTime, 0, Integer.MAX_VALUE);
        return transactions.size();
    }

    public List<Transaction> getTransactions(String accountId,
                                          LocalDateTime fromDateTime,
                                          LocalDateTime toDateTime, int offset, int pageSize) {
        return transactionRepositoryCustom.findAllWithFiltersPaginated(accountId
                , fromDateTime, toDateTime, offset, pageSize);
    }
}
