package com.ionapi.economy;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;

/**
 * Result of an economy transaction.
 */
public interface TransactionResult {

    /**
     * Whether the transaction was successful.
     */
    boolean isSuccess();

    /**
     * Gets the result type.
     */
    @NotNull ResultType getType();

    /**
     * Gets the new balance after the transaction (if successful).
     */
    @Nullable BigDecimal getNewBalance();

    /**
     * Gets the amount that was transacted.
     */
    @NotNull BigDecimal getAmount();

    /**
     * Gets an error message if the transaction failed.
     */
    @Nullable String getErrorMessage();

    /**
     * Transaction result types.
     */
    enum ResultType {
        SUCCESS,
        INSUFFICIENT_FUNDS,
        ACCOUNT_NOT_FOUND,
        INVALID_AMOUNT,
        MAX_BALANCE_EXCEEDED,
        DATABASE_ERROR,
        UNKNOWN_ERROR
    }

    /**
     * Creates a successful result.
     */
    static @NotNull TransactionResult success(@NotNull BigDecimal amount, @NotNull BigDecimal newBalance) {
        return new TransactionResultImpl(true, ResultType.SUCCESS, amount, newBalance, null);
    }

    /**
     * Creates a failure result.
     */
    static @NotNull TransactionResult failure(@NotNull ResultType type, @NotNull BigDecimal amount, @Nullable String message) {
        return new TransactionResultImpl(false, type, amount, null, message);
    }
}
