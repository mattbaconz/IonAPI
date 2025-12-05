package com.ionapi.api.command;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Registry for managing commands.
 */
public interface CommandRegistry {

    /**
     * Registers a command.
     *
     * @param command the command to register
     */
    void register(@NotNull IonCommand command);

    /**
     * Unregisters a command by name.
     *
     * @param name the command name
     * @return true if a command was unregistered
     */
    boolean unregister(@NotNull String name);

    /**
     * Gets a command by name.
     *
     * @param name the command name
     * @return the command, or null if not found
     */
    IonCommand getCommand(@NotNull String name);

    /**
     * Gets all registered commands.
     *
     * @return list of all commands
     */
    @NotNull
    List<IonCommand> getCommands();

    /**
     * Unregisters all commands for a plugin.
     */
    void unregisterAll();
}
