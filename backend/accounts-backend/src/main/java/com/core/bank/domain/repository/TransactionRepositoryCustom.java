package com.core.bank.domain.repository;

import com.core.bank.domain.entity.Transaction;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepositoryCustom {
    
    List<Transaction> findAllWithFiltersPaginated(String accountId, LocalDateTime from, LocalDateTime to,
                                                  int offset, int limit);

    List<Transaction> findByAccountIdAndDateRange(String accountId, LocalDateTime from, LocalDateTime to);
}
