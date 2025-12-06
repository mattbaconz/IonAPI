package examples;

import com.ionapi.economy.EconomyProvider;
import com.ionapi.economy.IonEconomy;
import com.ionapi.economy.TransactionResult;
import com.ionapi.economy.vault.IonEconomyVaultHook;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.math.BigDecimal;

/**
 * Example showing how to use IonAPI's Economy system.
 * 
 * Features demonstrated:
 * - Basic balance operations
 * - Fluent transaction API
 * - Vault integration
 */
public class EconomyExample extends JavaPlugin {

    @Override
    public void onEnable() {
        // Initialize your economy provider (you'd implement this)
        // EconomyProvider provider = new MyEconomyProvider(this);
        // IonEconomy.setProvider(provider);
        
        // Hook into Vault if present
        // hookVault(provider);
        
        getLogger().info("Economy example loaded!");
    }

    /**
     * Example: Check player balance
     */
    public void checkBalance(Player player) {
        IonEconomy.getBalance(player.getUniqueId()).thenAccept(balance -> {
            player.sendMessage("Your balance: " + IonEconomy.format(balance));
        });
    }

    /**
     * Example: Simple withdraw
     */
    public void buyItem(Player player, double price) {
        IonEconomy.withdraw(player.getUniqueId(), price).thenAccept(result -> {
            if (result.isSuccess()) {
                player.sendMessage("Purchase successful! New balance: " + 
                    IonEconomy.format(result.getNewBalance()));
                // Give item to player
            } else {
                player.sendMessage("Purchase failed: " + result.getErrorMessage());
            }
        });
    }

    /**
     * Example: Fluent transaction API
     */
    public void complexTransaction(Player player) {
        IonEconomy.transaction(player.getUniqueId())
            .withdraw(BigDecimal.valueOf(100))
            .reason("Shop purchase: Diamond Sword")
            .commit()
            .thenAccept(result -> {
                if (result.isSuccess()) {
                    player.sendMessage("Transaction complete!");
                } else {
                    handleTransactionError(player, result);
                }
            });
    }

    /**
     * Example: Transfer between players
     */
    public void payPlayer(Player sender, Player receiver, double amount) {
        IonEconomy.transfer(
            sender.getUniqueId(), 
            receiver.getUniqueId(), 
            BigDecimal.valueOf(amount)
        ).thenAccept(result -> {
            if (result.isSuccess()) {
                sender.sendMessage("Sent " + IonEconomy.format(amount) + " to " + receiver.getName());
                receiver.sendMessage("Received " + IonEconomy.format(amount) + " from " + sender.getName());
            } else {
                sender.sendMessage("Transfer failed: " + result.getErrorMessage());
            }
        });
    }

    /**
     * Example: Check before purchase
     */
    public void safePurchase(Player player, double price) {
        IonEconomy.has(player.getUniqueId(), price).thenAccept(hasEnough -> {
            if (hasEnough) {
                // Proceed with purchase
                buyItem(player, price);
            } else {
                player.sendMessage("You don't have enough money!");
            }
        });
    }

    /**
     * Hook into Vault for compatibility with other plugins
     */
    private void hookVault(EconomyProvider provider) {
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            Economy vaultEconomy = new IonEconomyVaultHook(provider);
            Bukkit.getServicesManager().register(
                Economy.class, 
                vaultEconomy, 
                this, 
                ServicePriority.Highest
            );
            getLogger().info("Hooked into Vault!");
        }
    }

    private void handleTransactionError(Player player, TransactionResult result) {
        String message = switch (result.getType()) {
            case INSUFFICIENT_FUNDS -> "You don't have enough money!";
            case ACCOUNT_NOT_FOUND -> "Account not found!";
            case INVALID_AMOUNT -> "Invalid amount!";
            case MAX_BALANCE_EXCEEDED -> "Maximum balance exceeded!";
            default -> "Transaction failed: " + result.getErrorMessage();
        };
        player.sendMessage(message);
    }
}
