package com.ionapi.test.mock;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Mock logger for unit testing that captures all log messages.
 */
public class MockLogger extends Logger {

    private final List<LogEntry> entries = new ArrayList<>();
    private boolean printToConsole = false;

    public MockLogger(@NotNull String name) {
        super(name, null);
        setLevel(Level.ALL);
        addHandler(new CaptureHandler());
    }

    /**
     * Sets whether to also print logs to console.
     */
    public void setPrintToConsole(boolean print) {
        this.printToConsole = print;
    }

    /**
     * Gets all captured log entries.
     */
    @NotNull
    public List<LogEntry> getEntries() {
        return new ArrayList<>(entries);
    }

    /**
     * Gets entries at a specific level.
     */
    @NotNull
    public List<LogEntry> getEntries(@NotNull Level level) {
        List<LogEntry> result = new ArrayList<>();
        for (LogEntry entry : entries) {
            if (entry.level.equals(level)) {
                result.add(entry);
            }
        }
        return result;
    }

    /**
     * Gets all info-level messages.
     */
    @NotNull
    public List<String> getInfoMessages() {
        return getMessages(Level.INFO);
    }

    /**
     * Gets all warning-level messages.
     */
    @NotNull
    public List<String> getWarningMessages() {
        return getMessages(Level.WARNING);
    }

    /**
     * Gets all severe-level messages.
     */
    @NotNull
    public List<String> getSevereMessages() {
        return getMessages(Level.SEVERE);
    }

    /**
     * Gets messages at a specific level.
     */
    @NotNull
    public List<String> getMessages(@NotNull Level level) {
        List<String> result = new ArrayList<>();
        for (LogEntry entry : entries) {
            if (entry.level.equals(level)) {
                result.add(entry.message);
            }
        }
        return result;
    }

    /**
     * Checks if a message was logged at any level.
     */
    public boolean hasMessage(@NotNull String message) {
        for (LogEntry entry : entries) {
            if (entry.message.contains(message)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a message was logged at a specific level.
     */
    public boolean hasMessage(@NotNull Level level, @NotNull String message) {
        for (LogEntry entry : entries) {
            if (entry.level.equals(level) && entry.message.contains(message)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Clears all captured entries.
     */
    public void clear() {
        entries.clear();
    }

    private class CaptureHandler extends Handler {
        @Override
        public void publish(LogRecord record) {
            entries.add(new LogEntry(record.getLevel(), record.getMessage(), record.getThrown()));
            if (printToConsole) {
                System.out.println("[" + record.getLevel() + "] " + record.getMessage());
                if (record.getThrown() != null) {
                    record.getThrown().printStackTrace();
                }
            }
        }

        @Override
        public void flush() {}

        @Override
        public void close() {}
    }

    /**
     * Represents a captured log entry.
     */
    public static class LogEntry {
        public final Level level;
        public final String message;
        public final Throwable thrown;

        public LogEntry(Level level, String message, Throwable thrown) {
            this.level = level;
            this.message = message;
            this.thrown = thrown;
        }

        @Override
        public String toString() {
            return "[" + level + "] " + message;
        }
    }
}
