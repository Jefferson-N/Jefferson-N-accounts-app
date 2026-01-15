package com.core.bank.application.service;

import com.core.bank.application.strategy.transaction.CreditStrategy;
import com.core.bank.application.strategy.transaction.DebitStrategy;
import com.core.bank.application.strategy.transaction.TransactionStrategy;
import com.core.bank.model.dto.TransactionCreate;
import org.springframework.stereotype.Service;

@Service
public class TransactionStrategyService {

    public TransactionStrategy getStrategy(String type) {
        TransactionCreate.TransactionTypeEnum typeTransaction =
                TransactionCreate.TransactionTypeEnum.fromValue(type);

        return switch (typeTransaction) {
            case DEBITO -> new DebitStrategy();
            case CREDITO -> new CreditStrategy();
        };

    }
}
