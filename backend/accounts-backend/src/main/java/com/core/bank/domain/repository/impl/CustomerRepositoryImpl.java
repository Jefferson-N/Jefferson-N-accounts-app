package com.core.bank.domain.repository.impl;

import com.core.bank.domain.entity.Customer;
import com.core.bank.domain.repository.CustomerRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("CustomerRepositoryImpl")
@RequiredArgsConstructor
public class CustomerRepositoryImpl implements CustomerRepositoryCustom {

    private final EntityManager entityManager;

    @Override
    public List<Customer> findAllWithSearchPaginated(String search, int offset, int limit) {
        String jpql = "SELECT c FROM Customer c WHERE " +
                "COALESCE(:search, '') = '' OR " +
                "LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                "LOWER(c.identification) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                "LOWER(c.phone) LIKE LOWER(CONCAT('%', :search, '%')) " +
                "ORDER BY c.createdAt DESC";

        TypedQuery<Customer> query = entityManager.createQuery(jpql, Customer.class);
        query.setParameter("search", search != null ? search : "")
                .setFirstResult(offset)
                .setMaxResults(limit);

        return query.getResultList();
    }
}
