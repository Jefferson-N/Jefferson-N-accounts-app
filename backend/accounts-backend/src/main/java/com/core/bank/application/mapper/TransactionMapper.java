package com.core.bank.application.mapper;

import com.core.bank.domain.entity.Account;
import com.core.bank.domain.entity.Transaction;
import com.core.bank.model.dto.TransactionCreate;
import com.core.bank.model.dto.TransactionDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TransactionMapper {

    @Mapping(source = "type", target = "transactionType")
    @Mapping(source = "account.id", target = "accountId")
    TransactionDTO toDto(Transaction transaction);

    @Mapping(source = "transactionType", target = "type")
    @Mapping(source = "accountId", target = "account.id")
    Transaction toEntity(TransactionCreate request);

    default TransactionDTO.TransactionTypeEnum stringToTransactionTypeEnum(String value) {
        if (value == null) {
            return null;
        }
        return TransactionDTO.TransactionTypeEnum.fromValue(value);
    }

    default Account uuidToAccount(UUID accountId) {
        if (accountId == null) {
            return null;
        }
        Account account = new Account();
        account.setId(accountId.toString());
        return account;
    }

    default OffsetDateTime map(LocalDateTime value) {
        return value == null ? null : value.atOffset(ZoneOffset.UTC);
    }

    default LocalDateTime map(OffsetDateTime value) {
        return value == null ? null : value.toLocalDateTime();
    }
}
