package com.ionapi.database;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Batch operations for efficient bulk database operations.
 * Significantly faster than individual operations for large datasets.
 *
 * @param <T> the entity type
 * @since 1.2.0
 */
public interface BatchOperation<T> {

    /**
     * Adds an entity to the batch for insertion.
     *
     * @param entity the entity to insert
     * @return this batch operation for chaining
     */
    @NotNull BatchOperation<T> insert(@NotNull T entity);

    /**
     * Adds multiple entities to the batch for insertion.
     *
     * @param entities the entities to insert
     * @return this batch operation for chaining
     */
    @NotNull BatchOperation<T> insertAll(@NotNull List<T> entities);

    /**
     * Adds an entity to the batch for update.
     *
     * @param entity the entity to update
     * @return this batch operation for chaining
     */
    @NotNull BatchOperation<T> update(@NotNull T entity);

    /**
     * Adds multiple entities to the batch for update.
     *
     * @param entities the entities to update
     * @return this batch operation for chaining
     */
    @NotNull BatchOperation<T> updateAll(@NotNull List<T> entities);

    /**
     * Adds an entity to the batch for deletion.
     *
     * @param entity the entity to delete
     * @return this batch operation for chaining
     */
    @NotNull BatchOperation<T> delete(@NotNull T entity);

    /**
     * Adds multiple entities to the batch for deletion.
     *
     * @param entities the entities to delete
     * @return this batch operation for chaining
     */
    @NotNull BatchOperation<T> deleteAll(@NotNull List<T> entities);

    /**
     * Sets the batch size for execution.
     *
     * @param size the batch size (default: 1000)
     * @return this batch operation for chaining
     */
    @NotNull BatchOperation<T> batchSize(int size);

    /**
     * Executes all batched operations synchronously.
     *
     * @return the result containing affected row counts
     * @throws DatabaseException if execution fails
     */
    @NotNull BatchResult execute() throws DatabaseException;

    /**
     * Executes all batched operations asynchronously.
     *
     * @return a future containing the result
     */
    @NotNull CompletableFuture<BatchResult> executeAsync();

    /**
     * Result of a batch operation.
     */
    record BatchResult(int insertedCount, int updatedCount, int deletedCount, long executionTimeMs) {
        public int totalAffected() {
            return insertedCount + updatedCount + deletedCount;
        }
    }
}
