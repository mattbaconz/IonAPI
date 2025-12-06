package com.ionapi.economy;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;

/**
 * Implementation of TransactionResult.
 */
public class TransactionResultImpl implements TransactionResult {

    private final boolean success;
    private final ResultType type;
    private final BigDecimal amount;
    private final BigDecimal newBalance;
    private final String errorMessage;

    public TransactionResultImpl(boolean success, @NotNull ResultType type, @NotNull BigDecimal amount,
                                  @Nullable BigDecimal newBalance, @Nullable String errorMessage) {
        this.success = success;
        this.type = type;
        this.amount = amount;
        this.newBalance = newBalance;
        this.errorMessage = errorMessage;
    }

    @Override
    public boolean isSuccess() {
        return success;
    }

    @Override
    public @NotNull ResultType getType() {
        return type;
    }

    @Override
    public @Nullable BigDecimal getNewBalance() {
        return newBalance;
    }

    @Override
    public @NotNull BigDecimal getAmount() {
        return amount;
    }

    @Override
    public @Nullable String getErrorMessage() {
        return errorMessage;
    }
}
