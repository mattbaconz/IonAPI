package com.ionapi.core;

import com.ionapi.api.IonPlugin;
import com.ionapi.api.command.CommandRegistry;
import com.ionapi.api.config.ConfigurationProvider;
import com.ionapi.api.event.EventBus;
import com.ionapi.api.scheduler.IonScheduler;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.logging.Logger;

/**
 * Base implementation of {@link IonPlugin} that extends {@link JavaPlugin}.
 * This class handles the common logic for both Paper and Folia platforms.
 */
public abstract class IonPluginImpl extends JavaPlugin implements IonPlugin {

    private CommandRegistry commandRegistry;
    private ConfigurationProvider configProvider;
    private EventBus eventBus;

    @Override
    public void onEnable() {
        // Initialize core services
        this.commandRegistry = createCommandRegistry();
        this.configProvider = createConfigProvider();
        this.eventBus = createEventBus();

        getLogger().info("IonAPI (" + getPlatform() + ") enabled!");
    }

    @Override
    public void onDisable() {
        // Cleanup logic if needed
        if (eventBus != null) {
            // eventBus.unregisterAll();
        }
        getLogger().info("IonAPI disabled!");
    }

    @Override
    @NotNull
    public CommandRegistry getCommandRegistry() {
        if (commandRegistry == null) {
            throw new IllegalStateException("CommandRegistry not initialized. Is the plugin enabled?");
        }
        return commandRegistry;
    }

    @Override
    @NotNull
    public ConfigurationProvider getConfigProvider() {
        if (configProvider == null) {
            throw new IllegalStateException("ConfigurationProvider not initialized. Is the plugin enabled?");
        }
        return configProvider;
    }

    @Override
    @NotNull
    public EventBus getEventBus() {
        if (eventBus == null) {
            throw new IllegalStateException("EventBus not initialized. Is the plugin enabled?");
        }
        return eventBus;
    }





    // -- Abstract methods to be implemented by platform-specific subclasses or
    // factories --

    /**
     * Creates the command registry instance.
     * Can be overridden by subclasses to provide platform-specific implementations.
     */
    protected CommandRegistry createCommandRegistry() {
        // For now return a simple implementation or look for a factory
        // Since we don't have the implementation details of CommandRegistryImpl here
        // yet,
        // we might need to assume it exists or create a basic one.
        // Assuming a standard implementation exists in ion-api or similar.
        // CHECKME: We need to see where CommandRegistryImpl is.
        // If it doesn't exist, we need to create it or stub it.
        // For this step, I will assume we need to instantiate something.
        // Since I'm not implementing CommandRegistry right now, I'll return null or
        // throw.
        // Actually, to make it valid java, I'll need to implement a dummy or find
        // existing.
        // Let's check imports.
        return new com.ionapi.core.impl.SimpleCommandRegistry(this);
    }

    protected ConfigurationProvider createConfigProvider() {
        return new com.ionapi.core.impl.SimpleConfigProvider(this);
    }

    protected EventBus createEventBus() {
        return new com.ionapi.core.impl.SimpleEventBus(this);
    }

    // IonScheduler is platform specific, so we leave it abstract here?
    // No, IonPlugin interface defines getScheduler().
    // We should abstract the creation or let subclass implement getScheduler().
    // The interface already defines getScheduler(), so we just don't implement it
    // here
    // and let the subclass (IonPaperPlugin / IonFoliaPlugin) implement it.

    @Override
    @NotNull
    public abstract IonScheduler getScheduler();
}
