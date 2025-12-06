package com.ionapi.economy;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Core economy provider interface.
 * Provides async-first economy operations with BigDecimal precision.
 */
public interface EconomyProvider {

    /**
     * Gets the name of this economy provider.
     */
    @NotNull String getName();

    /**
     * Gets the default currency.
     */
    @NotNull Currency getDefaultCurrency();

    /**
     * Checks if a player has at least the specified amount.
     */
    @NotNull CompletableFuture<Boolean> has(@NotNull UUID playerId, @NotNull BigDecimal amount);

    /**
     * Checks if a player has at least the specified amount of a specific currency.
     */
    @NotNull CompletableFuture<Boolean> has(@NotNull UUID playerId, @NotNull Currency currency, @NotNull BigDecimal amount);

    /**
     * Gets a player's balance.
     */
    @NotNull CompletableFuture<BigDecimal> getBalance(@NotNull UUID playerId);

    /**
     * Gets a player's balance for a specific currency.
     */
    @NotNull CompletableFuture<BigDecimal> getBalance(@NotNull UUID playerId, @NotNull Currency currency);

    /**
     * Withdraws an amount from a player's account.
     */
    @NotNull CompletableFuture<TransactionResult> withdraw(@NotNull UUID playerId, @NotNull BigDecimal amount);

    /**
     * Withdraws an amount of a specific currency from a player's account.
     */
    @NotNull CompletableFuture<TransactionResult> withdraw(@NotNull UUID playerId, @NotNull Currency currency, @NotNull BigDecimal amount);

    /**
     * Deposits an amount to a player's account.
     */
    @NotNull CompletableFuture<TransactionResult> deposit(@NotNull UUID playerId, @NotNull BigDecimal amount);

    /**
     * Deposits an amount of a specific currency to a player's account.
     */
    @NotNull CompletableFuture<TransactionResult> deposit(@NotNull UUID playerId, @NotNull Currency currency, @NotNull BigDecimal amount);

    /**
     * Sets a player's balance directly.
     */
    @NotNull CompletableFuture<TransactionResult> setBalance(@NotNull UUID playerId, @NotNull BigDecimal amount);

    /**
     * Transfers money between two players atomically.
     */
    @NotNull CompletableFuture<TransactionResult> transfer(@NotNull UUID from, @NotNull UUID to, @NotNull BigDecimal amount);

    /**
     * Formats an amount for display.
     */
    @NotNull String format(@NotNull BigDecimal amount);

    /**
     * Formats an amount with a specific currency for display.
     */
    @NotNull String format(@NotNull BigDecimal amount, @NotNull Currency currency);

    /**
     * Creates a new transaction builder for complex operations.
     */
    @NotNull EconomyTransaction transaction(@NotNull UUID playerId);
}
