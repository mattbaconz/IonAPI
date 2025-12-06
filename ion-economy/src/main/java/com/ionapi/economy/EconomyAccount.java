package com.ionapi.economy;

import com.ionapi.database.annotations.Column;
import com.ionapi.database.annotations.PrimaryKey;
import com.ionapi.database.annotations.Table;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Database entity representing an economy account.
 * Uses BigDecimal for thread-safe, precise monetary calculations.
 */
@Table("ion_economy_accounts")
public class EconomyAccount {

    @PrimaryKey(autoGenerate = true)
    @Column(name = "id")
    private Long id;

    @Column(name = "player_uuid", unique = true)
    private UUID playerUuid;

    @Column(name = "currency_id")
    private String currencyId;

    @Column(name = "balance", columnDefinition = "DECIMAL(19,4)")
    private BigDecimal balance;

    @Column(name = "created_at")
    private long createdAt;

    @Column(name = "updated_at")
    private long updatedAt;

    public EconomyAccount() {
        this.balance = BigDecimal.ZERO;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public EconomyAccount(@NotNull UUID playerUuid, @NotNull String currencyId) {
        this();
        this.playerUuid = playerUuid;
        this.currencyId = currencyId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @NotNull UUID getPlayerUuid() {
        return playerUuid;
    }

    public void setPlayerUuid(@NotNull UUID playerUuid) {
        this.playerUuid = playerUuid;
    }

    public @NotNull String getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(@NotNull String currencyId) {
        this.currencyId = currencyId;
    }

    public @NotNull BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(@NotNull BigDecimal balance) {
        this.balance = balance;
        this.updatedAt = System.currentTimeMillis();
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Adds to the balance (thread-safe).
     */
    public synchronized void add(@NotNull BigDecimal amount) {
        this.balance = this.balance.add(amount);
        this.updatedAt = System.currentTimeMillis();
    }

    /**
     * Subtracts from the balance (thread-safe).
     * @return true if successful, false if insufficient funds
     */
    public synchronized boolean subtract(@NotNull BigDecimal amount) {
        if (this.balance.compareTo(amount) < 0) {
            return false;
        }
        this.balance = this.balance.subtract(amount);
        this.updatedAt = System.currentTimeMillis();
        return true;
    }

    /**
     * Checks if the account has at least the specified amount.
     */
    public boolean has(@NotNull BigDecimal amount) {
        return this.balance.compareTo(amount) >= 0;
    }
}
