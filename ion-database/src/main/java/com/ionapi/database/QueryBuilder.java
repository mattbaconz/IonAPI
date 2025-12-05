package com.ionapi.database;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Type-safe query builder for database operations.
 * <p>
 * Example usage:
 * <pre>{@code
 * List<PlayerData> players = db.select(PlayerData.class)
 *     .where("level", ">", 10)
 *     .and("coins", ">", 1000)
 *     .orderBy("level", "DESC")
 *     .limit(10)
 *     .execute();
 * }</pre>
 *
 * @param <T> the entity type
 */
public interface QueryBuilder<T> {

    /**
     * Adds a WHERE condition.
     *
     * @param column the column name
     * @param operator the comparison operator (=, !=, >, <, >=, <=, LIKE, etc.)
     * @param value the value to compare against
     * @return this query builder
     */
    @NotNull
    QueryBuilder<T> where(@NotNull String column, @NotNull String operator, @NotNull Object value);

    /**
     * Adds a WHERE condition with equals operator.
     *
     * @param column the column name
     * @param value the value to match
     * @return this query builder
     */
    @NotNull
    QueryBuilder<T> where(@NotNull String column, @NotNull Object value);

    /**
     * Adds an AND condition.
     *
     * @param column the column name
     * @param operator the comparison operator
     * @param value the value to compare against
     * @return this query builder
     */
    @NotNull
    QueryBuilder<T> and(@NotNull String column, @NotNull String operator, @NotNull Object value);

    /**
     * Adds an AND condition with equals operator.
     *
     * @param column the column name
     * @param value the value to match
     * @return this query builder
     */
    @NotNull
    QueryBuilder<T> and(@NotNull String column, @NotNull Object value);

    /**
     * Adds an OR condition.
     *
     * @param column the column name
     * @param operator the comparison operator
     * @param value the value to compare against
     * @return this query builder
     */
    @NotNull
    QueryBuilder<T> or(@NotNull String column, @NotNull String operator, @NotNull Object value);

    /**
     * Adds an OR condition with equals operator.
     *
     * @param column the column name
     * @param value the value to match
     * @return this query builder
     */
    @NotNull
    QueryBuilder<T> or(@NotNull String column, @NotNull Object value);

    /**
     * Adds an ORDER BY clause.
     *
     * @param column the column to order by
     * @return this query builder
     */
    @NotNull
    QueryBuilder<T> orderBy(@NotNull String column);

    /**
     * Adds an ORDER BY clause with direction.
     *
     * @param column the column to order by
     * @param direction the direction (ASC or DESC)
     * @return this query builder
     */
    @NotNull
    QueryBuilder<T> orderBy(@NotNull String column, @NotNull String direction);

    /**
     * Sets the maximum number of results to return.
     *
     * @param limit the maximum number of results
     * @return this query builder
     */
    @NotNull
    QueryBuilder<T> limit(int limit);

    /**
     * Sets the number of results to skip.
     *
     * @param offset the number of results to skip
     * @return this query builder
     */
    @NotNull
    QueryBuilder<T> offset(int offset);

    /**
     * Executes the query and returns all matching results.
     *
     * @return the list of matching entities
     * @throws DatabaseException if the query fails
     */
    @NotNull
    List<T> execute() throws DatabaseException;

    /**
     * Executes the query asynchronously.
     *
     * @return a completable future with the list of results
     */
    @NotNull
    CompletableFuture<List<T>> executeAsync();

    /**
     * Executes the query and returns the first result.
     *
     * @return the first matching entity, or null if not found
     * @throws DatabaseException if the query fails
     */
    T first() throws DatabaseException;

    /**
     * Executes the query asynchronously and returns the first result.
     *
     * @return a completable future with the first result
     */
    @NotNull
    CompletableFuture<T> firstAsync();

    /**
     * Counts the number of matching results without fetching them.
     *
     * @return the count of matching results
     * @throws DatabaseException if the query fails
     */
    int count() throws DatabaseException;

    /**
     * Counts the number of matching results asynchronously.
     *
     * @return a completable future with the count
     */
    @NotNull
    CompletableFuture<Integer> countAsync();

    /**
     * Checks if any results match the query.
     *
     * @return true if at least one result matches
     * @throws DatabaseException if the query fails
     */
    boolean exists() throws DatabaseException;

    /**
     * Deletes all entities matching the query.
     *
     * @return the number of deleted entities
     * @throws DatabaseException if the deletion fails
     */
    int delete() throws DatabaseException;

    /**
     * Builds the SQL query string for debugging.
     *
     * @return the SQL query string
     */
    @NotNull
    String toSql();
}
