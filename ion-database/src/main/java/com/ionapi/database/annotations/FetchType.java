package com.ionapi.database.annotations;

/**
 * Defines when related entities should be loaded.
 */
public enum FetchType {
    
    /**
     * Load related entities immediately with the parent.
     */
    EAGER,
    
    /**
     * Load related entities only when accessed.
     */
    LAZY
}
