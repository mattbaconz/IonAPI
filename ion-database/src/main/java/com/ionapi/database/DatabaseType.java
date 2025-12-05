package com.ionapi.database;

/**
 * Supported database types for IonDatabase.
 */
public enum DatabaseType {

    /**
     * MySQL database.
     * JDBC URL format: jdbc:mysql://host:port/database
     */
    MYSQL("com.mysql.cj.jdbc.Driver", "jdbc:mysql://%s:%d/%s", 3306),

    /**
     * PostgreSQL database.
     * JDBC URL format: jdbc:postgresql://host:port/database
     */
    POSTGRESQL("org.postgresql.Driver", "jdbc:postgresql://%s:%d/%s", 5432),

    /**
     * SQLite database (file-based).
     * JDBC URL format: jdbc:sqlite:path/to/file.db
     */
    SQLITE("org.sqlite.JDBC", "jdbc:sqlite:%s", 0),

    /**
     * H2 database (embedded or server mode).
     * JDBC URL format: jdbc:h2:./path/to/file
     */
    H2("org.h2.Driver", "jdbc:h2:./%s", 0),

    /**
     * MariaDB database (compatible with MySQL).
     * JDBC URL format: jdbc:mariadb://host:port/database
     */
    MARIADB("org.mariadb.jdbc.Driver", "jdbc:mariadb://%s:%d/%s", 3306);

    private final String driverClass;
    private final String urlFormat;
    private final int defaultPort;

    DatabaseType(String driverClass, String urlFormat, int defaultPort) {
        this.driverClass = driverClass;
        this.urlFormat = urlFormat;
        this.defaultPort = defaultPort;
    }

    /**
     * Gets the JDBC driver class name.
     *
     * @return the driver class name
     */
    public String getDriverClass() {
        return driverClass;
    }

    /**
     * Gets the JDBC URL format string.
     *
     * @return the URL format
     */
    public String getUrlFormat() {
        return urlFormat;
    }

    /**
     * Gets the default port for this database type.
     *
     * @return the default port, or 0 if not applicable
     */
    public int getDefaultPort() {
        return defaultPort;
    }

    /**
     * Checks if this database type requires a host.
     *
     * @return true if host is required
     */
    public boolean requiresHost() {
        return this == MYSQL || this == POSTGRESQL || this == MARIADB;
    }

    /**
     * Checks if this database type is file-based.
     *
     * @return true if file-based
     */
    public boolean isFileBased() {
        return this == SQLITE || this == H2;
    }

    /**
     * Builds a JDBC URL for this database type.
     *
     * @param host the host (ignored for file-based databases)
     * @param port the port (0 to use default)
     * @param database the database name or file path
     * @return the JDBC URL
     */
    public String buildUrl(String host, int port, String database) {
        if (isFileBased()) {
            return String.format(urlFormat, database);
        } else {
            int actualPort = port > 0 ? port : defaultPort;
            return String.format(urlFormat, host, actualPort, database);
        }
    }

    /**
     * Builds a JDBC URL with default port.
     *
     * @param host the host
     * @param database the database name
     * @return the JDBC URL
     */
    public String buildUrl(String host, String database) {
        return buildUrl(host, defaultPort, database);
    }
}
