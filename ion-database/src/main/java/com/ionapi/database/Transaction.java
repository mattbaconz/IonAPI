package com.ionapi.database;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a database transaction for atomic operations.
 * <p>
 * Example usage:
 * <pre>{@code
 * Transaction tx = database.beginTransaction();
 * try {
 *     database.save(entity1);
 *     database.save(entity2);
 *     database.delete(entity3);
 *
 *     tx.commit();
 * } catch (Exception e) {
 *     tx.rollback();
 *     throw e;
 * }
 * }</pre>
 */
public interface Transaction {

    /**
     * Commits the transaction, making all changes permanent.
     *
     * @throws DatabaseException if the commit fails
     */
    void commit() throws DatabaseException;

    /**
     * Rolls back the transaction, undoing all changes.
     *
     * @throws DatabaseException if the rollback fails
     */
    void rollback() throws DatabaseException;

    /**
     * Checks if the transaction is active.
     *
     * @return true if the transaction is active
     */
    boolean isActive();

    /**
     * Checks if the transaction has been committed.
     *
     * @return true if committed
     */
    boolean isCommitted();

    /**
     * Checks if the transaction has been rolled back.
     *
     * @return true if rolled back
     */
    boolean isRolledBack();

    /**
     * Sets a savepoint in the transaction.
     * Allows partial rollback to this point.
     *
     * @param name the savepoint name
     * @throws DatabaseException if savepoint creation fails
     */
    void setSavepoint(@NotNull String name) throws DatabaseException;

    /**
     * Rolls back to a specific savepoint.
     *
     * @param name the savepoint name
     * @throws DatabaseException if rollback fails
     */
    void rollbackToSavepoint(@NotNull String name) throws DatabaseException;

    /**
     * Releases a savepoint.
     *
     * @param name the savepoint name
     * @throws DatabaseException if release fails
     */
    void releaseSavepoint(@NotNull String name) throws DatabaseException;

    /**
     * Gets the underlying database connection for this transaction.
     *
     * @return the database connection
     */
    @NotNull
    java.sql.Connection getConnection();

    /**
     * Closes the transaction and releases resources.
     * If not committed or rolled back, automatically rolls back.
     */
    void close();
}
