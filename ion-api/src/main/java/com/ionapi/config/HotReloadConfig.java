package com.ionapi.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * Configuration that automatically reloads when the file changes.
 * Uses WatchService to detect file modifications in real-time.
 * 
 * Example usage:
 * <pre>
 * HotReloadConfig config = HotReloadConfig.create(plugin, "config.yml")
 *     .onReload(cfg -> {
 *         plugin.getLogger().info("Config reloaded!");
 *         updateSettings(cfg);
 *     })
 *     .start();
 * </pre>
 */
public class HotReloadConfig {

    private final Plugin plugin;
    private final File configFile;
    private final Map<String, Consumer<FileConfiguration>> reloadHandlers = new ConcurrentHashMap<>();
    private FileConfiguration config;
    private WatchService watchService;
    private Thread watchThread;
    private volatile boolean running = false;

    private HotReloadConfig(@NotNull Plugin plugin, @NotNull String fileName) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), fileName);
        
        // Create file if it doesn't exist
        if (!configFile.exists()) {
            plugin.saveResource(fileName, false);
        }
        
        this.config = YamlConfiguration.loadConfiguration(configFile);
    }

    /**
     * Creates a new hot-reload config instance.
     */
    public static @NotNull HotReloadConfig create(@NotNull Plugin plugin, @NotNull String fileName) {
        return new HotReloadConfig(plugin, fileName);
    }

    /**
     * Registers a reload handler with a unique ID.
     * The handler is called whenever the config file changes.
     */
    public @NotNull HotReloadConfig onReload(@NotNull String id, @NotNull Consumer<FileConfiguration> handler) {
        reloadHandlers.put(id, handler);
        return this;
    }

    /**
     * Registers a reload handler.
     */
    public @NotNull HotReloadConfig onReload(@NotNull Consumer<FileConfiguration> handler) {
        return onReload("default", handler);
    }

    /**
     * Removes a reload handler by ID.
     */
    public void removeHandler(@NotNull String id) {
        reloadHandlers.remove(id);
    }

    /**
     * Starts watching the config file for changes.
     */
    public @NotNull HotReloadConfig start() {
        if (running) {
            return this;
        }

        try {
            Path path = configFile.toPath().getParent();
            watchService = FileSystems.getDefault().newWatchService();
            path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
            
            running = true;
            watchThread = new Thread(this::watchLoop, "IonAPI-ConfigWatcher");
            watchThread.setDaemon(true);
            watchThread.start();
            
            plugin.getLogger().info("Hot-reload enabled for " + configFile.getName());
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to start config watcher", e);
        }

        return this;
    }

    /**
     * Stops watching the config file.
     */
    public void stop() {
        running = false;
        
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Error closing watch service", e);
            }
        }
        
        if (watchThread != null) {
            watchThread.interrupt();
        }
    }

    /**
     * Manually reloads the configuration.
     */
    public void reload() {
        try {
            config = YamlConfiguration.loadConfiguration(configFile);
            
            // Call all handlers
            for (Consumer<FileConfiguration> handler : reloadHandlers.values()) {
                try {
                    handler.accept(config);
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Error in reload handler", e);
                }
            }
            
            plugin.getLogger().info("Configuration reloaded: " + configFile.getName());
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to reload config", e);
        }
    }

    /**
     * Gets the current configuration.
     */
    public @NotNull FileConfiguration getConfig() {
        return config;
    }

    /**
     * Saves the current configuration to disk.
     */
    public void save() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save config", e);
        }
    }

    private void watchLoop() {
        while (running) {
            try {
                WatchKey key = watchService.take();
                
                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                        Path changed = (Path) event.context();
                        
                        if (changed.toString().equals(configFile.getName())) {
                            // Small delay to ensure file write is complete
                            Thread.sleep(100);
                            reload();
                        }
                    }
                }
                
                key.reset();
            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Error in config watch loop", e);
            }
        }
    }
}
