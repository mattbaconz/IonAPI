package com.ionapi.database.annotations;

/**
 * Defines cascade operations for entity relationships.
 */
public enum CascadeType {
    
    /**
     * Cascade all operations.
     */
    ALL,
    
    /**
     * Cascade persist/save operations.
     */
    PERSIST,
    
    /**
     * Cascade merge/update operations.
     */
    MERGE,
    
    /**
     * Cascade remove/delete operations.
     */
    REMOVE,
    
    /**
     * Cascade refresh operations.
     */
    REFRESH
}
