package com.ionapi.test.mock;

import com.ionapi.api.command.CommandRegistry;
import com.ionapi.api.command.IonCommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Mock implementation of CommandRegistry for unit testing.
 */
public class MockCommandRegistry implements CommandRegistry {

    private final Map<String, IonCommand> commands = new LinkedHashMap<>();

    @Override
    public void register(@NotNull IonCommand command) {
        commands.put(command.getName().toLowerCase(), command);
    }

    @Override
    public boolean unregister(@NotNull String name) {
        return commands.remove(name.toLowerCase()) != null;
    }

    @Override
    @Nullable
    public IonCommand getCommand(@NotNull String name) {
        return commands.get(name.toLowerCase());
    }

    @Override
    @NotNull
    public List<IonCommand> getCommands() {
        // Return unique commands (not aliases)
        return new ArrayList<>(commands.values().stream().distinct().toList());
    }

    @Override
    public void unregisterAll() {
        commands.clear();
    }

    // ========== Test Utilities ==========

    /**
     * Checks if a command is registered.
     */
    public boolean isRegistered(@NotNull String name) {
        return commands.containsKey(name.toLowerCase());
    }

    /**
     * Gets the number of registered commands (excluding aliases).
     */
    public int getCommandCount() {
        return (int) commands.values().stream().distinct().count();
    }

    /**
     * Resets the registry.
     */
    public void reset() {
        commands.clear();
    }
}
