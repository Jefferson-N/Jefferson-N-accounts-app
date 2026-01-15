package com.core.bank.application.strategy.transaction;

import java.math.BigDecimal;

public class CreditStrategy implements TransactionStrategy {
    @Override
    public BigDecimal apply(BigDecimal currentBalance, BigDecimal amount) {
        return currentBalance.add(amount);
    }
}
