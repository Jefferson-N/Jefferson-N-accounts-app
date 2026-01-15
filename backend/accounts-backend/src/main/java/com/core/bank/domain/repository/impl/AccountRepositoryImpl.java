package com.core.bank.domain.repository.impl;

import com.core.bank.domain.entity.Account;
import com.core.bank.domain.repository.AccountRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("AccountRepositoryImpl")
@RequiredArgsConstructor
public class AccountRepositoryImpl implements AccountRepositoryCustom {

    private final EntityManager entityManager;

    @Override
    public List<Account> findAllWithFiltersPaginated(String customerId, String search, int offset, int limit) {
        String jpql = "SELECT a FROM Account a WHERE " +
                "(COALESCE(:customerId, '') = '' OR a.customer.id = :customerId) AND " +
                "(COALESCE(:search, '') = '' OR " +
                "LOWER(a.accountNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                "LOWER(a.accountType) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                "ORDER BY a.createdAt DESC";

        TypedQuery<Account> query = entityManager.createQuery(jpql, Account.class);
        query.setParameter("customerId", customerId != null ? customerId : "")
                .setParameter("search", search != null ? search : "")
                .setFirstResult(offset)
                .setMaxResults(limit);

        return query.getResultList();
    }
}
