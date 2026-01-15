package com.core.bank.domain.repository;

import com.core.bank.domain.entity.Account;
import java.util.List;


public interface AccountRepositoryCustom {
    
    List<Account> findAllWithFiltersPaginated(String customerId, String search, int offset, int limit);
}
