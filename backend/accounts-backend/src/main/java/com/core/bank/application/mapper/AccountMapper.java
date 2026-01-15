package com.core.bank.application.mapper;

import com.core.bank.domain.entity.Account;
import com.core.bank.domain.entity.Customer;
import com.core.bank.model.dto.AccountDTO;
import com.core.bank.model.dto.AccountCreate;
import com.core.bank.model.dto.AccountPatch;
import org.mapstruct.*;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AccountMapper {

    @Mapping(source = "accountType", target = "accountType")
    AccountDTO toDto(Account account);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "transactions", ignore = true)
    @Mapping(source = "customerId", target = "customer")
    @Mapping(target = "currentBalance", ignore = true)
    @Mapping(source = "accountType", target = "accountType")
    Account toEntity(AccountCreate request);
    
    Account toEntity(AccountPatch request);
    
    void updateEntity(AccountPatch request, @MappingTarget Account account);
    
    default Customer uuidToCustomer(UUID customerId) {
        if (customerId == null) {
            return null;
        }
        Customer customer = new Customer();
        customer.setId(customerId.toString());
        return customer;
    }

    default AccountDTO.AccountTypeEnum stringToAccountTypeEnum(String value) {
        if (value == null) {
            return null;
        }
        return AccountDTO.AccountTypeEnum.fromValue(value.toUpperCase());
    }


}
