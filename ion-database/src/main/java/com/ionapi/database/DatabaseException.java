package com.ionapi.database;

/**
 * Exception thrown when database operations fail.
 */
public class DatabaseException extends Exception {

    /**
     * Creates a new database exception with a message.
     *
     * @param message the error message
     */
    public DatabaseException(String message) {
        super(message);
    }

    /**
     * Creates a new database exception with a message and cause.
     *
     * @param message the error message
     * @param cause the underlying cause
     */
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new database exception with a cause.
     *
     * @param cause the underlying cause
     */
    public DatabaseException(Throwable cause) {
        super(cause);
    }
}
