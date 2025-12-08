package com.ionapi.economy.commands;

import com.ionapi.economy.IonEconomy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Admin debug commands for IonAPI Economy.
 * Commands: /ion eco set, /ion eco give, /ion eco debug
 * 
 * Register in your plugin:
 * <pre>
 * getCommand("ion").setExecutor(new EconomyCommand());
 * </pre>
 */
public class EconomyCommand implements CommandExecutor, TabCompleter {

    private static final String PERMISSION = "ionapi.economy.admin";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, 
                            @NotNull String label, @NotNull String[] args) {
        
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        if (args.length < 2 || !args[0].equalsIgnoreCase("eco")) {
            sendUsage(sender);
            return true;
        }

        String subCommand = args[1].toLowerCase();

        switch (subCommand) {
            case "set" -> handleSet(sender, args);
            case "give" -> handleGive(sender, args);
            case "debug" -> handleDebug(sender, args);
            default -> sendUsage(sender);
        }

        return true;
    }

    private void handleSet(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 4) {
            sender.sendMessage("§cUsage: /ion eco set <player> <amount>");
            return;
        }

        String playerName = args[2];
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage("§cPlayer not found: " + playerName);
            return;
        }

        try {
            BigDecimal amount = new BigDecimal(args[3]);
            
            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                sender.sendMessage("§cAmount must be positive.");
                return;
            }

            IonEconomy.getProvider().setBalance(target.getUniqueId(), amount)
                .thenAccept(result -> {
                    if (result.isSuccess()) {
                        sender.sendMessage("§aSet " + playerName + "'s balance to " + 
                            IonEconomy.format(amount));
                    } else {
                        sender.sendMessage("§cFailed to set balance: " + result.getErrorMessage());
                    }
                })
                .exceptionally(ex -> {
                    sender.sendMessage("§cError: " + ex.getMessage());
                    return null;
                });

        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid amount: " + args[3]);
        }
    }

    private void handleGive(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 4) {
            sender.sendMessage("§cUsage: /ion eco give <player> <amount>");
            return;
        }

        String playerName = args[2];
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage("§cPlayer not found: " + playerName);
            return;
        }

        try {
            BigDecimal amount = new BigDecimal(args[3]);
            
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                sender.sendMessage("§cAmount must be positive.");
                return;
            }

            IonEconomy.deposit(target.getUniqueId(), amount)
                .thenAccept(result -> {
                    if (result.isSuccess()) {
                        sender.sendMessage("§aGave " + IonEconomy.format(amount) + 
                            " to " + playerName);
                        sender.sendMessage("§7New balance: " + 
                            IonEconomy.format(result.getNewBalance()));
                    } else {
                        sender.sendMessage("§cFailed to give money: " + result.getErrorMessage());
                    }
                })
                .exceptionally(ex -> {
                    sender.sendMessage("§cError: " + ex.getMessage());
                    return null;
                });

        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid amount: " + args[3]);
        }
    }

    private void handleDebug(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cUsage: /ion eco debug <player>");
            return;
        }

        String playerName = args[2];
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage("§cPlayer not found: " + playerName);
            return;
        }

        UUID playerId = target.getUniqueId();

        sender.sendMessage("§e§l=== Economy Debug: " + playerName + " ===");
        sender.sendMessage("§7UUID: §f" + playerId);

        IonEconomy.getBalance(playerId)
            .thenAccept(balance -> {
                sender.sendMessage("§7Balance: §a" + IonEconomy.format(balance));
                sender.sendMessage("§7Raw Balance: §f" + balance.toPlainString());
                sender.sendMessage("§7Currency: §f" + 
                    IonEconomy.getProvider().getDefaultCurrency().getId());
                
                // Test has() method
                IonEconomy.has(playerId, BigDecimal.valueOf(100))
                    .thenAccept(has100 -> {
                        sender.sendMessage("§7Has $100: §f" + (has100 ? "Yes" : "No"));
                    });
            })
            .exceptionally(ex -> {
                sender.sendMessage("§cError fetching balance: " + ex.getMessage());
                ex.printStackTrace();
                return null;
            });
    }

    private void sendUsage(@NotNull CommandSender sender) {
        sender.sendMessage("§e§l=== IonAPI Economy Commands ===");
        sender.sendMessage("§7/ion eco set <player> <amount> §f- Set player's balance");
        sender.sendMessage("§7/ion eco give <player> <amount> §f- Give money to player");
        sender.sendMessage("§7/ion eco debug <player> §f- View raw database state");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String alias, @NotNull String[] args) {
        
        if (!sender.hasPermission(PERMISSION)) {
            return null;
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("eco");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("eco")) {
            completions.addAll(Arrays.asList("set", "give", "debug"));
        } else if (args.length == 3 && args[0].equalsIgnoreCase("eco")) {
            // Player names
            Bukkit.getOnlinePlayers().forEach(p -> completions.add(p.getName()));
        } else if (args.length == 4 && args[0].equalsIgnoreCase("eco") && 
                  (args[1].equalsIgnoreCase("set") || args[1].equalsIgnoreCase("give"))) {
            completions.addAll(Arrays.asList("100", "1000", "10000"));
        }

        return completions;
    }
}
