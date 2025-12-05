package com.ionapi.api.config;

import org.jetbrains.annotations.NotNull;

/**
 * Provider for creating and managing configuration files.
 */
public interface ConfigurationProvider {

    /**
     * Loads a config file.
     *
     * @param fileName the file name (e.g., "config.yml")
     * @return the loaded config
     */
    @NotNull
    IonConfig loadConfig(@NotNull String fileName);

    /**
     * Creates a new config file with defaults.
     *
     * @param fileName the file name
     * @param defaults the default values
     * @return the created config
     */
    @NotNull
    IonConfig createConfig(@NotNull String fileName, @NotNull IonConfig defaults);

    /**
     * Gets the main config file (config.yml).
     *
     * @return the main config
     */
    @NotNull
    IonConfig getConfig();

    /**
     * Saves all loaded configs.
     */
    void saveAll();

    /**
     * Reloads all loaded configs.
     */
    void reloadAll();
}
