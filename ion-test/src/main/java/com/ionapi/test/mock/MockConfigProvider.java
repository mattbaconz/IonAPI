package com.ionapi.test.mock;

import com.ionapi.api.config.ConfigurationProvider;
import com.ionapi.api.config.IonConfig;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Mock implementation of ConfigurationProvider for unit testing.
 */
public class MockConfigProvider implements ConfigurationProvider {

    private final Map<String, MockConfig> configs = new HashMap<>();
    private final MockConfig mainConfig = new MockConfig();

    public MockConfigProvider() {
        configs.put("config.yml", mainConfig);
    }

    @Override
    @NotNull
    public IonConfig loadConfig(@NotNull String fileName) {
        return configs.computeIfAbsent(fileName, k -> new MockConfig());
    }

    @Override
    @NotNull
    public IonConfig createConfig(@NotNull String fileName, @NotNull IonConfig defaults) {
        MockConfig config = new MockConfig();
        // Copy defaults
        for (String key : defaults.getKeys(true)) {
            config.set(key, defaults.get(key));
        }
        configs.put(fileName, config);
        return config;
    }

    @Override
    @NotNull
    public IonConfig getConfig() {
        return mainConfig;
    }

    @Override
    public void saveAll() {
        for (MockConfig config : configs.values()) {
            config.save();
        }
    }

    @Override
    public void reloadAll() {
        for (MockConfig config : configs.values()) {
            config.reload();
        }
    }

    // ========== Test Utilities ==========

    /**
     * Gets the main config as MockConfig for test assertions.
     */
    @NotNull
    public MockConfig getMockConfig() {
        return mainConfig;
    }

    /**
     * Gets a specific config as MockConfig.
     */
    @NotNull
    public MockConfig getMockConfig(@NotNull String fileName) {
        return configs.computeIfAbsent(fileName, k -> new MockConfig());
    }

    /**
     * Clears all configs.
     */
    public void reset() {
        configs.clear();
        mainConfig.clear();
        configs.put("config.yml", mainConfig);
    }
}
