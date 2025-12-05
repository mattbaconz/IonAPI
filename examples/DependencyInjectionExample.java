package examples;

import com.ionapi.api.command.CommandContext;
import com.ionapi.api.command.IonCommand;
import com.ionapi.api.config.IonConfig;
import com.ionapi.inject.Inject;
import com.ionapi.inject.IonInjector;
import com.ionapi.inject.Singleton;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Example demonstrating the micro-dependency injection system.
 */
public class DependencyInjectionExample extends JavaPlugin {

    private IonInjector injector;

    @Override
    public void onEnable() {
        // Create the injector
        injector = IonInjector.create(this)
            // Register singleton services
            .register(PlayerService.class)
            .register(EconomyService.class)
            
            // Register config as a factory (always gets fresh instance)
            .register(IonConfig.class, () -> getConfig())
            
            // Register named bindings
            .registerNamed("serverName", getServer().getName())
            .registerNamed("maxPlayers", getServer().getMaxPlayers())
            
            .build();

        // Create command with injected dependencies
        BalanceCommand balanceCmd = injector.create(BalanceCommand.class);
        getCommand("balance").setExecutor((sender, cmd, label, args) -> {
            if (sender instanceof Player player) {
                return balanceCmd.execute(new SimpleCommandContext(player));
            }
            return false;
        });

        // Or inject into existing instance
        PayCommand payCmd = new PayCommand();
        injector.inject(payCmd);
        getCommand("pay").setExecutor((sender, cmd, label, args) -> {
            if (sender instanceof Player player) {
                return payCmd.execute(new SimpleCommandContext(player, args));
            }
            return false;
        });
    }

    // ========== Services ==========

    /**
     * Singleton service - only one instance created.
     */
    @Singleton
    public static class PlayerService {
        private final Map<UUID, PlayerData> cache = new HashMap<>();

        public PlayerData getData(Player player) {
            return cache.computeIfAbsent(player.getUniqueId(), 
                uuid -> new PlayerData(player.getName()));
        }
    }

    @Singleton
    public static class EconomyService {
        private final Map<UUID, Double> balances = new HashMap<>();

        public double getBalance(Player player) {
            return balances.getOrDefault(player.getUniqueId(), 1000.0);
        }

        public void setBalance(Player player, double amount) {
            balances.put(player.getUniqueId(), amount);
        }

        public boolean transfer(Player from, Player to, double amount) {
            double fromBalance = getBalance(from);
            if (fromBalance < amount) return false;
            
            setBalance(from, fromBalance - amount);
            setBalance(to, getBalance(to) + amount);
            return true;
        }
    }

    public static class PlayerData {
        public final String name;
        public int level = 1;
        public int xp = 0;

        public PlayerData(String name) {
            this.name = name;
        }
    }

    // ========== Commands with Injection ==========

    /**
     * Command with injected dependencies.
     */
    public static class BalanceCommand implements IonCommand {
        
        @Inject
        private EconomyService economy;
        
        @Inject
        private PlayerService playerService;

        @Override
        public boolean execute(@NotNull CommandContext ctx) {
            Player player = (Player) ctx.getSender();
            double balance = economy.getBalance(player);
            PlayerData data = playerService.getData(player);
            
            player.sendMessage("§aBalance: §f$" + String.format("%.2f", balance));
            player.sendMessage("§aLevel: §f" + data.level);
            return true;
        }

        @Override @NotNull public String getName() { return "balance"; }
        @Override @NotNull public String getDescription() { return "Check your balance"; }
        @Override @NotNull public String getUsage() { return "/balance"; }
        @Override @NotNull public String getPermission() { return "economy.balance"; }
    }

    public static class PayCommand implements IonCommand {
        
        @Inject
        private EconomyService economy;
        
        @Inject
        private DependencyInjectionExample plugin;

        private String[] args;

        @Override
        public boolean execute(@NotNull CommandContext ctx) {
            Player player = (Player) ctx.getSender();
            
            if (args == null || args.length < 2) {
                player.sendMessage("§cUsage: /pay <player> <amount>");
                return true;
            }

            Player target = plugin.getServer().getPlayer(args[0]);
            if (target == null) {
                player.sendMessage("§cPlayer not found!");
                return true;
            }

            try {
                double amount = Double.parseDouble(args[1]);
                if (economy.transfer(player, target, amount)) {
                    player.sendMessage("§aSent $" + amount + " to " + target.getName());
                    target.sendMessage("§aReceived $" + amount + " from " + player.getName());
                } else {
                    player.sendMessage("§cInsufficient funds!");
                }
            } catch (NumberFormatException e) {
                player.sendMessage("§cInvalid amount!");
            }
            
            return true;
        }

        @Override @NotNull public String getName() { return "pay"; }
        @Override @NotNull public String getDescription() { return "Pay another player"; }
        @Override @NotNull public String getUsage() { return "/pay <player> <amount>"; }
        @Override @NotNull public String getPermission() { return "economy.pay"; }
    }

    // Simple command context implementation
    private static class SimpleCommandContext implements CommandContext {
        private final Player player;
        private final String[] args;

        SimpleCommandContext(Player player, String... args) {
            this.player = player;
            this.args = args;
        }

        @Override public Object getSender() { return player; }
        @Override public String[] getArgs() { return args; }
        @Override public String getArg(int index) { return index < args.length ? args[index] : null; }
        @Override public int getArgCount() { return args.length; }
        @Override public boolean isPlayer() { return true; }
        @Override public void reply(String message) { player.sendMessage(message); }
    }
}
