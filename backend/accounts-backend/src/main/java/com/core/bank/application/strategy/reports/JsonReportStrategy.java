package com.core.bank.application.strategy.reports;

import com.core.bank.domain.entity.Account;
import com.core.bank.domain.entity.Customer;
import com.core.bank.domain.entity.Transaction;
import com.core.bank.domain.repository.AccountRepository;
import com.core.bank.domain.repository.CustomerRepository;
import com.core.bank.domain.repository.TransactionRepository;
import com.core.bank.infrastructure.exception.ResourceNotFoundException;
import com.core.bank.model.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JsonReportStrategy implements ReportGenerationStrategy {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public GetReporte200Response generateReport(UUID clienteId, LocalDate from, LocalDate to) {
        Customer customer = customerRepository.findById(clienteId.toString())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", clienteId.toString()));

        List<Account> accounts = accountRepository.findByCustomerId(clienteId.toString());

        ReportJsonCustomer customerDto = new ReportJsonCustomer()
                .id(clienteId)
                .name(customer.getName());

        ReportJsonRange range = new ReportJsonRange()
                .from(from)
                .to(to);

        List<ReportJsonAccountsInner> accountsList = accounts.stream()
                .map(account -> buildAccountWithTransactions(account, from, to))
                .collect(Collectors.toList());

        return new ReportJson()
                .customer(customerDto)
                .range(range)
                .accounts(accountsList);
    }

    private ReportJsonAccountsInner buildAccountWithTransactions(Account account, LocalDate from, LocalDate to) {
        List<Transaction> transactions = transactionRepository.findByAccountIdAndDateRange(
                account.getId(),
                from.atStartOfDay(),
                to.atTime(23, 59, 59)
        );

        List<ReportJsonAccountsInnerTransactionsInner> transactionsList = transactions.stream()
                .map(txn -> new ReportJsonAccountsInnerTransactionsInner()
                        .id(txn.getId())
                        .date(txn.getDate().toString())
                        .transactionType(txn.getType())
                        .amount(txn.getAmount())
                        .balance(txn.getBalance())
                )
                .collect(Collectors.toList());

        BigDecimal totalDebits = transactions.stream()
                .filter(txn -> "DEBITO".equals(txn.getType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCredits = transactions.stream()
                .filter(txn -> "CREDITO".equals(txn.getType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        ReportJsonAccountsInnerTotals totals = new ReportJsonAccountsInnerTotals()
                .debits(totalDebits)
                .credits(totalCredits);

        return new ReportJsonAccountsInner()
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .initialBalance(account.getInitialBalance())
                .status(account.getStatus())
                .transactions(transactionsList)
                .totals(totals);
    }
}
