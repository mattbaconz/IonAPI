package com.ionapi.api.command;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a command that can be executed.
 */
public interface IonCommand {

    /**
     * Executes this command.
     *
     * @param context the command context
     * @return true if the command executed successfully
     */
    boolean execute(@NotNull CommandContext context);

    /**
     * Gets the name of this command.
     *
     * @return the command name
     */
    @NotNull
    String getName();

    /**
     * Gets the description of this command.
     *
     * @return the command description
     */
    @NotNull
    String getDescription();

    /**
     * Gets the usage string for this command.
     *
     * @return the usage string
     */
    @NotNull
    String getUsage();

    /**
     * Gets the permission required to use this command.
     *
     * @return the permission, or null if no permission required
     */
    @NotNull
    String getPermission();
}
