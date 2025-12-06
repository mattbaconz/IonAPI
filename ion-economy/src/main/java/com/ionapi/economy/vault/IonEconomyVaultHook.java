package com.ionapi.economy.vault;

import com.ionapi.economy.EconomyProvider;
import com.ionapi.economy.TransactionResult;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Vault Economy hook for IonAPI Economy.
 * Bridges IonAPI's async economy to Vault's sync API.
 */
public class IonEconomyVaultHook implements Economy {

    private final EconomyProvider provider;
    private final String name;
    private static final long TIMEOUT_SECONDS = 5;

    public IonEconomyVaultHook(@NotNull EconomyProvider provider) {
        this.provider = provider;
        this.name = provider.getName();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return provider.getDefaultCurrency().getDecimalPlaces();
    }

    @Override
    public String format(double amount) {
        return provider.format(BigDecimal.valueOf(amount));
    }

    @Override
    public String currencyNamePlural() {
        return provider.getDefaultCurrency().getPluralName();
    }

    @Override
    public String currencyNameSingular() {
        return provider.getDefaultCurrency().getSingularName();
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return true; // Auto-create accounts
    }

    @Override
    public boolean hasAccount(String playerName) {
        return true;
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return hasAccount(player);
    }

    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return hasAccount(playerName);
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        try {
            return provider.getBalance(player.getUniqueId())
                    .get(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .doubleValue();
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return 0;
        }
    }

    @Override
    public double getBalance(String playerName) {
        return 0; // Deprecated, use UUID version
    }

    @Override
    public double getBalance(OfflinePlayer player, String world) {
        return getBalance(player);
    }

    @Override
    public double getBalance(String playerName, String world) {
        return getBalance(playerName);
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        try {
            return provider.has(player.getUniqueId(), BigDecimal.valueOf(amount))
                    .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return false;
        }
    }

    @Override
    public boolean has(String playerName, double amount) {
        return false;
    }

    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return has(player, amount);
    }

    @Override
    public boolean has(String playerName, String worldName, double amount) {
        return has(playerName, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        try {
            TransactionResult result = provider.withdraw(player.getUniqueId(), BigDecimal.valueOf(amount))
                    .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            
            return toVaultResponse(result, amount);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, e.getMessage());
        }
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Use UUID-based methods");
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return withdrawPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        try {
            TransactionResult result = provider.deposit(player.getUniqueId(), BigDecimal.valueOf(amount))
                    .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            
            return toVaultResponse(result, amount);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, e.getMessage());
        }
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Use UUID-based methods");
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return depositPlayer(player, amount);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return depositPlayer(playerName, amount);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        return true; // Auto-created
    }

    @Override
    public boolean createPlayerAccount(String playerName) {
        return true;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return createPlayerAccount(player);
    }

    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        return createPlayerAccount(playerName);
    }

    // Bank methods - not supported
    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported");
    }

    @Override
    public EconomyResponse createBank(String name, String player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported");
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported");
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported");
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported");
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported");
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported");
    }

    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported");
    }

    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported");
    }

    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported");
    }

    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported");
    }

    @Override
    public List<String> getBanks() {
        return List.of();
    }

    private EconomyResponse toVaultResponse(TransactionResult result, double amount) {
        if (result.isSuccess()) {
            double newBalance = result.getNewBalance() != null ? result.getNewBalance().doubleValue() : 0;
            return new EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, null);
        }
        
        EconomyResponse.ResponseType type = switch (result.getType()) {
            case INSUFFICIENT_FUNDS -> EconomyResponse.ResponseType.FAILURE;
            case ACCOUNT_NOT_FOUND -> EconomyResponse.ResponseType.FAILURE;
            default -> EconomyResponse.ResponseType.FAILURE;
        };
        
        return new EconomyResponse(0, 0, type, result.getErrorMessage());
    }
}
