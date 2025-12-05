package com.ionapi.api;

import com.ionapi.api.scheduler.IonScheduler;
import org.jetbrains.annotations.NotNull;

/**
 * The main entry point for an Ion plugin.
 * This interface provides access to all core Ion API services.
 */
public interface IonPlugin {

    /**
     * Called when the plugin is enabled.
     */
    void onEnable();

    /**
     * Called when the plugin is disabled.
     */
    void onDisable();

    /**
     * Gets the name of the plugin.
     *
     * @return the plugin name
     */
    @NotNull
    String getName();

    /**
     * Gets the logger for the plugin.
     *
     * @return the logger
     */
    @NotNull
    java.util.logging.Logger getLogger();

    /**
     * Gets the scheduler for this plugin.
     * The scheduler provides a unified API that works across Paper and Folia.
     *
     * @return the scheduler instance
     */
    @NotNull
    IonScheduler getScheduler();

    /**
     * Gets the command registry for this plugin.
     *
     * @return the command registry
     */
    @NotNull
    com.ionapi.api.command.CommandRegistry getCommandRegistry();

    /**
     * Gets the configuration provider for this plugin.
     *
     * @return the configuration provider
     */
    @NotNull
    com.ionapi.api.config.ConfigurationProvider getConfigProvider();

    /**
     * Gets the event bus for this plugin.
     *
     * @return the event bus
     */
    @NotNull
    com.ionapi.api.event.EventBus getEventBus();

    /**
     * Gets the data folder for this plugin.
     *
     * @return the data folder
     */
    @NotNull
    java.io.File getDataFolder();

    /**
     * Gets the platform this plugin is running on.
     *
     * @return the platform name (e.g., "paper", "folia")
     */
    @NotNull
    String getPlatform();
}
