package com.ionapi.database;

import com.ionapi.database.impl.IonDatabaseImpl;
import org.jetbrains.annotations.NotNull;

/**
 * Builder for creating IonDatabase instances.
 * <p>
 * Example usage:
 * <pre>{@code
 * IonDatabase db = IonDatabase.builder()
 *     .type(DatabaseType.MYSQL)
 *     .host("localhost")
 *     .port(3306)
 *     .database("mydb")
 *     .username("user")
 *     .password("pass")
 *     .poolSize(10)
 *     .build();
 * }</pre>
 */
public class IonDatabaseBuilder {

    private DatabaseType type;
    private String host;
    private int port;
    private String database;
    private String username;
    private String password;
    private String jdbcUrl;
    private int poolSize = 10;
    private int connectionTimeout = 30000;
    private int maxLifetime = 1800000;
    private boolean autoCommit = true;

    /**
     * Creates a new database builder.
     */
    public IonDatabaseBuilder() {
        this.type = DatabaseType.SQLITE;
        this.host = "localhost";
        this.port = 0;
    }

    /**
     * Sets the database type.
     *
     * @param type the database type
     * @return this builder
     */
    @NotNull
    public IonDatabaseBuilder type(@NotNull DatabaseType type) {
        this.type = type;
        return this;
    }

    /**
     * Sets the database host.
     *
     * @param host the host address
     * @return this builder
     */
    @NotNull
    public IonDatabaseBuilder host(@NotNull String host) {
        this.host = host;
        return this;
    }

    /**
     * Sets the database port.
     *
     * @param port the port number
     * @return this builder
     */
    @NotNull
    public IonDatabaseBuilder port(int port) {
        this.port = port;
        return this;
    }

    /**
     * Sets the database name or file path.
     *
     * @param database the database name
     * @return this builder
     */
    @NotNull
    public IonDatabaseBuilder database(@NotNull String database) {
        this.database = database;
        return this;
    }

    /**
     * Sets the database username.
     *
     * @param username the username
     * @return this builder
     */
    @NotNull
    public IonDatabaseBuilder username(@NotNull String username) {
        this.username = username;
        return this;
    }

    /**
     * Sets the database password.
     *
     * @param password the password
     * @return this builder
     */
    @NotNull
    public IonDatabaseBuilder password(@NotNull String password) {
        this.password = password;
        return this;
    }

    /**
     * Sets a custom JDBC URL.
     * When set, this overrides host, port, and database settings.
     *
     * @param jdbcUrl the JDBC URL
     * @return this builder
     */
    @NotNull
    public IonDatabaseBuilder jdbcUrl(@NotNull String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
        return this;
    }

    /**
     * Sets the connection pool size.
     *
     * @param poolSize the maximum number of connections in the pool
     * @return this builder
     */
    @NotNull
    public IonDatabaseBuilder poolSize(int poolSize) {
        this.poolSize = Math.max(1, poolSize);
        return this;
    }

    /**
     * Sets the connection timeout in milliseconds.
     *
     * @param connectionTimeout the timeout in milliseconds
     * @return this builder
     */
    @NotNull
    public IonDatabaseBuilder connectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    /**
     * Sets the maximum lifetime of a connection in milliseconds.
     *
     * @param maxLifetime the max lifetime in milliseconds
     * @return this builder
     */
    @NotNull
    public IonDatabaseBuilder maxLifetime(int maxLifetime) {
        this.maxLifetime = maxLifetime;
        return this;
    }

    /**
     * Sets whether connections should auto-commit.
     *
     * @param autoCommit true to enable auto-commit
     * @return this builder
     */
    @NotNull
    public IonDatabaseBuilder autoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
        return this;
    }

    /**
     * Builds the IonDatabase instance.
     *
     * @return the configured database instance
     * @throws IllegalStateException if required configuration is missing
     */
    @NotNull
    public IonDatabase build() {
        validate();

        return new IonDatabaseImpl(
            type,
            getJdbcUrl(),
            username,
            password,
            poolSize,
            connectionTimeout,
            maxLifetime,
            autoCommit
        );
    }

    /**
     * Validates the builder configuration.
     *
     * @throws IllegalStateException if configuration is invalid
     */
    private void validate() {
        if (jdbcUrl == null) {
            if (type == null) {
                throw new IllegalStateException("Database type must be specified");
            }
            if (database == null || database.isEmpty()) {
                throw new IllegalStateException("Database name/path must be specified");
            }
            if (type.requiresHost() && (host == null || host.isEmpty())) {
                throw new IllegalStateException("Host must be specified for " + type);
            }
        }
    }

    /**
     * Gets the configured JDBC URL.
     *
     * @return the JDBC URL
     */
    @NotNull
    String getJdbcUrl() {
        if (jdbcUrl != null) {
            return jdbcUrl;
        }
        return type.buildUrl(host, port, database);
    }

    /**
     * Gets the database type.
     *
     * @return the database type
     */
    @NotNull
    DatabaseType getType() {
        return type;
    }

    /**
     * Gets the username.
     *
     * @return the username
     */
    String getUsername() {
        return username;
    }

    /**
     * Gets the password.
     *
     * @return the password
     */
    String getPassword() {
        return password;
    }

    /**
     * Gets the pool size.
     *
     * @return the pool size
     */
    int getPoolSize() {
        return poolSize;
    }

    /**
     * Gets the connection timeout.
     *
     * @return the connection timeout in milliseconds
     */
    int getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * Gets the max lifetime.
     *
     * @return the max lifetime in milliseconds
     */
    int getMaxLifetime() {
        return maxLifetime;
    }

    /**
     * Checks if auto-commit is enabled.
     *
     * @return true if auto-commit is enabled
     */
    boolean isAutoCommit() {
        return autoCommit;
    }
}
