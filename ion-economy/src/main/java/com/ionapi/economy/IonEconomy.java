package com.ionapi.economy;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Main entry point for the IonAPI Economy system.
 * Provides static access to economy operations.
 * 
 * Example usage:
 * <pre>
 * // Check balance
 * IonEconomy.getBalance(player.getUniqueId()).thenAccept(balance -> {
 *     player.sendMessage("Balance: " + IonEconomy.format(balance));
 * });
 * 
 * // Withdraw with fluent API
 * IonEconomy.transaction(player.getUniqueId())
 *     .withdraw(100)
 *     .reason("Shop purchase")
 *     .commit()
 *     .thenAccept(result -> {
 *         if (result.isSuccess()) {
 *             player.sendMessage("Purchase complete!");
 *         }
 *     });
 * </pre>
 */
public final class IonEconomy {

    private static EconomyProvider provider;

    private IonEconomy() {}

    /**
     * Registers the economy provider.
     */
    public static void setProvider(@NotNull EconomyProvider provider) {
        IonEconomy.provider = provider;
    }

    /**
     * Gets the current economy provider.
     */
    public static @Nullable EconomyProvider getProvider() {
        return provider;
    }

    /**
     * Checks if an economy provider is registered.
     */
    public static boolean isEnabled() {
        return provider != null;
    }

    /**
     * Gets a player's balance.
     */
    public static @NotNull CompletableFuture<BigDecimal> getBalance(@NotNull UUID playerId) {
        checkProvider();
        return provider.getBalance(playerId);
    }

    /**
     * Checks if a player has at least the specified amount.
     */
    public static @NotNull CompletableFuture<Boolean> has(@NotNull UUID playerId, @NotNull BigDecimal amount) {
        checkProvider();
        return provider.has(playerId, amount);
    }

    /**
     * Checks if a player has at least the specified amount.
     */
    public static @NotNull CompletableFuture<Boolean> has(@NotNull UUID playerId, double amount) {
        return has(playerId, BigDecimal.valueOf(amount));
    }

    /**
     * Withdraws an amount from a player's account.
     */
    public static @NotNull CompletableFuture<TransactionResult> withdraw(@NotNull UUID playerId, @NotNull BigDecimal amount) {
        checkProvider();
        return provider.withdraw(playerId, amount);
    }

    /**
     * Withdraws an amount from a player's account.
     */
    public static @NotNull CompletableFuture<TransactionResult> withdraw(@NotNull UUID playerId, double amount) {
        return withdraw(playerId, BigDecimal.valueOf(amount));
    }

    /**
     * Deposits an amount to a player's account.
     */
    public static @NotNull CompletableFuture<TransactionResult> deposit(@NotNull UUID playerId, @NotNull BigDecimal amount) {
        checkProvider();
        return provider.deposit(playerId, amount);
    }

    /**
     * Deposits an amount to a player's account.
     */
    public static @NotNull CompletableFuture<TransactionResult> deposit(@NotNull UUID playerId, double amount) {
        return deposit(playerId, BigDecimal.valueOf(amount));
    }

    /**
     * Transfers money between two players atomically.
     */
    public static @NotNull CompletableFuture<TransactionResult> transfer(@NotNull UUID from, @NotNull UUID to, @NotNull BigDecimal amount) {
        checkProvider();
        return provider.transfer(from, to, amount);
    }

    /**
     * Creates a new transaction builder.
     */
    public static @NotNull EconomyTransaction transaction(@NotNull UUID playerId) {
        checkProvider();
        return provider.transaction(playerId);
    }

    /**
     * Formats an amount for display.
     */
    public static @NotNull String format(@NotNull BigDecimal amount) {
        checkProvider();
        return provider.format(amount);
    }

    /**
     * Formats an amount for display.
     */
    public static @NotNull String format(double amount) {
        return format(BigDecimal.valueOf(amount));
    }

    private static void checkProvider() {
        if (provider == null) {
            throw new IllegalStateException("No economy provider registered! Make sure IonEconomy is enabled.");
        }
    }
}
