package examples;

import com.ionapi.config.HotReloadConfig;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Example showing hot-reloading configuration.
 * Edit config.yml while the server is running and see changes apply instantly!
 */
public class HotReloadExample extends JavaPlugin {

    private HotReloadConfig config;
    private String welcomeMessage;
    private int maxPlayers;

    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();

        // Create hot-reload config
        config = HotReloadConfig.create(this, "config.yml")
            .onReload(this::loadSettings)
            .start();

        // Load initial settings
        loadSettings(config.getConfig());

        getLogger().info("Hot-reload example enabled!");
        getLogger().info("Try editing config.yml while the server is running!");
    }

    @Override
    public void onDisable() {
        if (config != null) {
            config.stop();
        }
    }

    /**
     * Called whenever config.yml is modified.
     */
    private void loadSettings(FileConfiguration cfg) {
        welcomeMessage = cfg.getString("welcome-message", "Welcome!");
        maxPlayers = cfg.getInt("max-players", 100);

        getLogger().info("Settings updated:");
        getLogger().info("  Welcome Message: " + welcomeMessage);
        getLogger().info("  Max Players: " + maxPlayers);
    }

    /**
     * Example: Multiple reload handlers
     */
    public void advancedExample() {
        config = HotReloadConfig.create(this, "config.yml")
            // Handler for messages
            .onReload("messages", cfg -> {
                welcomeMessage = cfg.getString("welcome-message");
                getLogger().info("Messages reloaded");
            })
            // Handler for limits
            .onReload("limits", cfg -> {
                maxPlayers = cfg.getInt("max-players");
                getLogger().info("Limits reloaded");
            })
            .start();
    }

    /**
     * Example: Programmatic config updates
     */
    public void updateConfig() {
        FileConfiguration cfg = config.getConfig();
        cfg.set("last-updated", System.currentTimeMillis());
        config.save();
        // File change will trigger automatic reload!
    }
}
