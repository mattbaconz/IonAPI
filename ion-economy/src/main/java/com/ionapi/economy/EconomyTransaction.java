package com.ionapi.economy;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

/**
 * Fluent builder for complex economy transactions.
 * Allows chaining multiple operations that execute atomically.
 * 
 * Example:
 * <pre>
 * IonEconomy.transaction(player)
 *     .withdraw(50)
 *     .reason("Shop purchase")
 *     .commit();
 * </pre>
 */
public interface EconomyTransaction {

    /**
     * Adds a withdrawal operation.
     */
    @NotNull EconomyTransaction withdraw(@NotNull BigDecimal amount);

    /**
     * Adds a withdrawal operation with double amount.
     */
    default @NotNull EconomyTransaction withdraw(double amount) {
        return withdraw(BigDecimal.valueOf(amount));
    }

    /**
     * Adds a deposit operation.
     */
    @NotNull EconomyTransaction deposit(@NotNull BigDecimal amount);

    /**
     * Adds a deposit operation with double amount.
     */
    default @NotNull EconomyTransaction deposit(double amount) {
        return deposit(BigDecimal.valueOf(amount));
    }

    /**
     * Sets the currency for this transaction.
     */
    @NotNull EconomyTransaction currency(@NotNull Currency currency);

    /**
     * Sets a reason/description for this transaction (for logging).
     */
    @NotNull EconomyTransaction reason(@NotNull String reason);

    /**
     * Commits the transaction atomically.
     */
    @NotNull CompletableFuture<TransactionResult> commit();

    /**
     * Previews the transaction without executing it.
     * Returns what the result would be.
     */
    @NotNull CompletableFuture<TransactionResult> preview();
}
