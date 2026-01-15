package com.core.bank.application.strategy.transaction;

import java.math.BigDecimal;

public class DebitStrategy implements TransactionStrategy {
    @Override
    public BigDecimal apply(BigDecimal currentBalance, BigDecimal amount) {
        return currentBalance.subtract(amount);
    }
}
