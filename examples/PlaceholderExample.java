package examples;

import com.ionapi.placeholder.IonPlaceholder;
import com.ionapi.placeholder.IonPlaceholderRegistry;
import com.ionapi.placeholder.SimplePlaceholder;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Examples demonstrating the IonPlaceholder API.
 * 
 * The ion-placeholder module provides automatic PlaceholderAPI integration.
 * Just implement IonPlaceholder or use SimplePlaceholder builder, and Ion
 * handles the PAPI hook registration automatically when the plugin is present.
 */
public class PlaceholderExample extends JavaPlugin {

    private IonPlaceholderRegistry placeholderRegistry;

    @Override
    public void onEnable() {
        // ============================================
        // APPROACH 1: Using SimplePlaceholder Builder
        // ============================================
        // Best for simple placeholders with known params
        
        placeholderRegistry = IonPlaceholderRegistry.create(this)
            .register(SimplePlaceholder.create("myplugin")
                .author("YourName")
                .version("1.0.0")
                
                // Static placeholders (not player-dependent)
                .staticPlaceholder("server", "My Server")
                .staticPlaceholder("version", "1.0.0")
                
                // Player-dependent placeholders
                .placeholder("name", player -> player.getName())
                .placeholder("health", player -> {
                    if (player.isOnline()) {
                        return String.valueOf((int) player.getPlayer().getHealth());
                    }
                    return "0";
                })
                .placeholder("level", player -> {
                    if (player.isOnline()) {
                        return String.valueOf(player.getPlayer().getLevel());
                    }
                    return "0";
                })
                
                // Fallback for dynamic params like %myplugin_stat_kills%
                .fallback((player, params) -> {
                    if (params.startsWith("stat_")) {
                        String statName = params.substring(5);
                        return getPlayerStat(player, statName);
                    }
                    return null;
                })
                .build())
            
            // ============================================
            // APPROACH 2: Using IonPlaceholder Interface
            // ============================================
            // Best for complex logic or when you need full control
            
            .register(new EconomyPlaceholders())
            .build();
        
        // Check if PAPI was available
        if (placeholderRegistry.isPlaceholderAPIAvailable()) {
            getLogger().info("PlaceholderAPI found! Registered " + 
                placeholderRegistry.getRegisteredCount() + " placeholder expansions.");
        }
    }

    @Override
    public void onDisable() {
        if (placeholderRegistry != null) {
            placeholderRegistry.unregisterAll();
        }
    }

    private String getPlayerStat(OfflinePlayer player, String statName) {
        // Your stat lookup logic here
        return "0";
    }

    // ============================================
    // Custom IonPlaceholder Implementation
    // ============================================
    
    /**
     * Example of implementing IonPlaceholder directly.
     * Creates placeholders like:
     * - %economy_balance%
     * - %economy_formatted%
     * - %economy_currency%
     */
    public class EconomyPlaceholders implements IonPlaceholder {

        @Override
        @NotNull
        public String getIdentifier() {
            return "economy";
        }

        @Override
        @NotNull
        public String getAuthor() {
            return "YourName";
        }

        @Override
        @NotNull
        public String getVersion() {
            return "1.0.0";
        }

        @Override
        @NotNull
        public Map<String, String> getStaticPlaceholders() {
            // These don't need a player
            return Map.of(
                "currency", "$",
                "currency_name", "Coins"
            );
        }

        @Override
        @Nullable
        public String onRequest(@Nullable OfflinePlayer player, @NotNull String params) {
            if (player == null) return null;

            return switch (params) {
                case "balance" -> String.valueOf(getBalance(player));
                case "formatted" -> formatBalance(getBalance(player));
                case "rank" -> getEconomyRank(player);
                default -> null;
            };
        }

        private double getBalance(OfflinePlayer player) {
            // Your economy integration here
            return 1000.0;
        }

        private String formatBalance(double balance) {
            return String.format("$%,.2f", balance);
        }

        private String getEconomyRank(OfflinePlayer player) {
            // Your ranking logic here
            return "#1";
        }
    }
}

/*
 * RESULTING PLACEHOLDERS:
 * 
 * From SimplePlaceholder "myplugin":
 *   %myplugin_server%     -> "My Server"
 *   %myplugin_version%    -> "1.0.0"
 *   %myplugin_name%       -> Player's name
 *   %myplugin_health%     -> Player's health
 *   %myplugin_level%      -> Player's XP level
 *   %myplugin_stat_kills% -> Dynamic stat lookup
 * 
 * From EconomyPlaceholders:
 *   %economy_currency%      -> "$"
 *   %economy_currency_name% -> "Coins"
 *   %economy_balance%       -> "1000.0"
 *   %economy_formatted%     -> "$1,000.00"
 *   %economy_rank%          -> "#1"
 */
