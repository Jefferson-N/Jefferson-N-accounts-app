package com.core.bank.domain.repository.impl;

import com.core.bank.domain.entity.Transaction;
import com.core.bank.domain.repository.TransactionRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository("TransactionRepositoryImpl")
@RequiredArgsConstructor
public class TransactionRepositoryImpl implements TransactionRepositoryCustom {

    private final EntityManager entityManager;

    @Override
    public List<Transaction> findAllWithFiltersPaginated(String accountId, LocalDateTime from, LocalDateTime to, int offset, int limit) {
        String jpql = "SELECT t FROM Transaction t WHERE " +
                "(COALESCE(:accountId, '') = '' OR t.account.id = :accountId) AND " +
                "(COALESCE(:from, NULL) IS NULL OR COALESCE(:to, NULL) IS NULL OR t.date BETWEEN :from AND :to) " +
                "ORDER BY t.date DESC";

        TypedQuery<Transaction> query = entityManager.createQuery(jpql, Transaction.class);

        query.setParameter("accountId", accountId != null ? accountId : "")
                .setParameter("from", from)
                .setParameter("to", to)
                .setFirstResult(offset)
                .setMaxResults(limit);

        return query.getResultList();
    }

    @Override
    public List<Transaction> findByAccountIdAndDateRange(String accountId, LocalDateTime from, LocalDateTime to) {
        String jpql = "SELECT t FROM Transaction t WHERE " +
                "t.account.id = :accountId AND " +
                "t.date BETWEEN :from AND :to " +
                "ORDER BY t.date ASC";

        TypedQuery<Transaction> query = entityManager.createQuery(jpql, Transaction.class);
        query.setParameter("accountId", accountId);
        query.setParameter("from", from);
        query.setParameter("to", to);

        return query.getResultList();
    }
}
