package com.core.bank.application.mapper;

import com.core.bank.domain.entity.Customer;
import com.core.bank.model.dto.CustomerDTO;
import com.core.bank.model.dto.CustomerCreate;
import com.core.bank.model.dto.CustomerUpdate;
import com.core.bank.model.dto.CustomerPatch;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CustomerMapper {

    CustomerDTO toDto(Customer customer);

    Customer toEntity(CustomerCreate request);
    
    Customer toEntity(CustomerUpdate request);
    
    void updateEntity(CustomerUpdate request, @MappingTarget Customer customer);
    
    void updateEntity(CustomerPatch request, @MappingTarget Customer customer);
    
}
