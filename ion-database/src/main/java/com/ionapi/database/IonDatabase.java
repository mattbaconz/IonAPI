package com.ionapi.database;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Fluent API for database operations with ORM support.
 * <p>
 * Example usage:
 * <pre>{@code
 * // Connect to database
 * IonDatabase db = IonDatabase.builder()
 *     .type(DatabaseType.MYSQL)
 *     .host("localhost")
 *     .port(3306)
 *     .database("mydb")
 *     .username("user")
 *     .password("pass")
 *     .build();
 *
 * // Define entity
 * @Table("players")
 * public class PlayerData {
 *     @PrimaryKey
 *     private UUID uuid;
 *     private String name;
 *     private int level;
 *     private double balance;
 *     // getters/setters
 * }
 *
 * // Query
 * PlayerData data = db.find(PlayerData.class, playerUuid);
 * data.setBalance(data.getBalance() + 100);
 * db.save(data);
 *
 * // Async query
 * db.findAsync(PlayerData.class, playerUuid)
 *     .thenAccept(data -> {
 *         // Process data on async thread
 *     });
 * }</pre>
 */
public interface IonDatabase {

    /**
     * Creates a new database builder.
     *
     * @return a new database builder
     */
    @NotNull
    static IonDatabaseBuilder builder() {
        return new IonDatabaseBuilder();
    }

    /**
     * Creates a database connection with a JDBC URL.
     *
     * @param jdbcUrl the JDBC connection URL
     * @return a new database builder
     */
    @NotNull
    static IonDatabaseBuilder connect(@NotNull String jdbcUrl) {
        return new IonDatabaseBuilder().jdbcUrl(jdbcUrl);
    }

    /**
     * Creates a SQLite database connection.
     *
     * @param file the database file path
     * @return a new database builder
     */
    @NotNull
    static IonDatabaseBuilder sqlite(@NotNull String file) {
        return new IonDatabaseBuilder().type(DatabaseType.SQLITE).database(file);
    }

    /**
     * Gets the database type.
     *
     * @return the database type
     */
    @NotNull
    DatabaseType getType();

    /**
     * Checks if the database is connected.
     *
     * @return true if connected
     */
    boolean isConnected();

    /**
     * Connects to the database.
     *
     * @throws DatabaseException if connection fails
     */
    void connect() throws DatabaseException;

    /**
     * Disconnects from the database and closes all connections.
     */
    void disconnect();

    /**
     * Gets a connection from the pool.
     *
     * @return a database connection
     * @throws DatabaseException if unable to get connection
     */
    @NotNull
    Connection getConnection() throws DatabaseException;

    /**
     * Executes a raw SQL query.
     *
     * @param sql the SQL query
     * @param params the query parameters
     * @return the result set
     * @throws DatabaseException if query fails
     */
    @NotNull
    ResultSet query(@NotNull String sql, @NotNull Object... params) throws DatabaseException;

    /**
     * Executes a raw SQL update/insert/delete.
     *
     * @param sql the SQL statement
     * @param params the statement parameters
     * @return the number of affected rows
     * @throws DatabaseException if execution fails
     */
    int execute(@NotNull String sql, @NotNull Object... params) throws DatabaseException;

    /**
     * Executes a raw SQL statement asynchronously.
     *
     * @param sql the SQL statement
     * @param params the statement parameters
     * @return a completable future with affected row count
     */
    @NotNull
    CompletableFuture<Integer> executeAsync(@NotNull String sql, @NotNull Object... params);

    /**
     * Executes a query asynchronously.
     *
     * @param sql the SQL query
     * @param params the query parameters
     * @return a completable future with the result set
     */
    @NotNull
    CompletableFuture<ResultSet> queryAsync(@NotNull String sql, @NotNull Object... params);

    /**
     * Creates a query builder for type-safe queries.
     *
     * @param entityClass the entity class
     * @param <T> the entity type
     * @return a query builder
     */
    @NotNull
    <T> QueryBuilder<T> select(@NotNull Class<T> entityClass);

    /**
     * Finds an entity by its primary key.
     *
     * @param entityClass the entity class
     * @param primaryKey the primary key value
     * @param <T> the entity type
     * @return the entity, or null if not found
     * @throws DatabaseException if query fails
     */
    @Nullable
    <T> T find(@NotNull Class<T> entityClass, @NotNull Object primaryKey) throws DatabaseException;

    /**
     * Finds an entity by its primary key asynchronously.
     *
     * @param entityClass the entity class
     * @param primaryKey the primary key value
     * @param <T> the entity type
     * @return a completable future with the entity (or null)
     */
    @NotNull
    <T> CompletableFuture<Optional<T>> findAsync(@NotNull Class<T> entityClass, @NotNull Object primaryKey);

