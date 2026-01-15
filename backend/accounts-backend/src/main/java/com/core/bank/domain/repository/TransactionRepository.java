package com.core.bank.domain.repository;

import com.core.bank.domain.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String>, TransactionRepositoryCustom {

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.account.id = :accountId AND t.type = 'DEBITO' " +
           "AND CAST(t.date AS DATE) = CAST(:date AS DATE)")
    BigDecimal sumDebitsForDay(@Param("accountId") String accountId,
                              @Param("date") LocalDateTime date);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.account.id = :accountId")
    long countByAccountId(@Param("accountId") String accountId);
}
