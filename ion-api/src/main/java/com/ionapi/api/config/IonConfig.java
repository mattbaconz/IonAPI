package com.ionapi.api.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a configuration file.
 */
public interface IonConfig {

    /**
     * Gets a value from the config.
     *
     * @param path the config path (e.g., "database.host")
     * @return the value, or null if not found
     */
    @Nullable
    Object get(@NotNull String path);

    /**
     * Gets a value from the config with a default.
     *
     * @param path         the config path
     * @param defaultValue the default value
     * @return the value, or default if not found
     */
    @NotNull
    Object get(@NotNull String path, @NotNull Object defaultValue);

    /**
     * Gets a string value.
     *
     * @param path the config path
     * @return the string value, or null if not found
     */
    @Nullable
    String getString(@NotNull String path);

    /**
     * Gets a string value with a default.
     *
     * @param path         the config path
     * @param defaultValue the default value
     * @return the string value, or default if not found
     */
    @NotNull
    String getString(@NotNull String path, @NotNull String defaultValue);

    /**
     * Gets an integer value.
     *
     * @param path the config path
     * @return the integer value, or 0 if not found
     */
    int getInt(@NotNull String path);

    /**
     * Gets an integer value with a default.
     *
     * @param path         the config path
     * @param defaultValue the default value
     * @return the integer value, or default if not found
     */
    int getInt(@NotNull String path, int defaultValue);

    /**
     * Gets a double value.
     *
     * @param path the config path
     * @return the double value, or 0.0 if not found
     */
    double getDouble(@NotNull String path);

    /**
     * Gets a double value with a default.
     *
     * @param path         the config path
     * @param defaultValue the default value
     * @return the double value, or default if not found
     */
    double getDouble(@NotNull String path, double defaultValue);

    /**
     * Gets a boolean value.
     *
     * @param path the config path
     * @return the boolean value, or false if not found
     */
    boolean getBoolean(@NotNull String path);

    /**
     * Gets a boolean value with a default.
     *
     * @param path         the config path
     * @param defaultValue the default value
     * @return the boolean value, or default if not found
     */
    boolean getBoolean(@NotNull String path, boolean defaultValue);

    /**
     * Gets a list value.
     *
     * @param path the config path
     * @return the list value, or empty list if not found
     */
    @NotNull
    List<?> getList(@NotNull String path);

    /**
     * Gets a string list value.
     *
     * @param path the config path
     * @return the string list value, or empty list if not found
     */
    @NotNull
    List<String> getStringList(@NotNull String path);

    /**
     * Sets a value in the config.
     *
     * @param path  the config path
     * @param value the value to set
     */
    void set(@NotNull String path, @Nullable Object value);

    /**
     * Checks if a path exists in the config.
     *
     * @param path the config path
     * @return true if the path exists
     */
    boolean contains(@NotNull String path);

    /**
     * Gets all keys in the config.
     *
     * @param deep whether to get nested keys
     * @return set of all keys
     */
    @NotNull
    Set<String> getKeys(boolean deep);

    /**
     * Gets a section of the config as a map.
     *
     * @param path the config path
     * @return the section as a map, or empty map if not found
     */
    @NotNull
    Map<String, Object> getSection(@NotNull String path);

    /**
     * Saves the config to disk.
     */
    void save();

    /**
     * Reloads the config from disk.
     */
    void reload();
}
