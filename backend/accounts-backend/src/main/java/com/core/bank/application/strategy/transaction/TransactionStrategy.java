package com.core.bank.application.strategy.transaction;

import java.math.BigDecimal;

public interface TransactionStrategy {
    BigDecimal apply(BigDecimal currentBalance, BigDecimal amount);
}
