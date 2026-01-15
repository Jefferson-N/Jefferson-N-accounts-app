package com.core.bank.domain.repository;

import com.core.bank.domain.entity.Customer;
import java.util.List;

public interface CustomerRepositoryCustom {
    
    List<Customer> findAllWithSearchPaginated(String search, int offset, int limit);
}
