package com.core.bank.domain.repository;

import com.core.bank.domain.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String>, CustomerRepositoryCustom {

    Optional<Customer> findByIdentification(String identification);
}
