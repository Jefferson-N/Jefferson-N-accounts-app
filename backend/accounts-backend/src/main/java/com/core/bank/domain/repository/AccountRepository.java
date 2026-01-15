package com.core.bank.domain.repository;

import com.core.bank.domain.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String>, AccountRepositoryCustom {

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByCustomerId(String customerId);
}
