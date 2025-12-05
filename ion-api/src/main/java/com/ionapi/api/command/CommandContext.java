package com.ionapi.api.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Represents the context in which a command is executed.
 */
public interface CommandContext {

    /**
     * Gets the command sender.
     *
     * @return the sender
     */
    @NotNull
    Object getSender();

    /**
     * Gets the command arguments.
     *
     * @return the arguments
     */
    @NotNull
    List<String> getArgs();

    /**
     * Gets an argument at a specific index.
     *
     * @param index the index
     * @return the argument, or null if index is out of bounds
     */
    @Nullable
    String getArg(int index);

    /**
     * Gets an argument at a specific index, or a default value.
     *
     * @param index        the index
     * @param defaultValue the default value
     * @return the argument, or default value if index is out of bounds
     */
    @NotNull
    String getArg(int index, @NotNull String defaultValue);

    /**
     * Gets the number of arguments.
     *
     * @return the argument count
     */
    int getArgCount();

    /**
     * Sends a message to the sender.
     *
     * @param message the message to send
     */
    void reply(@NotNull String message);

    /**
     * Checks if the sender has a permission.
     *
     * @param permission the permission to check
     * @return true if the sender has the permission
     */
    boolean hasPermission(@NotNull String permission);
}
