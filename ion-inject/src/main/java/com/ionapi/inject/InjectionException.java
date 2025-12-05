package com.ionapi.inject;

/**
 * Exception thrown when dependency injection fails.
 */
public class InjectionException extends RuntimeException {

    public InjectionException(String message) {
        super(message);
    }

    public InjectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