    /**
     * Finds all entities of a specific type.
     *
     * @param entityClass the entity class
     * @param <T> the entity type
     * @return list of all entities
     * @throws DatabaseException if query fails
     */
    @NotNull
    <T> List<T> findAll(@NotNull Class<T> entityClass) throws DatabaseException;

    /**
     * Finds all entities asynchronously.
     *
     * @param entityClass the entity class
     * @param <T> the entity type
     * @return a completable future with the list of entities
     */
    @NotNull
    <T> CompletableFuture<List<T>> findAllAsync(@NotNull Class<T> entityClass);

    /**
     * Saves an entity (insert or update).
     *
     * @param entity the entity to save
     * @param <T> the entity type
     * @throws DatabaseException if save fails
     */
    <T> void save(@NotNull T entity) throws DatabaseException;

    /**
     * Saves an entity asynchronously.
     *
     * @param entity the entity to save
     * @param <T> the entity type
     * @return a completable future
     */
    @NotNull
    <T> CompletableFuture<Void> saveAsync(@NotNull T entity);

    /**
     * Inserts a new entity.
     *
     * @param entity the entity to insert
     * @param <T> the entity type
     * @throws DatabaseException if insert fails
     */
    <T> void insert(@NotNull T entity) throws DatabaseException;

    /**
     * Updates an existing entity.
     *
     * @param entity the entity to update
     * @param <T> the entity type
     * @throws DatabaseException if update fails
     */
    <T> void update(@NotNull T entity) throws DatabaseException;

    /**
     * Deletes an entity.
     *
     * @param entity the entity to delete
     * @param <T> the entity type
     * @throws DatabaseException if delete fails
     */
    <T> void delete(@NotNull T entity) throws DatabaseException;

    /**
     * Deletes an entity asynchronously.
     *
     * @param entity the entity to delete
     * @param <T> the entity type
     * @return a completable future
     */
    @NotNull
    <T> CompletableFuture<Void> deleteAsync(@NotNull T entity);

    /**
     * Deletes an entity by its primary key.
     *
     * @param entityClass the entity class
     * @param primaryKey the primary key value
     * @param <T> the entity type
     * @return true if deleted, false if not found
     * @throws DatabaseException if delete fails
     */
    <T> boolean deleteById(@NotNull Class<T> entityClass, @NotNull Object primaryKey) throws DatabaseException;

    /**
     * Creates the table for an entity if it doesn't exist.
     *
     * @param entityClass the entity class
     * @param <T> the entity type
     * @throws DatabaseException if table creation fails
     */
    <T> void createTable(@NotNull Class<T> entityClass) throws DatabaseException;

    /**
     * Drops the table for an entity.
     *
     * @param entityClass the entity class
     * @param <T> the entity type
     * @throws DatabaseException if table drop fails
     */
    <T> void dropTable(@NotNull Class<T> entityClass) throws DatabaseException;

    /**
     * Checks if a table exists.
     *
     * @param tableName the table name
     * @return true if the table exists
     * @throws DatabaseException if check fails
     */
    boolean tableExists(@NotNull String tableName) throws DatabaseException;

    /**
     * Executes a transaction.
     *
     * @param transaction the transaction operations
     * @throws DatabaseException if transaction fails
     */
    void transaction(@NotNull Consumer<IonDatabase> transaction) throws DatabaseException;

    /**
     * Executes a transaction asynchronously.
     *
     * @param transaction the transaction operations
     * @return a completable future
     */
    @NotNull
    CompletableFuture<Void> transactionAsync(@NotNull Consumer<IonDatabase> transaction);

    /**
     * Executes a transaction that returns a value.
     *
     * @param transaction the transaction function
     * @param <T> the return type
     * @return the transaction result
     * @throws DatabaseException if transaction fails
     */
    <T> T transactionWithResult(@NotNull Function<IonDatabase, T> transaction) throws DatabaseException;

    /**
     * Begins a manual transaction.
     * Must be committed or rolled back manually.
     *
     * @return a transaction handle
     * @throws DatabaseException if transaction cannot be started
     */
    @NotNull
    Transaction beginTransaction() throws DatabaseException;

    /**
     * Gets database statistics.
     *
     * @return database statistics
     */
    @NotNull
    DatabaseStats getStats();

    /**
     * Enables query logging for debugging.
     *
     * @param enabled true to enable logging
     */
    void setQueryLogging(boolean enabled);

    /**
     * Checks if query logging is enabled.
     *
     * @return true if logging is enabled
     */
    boolean isQueryLoggingEnabled();

    /**
     * Creates a batch operation for efficient bulk operations.
     *
     * @param entityClass the entity class
     * @param <T> the entity type
     * @return a batch operation builder
     * @since 1.2.0
     */
    @NotNull
    <T> BatchOperation<T> batch(@NotNull Class<T> entityClass);
}
