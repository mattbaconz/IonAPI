package com.ionapi.test.mock;

import com.ionapi.api.IonPlugin;
import com.ionapi.api.command.CommandRegistry;
import com.ionapi.api.config.ConfigurationProvider;
import com.ionapi.api.event.EventBus;
import com.ionapi.api.scheduler.IonScheduler;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.logging.Logger;

/**
 * Mock implementation of IonPlugin for unit testing.
 * Provides access to all mock components for test assertions.
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * @Test
 * void testMyFeature() {
 *     MockIonPlugin plugin = IonTest.createMockPlugin("TestPlugin");
 *     
 *     // Configure mock
 *     plugin.getMockConfig().set("feature.enabled", true);
 *     
 *     // Test your code
 *     MyFeature feature = new MyFeature(plugin);
 *     feature.doSomething();
 *     
 *     // Verify scheduler was used
 *     assertEquals(1, plugin.getMockScheduler().getPendingTasks().size());
 *     
 *     // Verify events were fired
 *     assertTrue(plugin.getMockEventBus().wasFired(MyEvent.class));
 * }
 * }</pre>
 */
public class MockIonPlugin implements IonPlugin {

    private final String name;
    private final MockScheduler scheduler;
    private final MockCommandRegistry commandRegistry;
    private final MockConfigProvider configProvider;
    private final MockEventBus eventBus;
    private final MockLogger logger;
    private final File dataFolder;
    private String platform = "paper";
    private boolean enabled = false;

    public MockIonPlugin(@NotNull String name) {
        this.name = name;
        this.scheduler = new MockScheduler();
        this.commandRegistry = new MockCommandRegistry();
        this.configProvider = new MockConfigProvider();
        this.eventBus = new MockEventBus();
        this.logger = new MockLogger(name);
        this.dataFolder = new File(System.getProperty("java.io.tmpdir"), "ion-test-" + name);
    }

    @Override
    public void onEnable() {
        enabled = true;
    }

    @Override
    public void onDisable() {
        enabled = false;
    }

    @Override
    @NotNull
    public String getName() {
        return name;
    }

    @Override
    @NotNull
    public Logger getLogger() {
        return logger;
    }

    @Override
    @NotNull
    public IonScheduler getScheduler() {
        return scheduler;
    }

    @Override
    @NotNull
    public CommandRegistry getCommandRegistry() {
        return commandRegistry;
    }

    @Override
    @NotNull
    public ConfigurationProvider getConfigProvider() {
        return configProvider;
    }

    @Override
    @NotNull
    public EventBus getEventBus() {
        return eventBus;
    }

    @Override
    @NotNull
    public File getDataFolder() {
        return dataFolder;
    }

    @Override
    @NotNull
    public String getPlatform() {
        return platform;
    }

    // ========== Mock Accessors ==========

    /**
     * Gets the mock scheduler for test assertions.
     */
    @NotNull
    public MockScheduler getMockScheduler() {
        return scheduler;
    }

    /**
     * Gets the mock command registry for test assertions.
     */
    @NotNull
    public MockCommandRegistry getMockCommandRegistry() {
        return commandRegistry;
    }

    /**
     * Gets the mock config provider for test assertions.
     */
    @NotNull
    public MockConfigProvider getMockConfigProvider() {
        return configProvider;
    }

    /**
     * Gets the main mock config for test assertions.
     */
    @NotNull
    public MockConfig getMockConfig() {
        return configProvider.getMockConfig();
    }

    /**
     * Gets the mock event bus for test assertions.
     */
    @NotNull
    public MockEventBus getMockEventBus() {
        return eventBus;
    }

    /**
     * Gets the mock logger for test assertions.
     */
    @NotNull
    public MockLogger getMockLogger() {
        return logger;
    }

    // ========== Test Utilities ==========

    /**
     * Sets the platform name (for testing platform-specific code).
     */
    public void setPlatform(@NotNull String platform) {
        this.platform = platform;
    }

    /**
     * Checks if the plugin is enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Resets all mock components to their initial state.
     */
    public void reset() {
        scheduler.reset();
        commandRegistry.reset();
        configProvider.reset();
        eventBus.reset();
        logger.clear();
        enabled = false;
    }
}
