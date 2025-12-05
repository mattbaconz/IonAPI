package com.ionapi.database.impl;

import com.ionapi.database.*;
import com.ionapi.database.annotations.Column;
import com.ionapi.database.annotations.PrimaryKey;
import com.ionapi.database.annotations.Table;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * HikariCP-backed implementation of IonDatabase.
 */
public class IonDatabaseImpl implements IonDatabase {

    private static final Logger LOGGER = Logger.getLogger(IonDatabaseImpl.class.getName());

    private final DatabaseType type;
    private final HikariDataSource dataSource;
    private final long startTime;
    private boolean queryLogging = false;

    // Stats tracking
    private final AtomicLong totalQueries = new AtomicLong(0);
    private final AtomicLong successfulQueries = new AtomicLong(0);
    private final AtomicLong failedQueries = new AtomicLong(0);
    private final AtomicLong totalQueryTime = new AtomicLong(0);
    private final AtomicLong slowestQuery = new AtomicLong(0);
    private final AtomicLong fastestQuery = new AtomicLong(Long.MAX_VALUE);
    private final AtomicLong totalTransactions = new AtomicLong(0);
    private final AtomicLong committedTransactions = new AtomicLong(0);
    private final AtomicLong rolledBackTransactions = new AtomicLong(0);

    public IonDatabaseImpl(DatabaseType type, String jdbcUrl, String username, String password,
                           int poolSize, int connectionTimeout, int maxLifetime, boolean autoCommit) {
        this.type = type;
        this.startTime = System.currentTimeMillis();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        if (username != null) config.setUsername(username);
        if (password != null) config.setPassword(password);
        config.setMaximumPoolSize(poolSize);
        config.setConnectionTimeout(connectionTimeout);
        config.setMaxLifetime(maxLifetime);
        config.setAutoCommit(autoCommit);
        config.setPoolName("IonDatabase-Pool");

        this.dataSource = new HikariDataSource(config);
    }

    @Override
    public @NotNull DatabaseType getType() {
        return type;
    }

    @Override
    public boolean isConnected() {
        return dataSource != null && !dataSource.isClosed();
    }

    @Override
    public void connect() throws DatabaseException {
        // HikariCP connects lazily, but we can validate
        try (Connection conn = getConnection()) {
            conn.isValid(5);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to validate connection", e);
        }
    }

    @Override
    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    @Override
    public @NotNull Connection getConnection() throws DatabaseException {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to get connection from pool", e);
        }
    }

    @Override
    public @NotNull ResultSet query(@NotNull String sql, @NotNull Object... params) throws DatabaseException {
        long start = System.currentTimeMillis();
        totalQueries.incrementAndGet();
        
        if (queryLogging) {
            LOGGER.info("[IonDB] Query: " + sql + " | Params: " + Arrays.toString(params));
        }

        try {
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            setParameters(stmt, params);
            ResultSet rs = stmt.executeQuery();
            
            long elapsed = System.currentTimeMillis() - start;
            recordQueryTime(elapsed);
            successfulQueries.incrementAndGet();
            return rs;
        } catch (SQLException e) {
            failedQueries.incrementAndGet();
            throw new DatabaseException("Query failed: " + sql, e);
        }
    }

    @Override
    public int execute(@NotNull String sql, @NotNull Object... params) throws DatabaseException {
        long start = System.currentTimeMillis();
        totalQueries.incrementAndGet();
        
        if (queryLogging) {
            LOGGER.info("[IonDB] Execute: " + sql + " | Params: " + Arrays.toString(params));
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParameters(stmt, params);
            int result = stmt.executeUpdate();
            
            long elapsed = System.currentTimeMillis() - start;
            recordQueryTime(elapsed);
            successfulQueries.incrementAndGet();
            return result;
        } catch (SQLException e) {
            failedQueries.incrementAndGet();
            throw new DatabaseException("Execute failed: " + sql, e);
        }
    }

    @Override
    public @NotNull CompletableFuture<Integer> executeAsync(@NotNull String sql, @NotNull Object... params) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return execute(sql, params);
            } catch (DatabaseException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public @NotNull CompletableFuture<ResultSet> queryAsync(@NotNull String sql, @NotNull Object... params) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return query(sql, params);
            } catch (DatabaseException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public @NotNull <T> QueryBuilder<T> select(@NotNull Class<T> entityClass) {
        return new QueryBuilderImpl<>(this, entityClass);
    }

    @Override
    @Nullable
    public <T> T find(@NotNull Class<T> entityClass, @NotNull Object primaryKey) throws DatabaseException {
        String tableName = getTableName(entityClass);
        Field pkField = getPrimaryKeyField(entityClass);
        String pkColumn = getColumnName(pkField);

        String sql = "SELECT * FROM " + tableName + " WHERE " + pkColumn + " = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, primaryKey);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntity(rs, entityClass);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Find failed for " + entityClass.getSimpleName(), e);
        }
        return null;
    }

    @Override
    public @NotNull <T> CompletableFuture<Optional<T>> findAsync(@NotNull Class<T> entityClass, @NotNull Object primaryKey) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return Optional.ofNullable(find(entityClass, primaryKey));
            } catch (DatabaseException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public @NotNull <T> List<T> findAll(@NotNull Class<T> entityClass) throws DatabaseException {
        String tableName = getTableName(entityClass);
        String sql = "SELECT * FROM " + tableName;
        List<T> results = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                results.add(mapResultSetToEntity(rs, entityClass));
            }
        } catch (SQLException e) {
            throw new DatabaseException("FindAll failed for " + entityClass.getSimpleName(), e);
        }
        return results;
    }

    @Override
    public @NotNull <T> CompletableFuture<List<T>> findAllAsync(@NotNull Class<T> entityClass) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return findAll(entityClass);
            } catch (DatabaseException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public <T> void save(@NotNull T entity) throws DatabaseException {
        Class<?> entityClass = entity.getClass();
        Field pkField = getPrimaryKeyField(entityClass);
        pkField.setAccessible(true);

        try {
            Object pkValue = pkField.get(entity);
            if (pkValue != null && find(entityClass, pkValue) != null) {
                update(entity);
            } else {
                insert(entity);
            }
        } catch (IllegalAccessException e) {
            throw new DatabaseException("Failed to access primary key field", e);
        }
    }

    @Override
    public @NotNull <T> CompletableFuture<Void> saveAsync(@NotNull T entity) {
        return CompletableFuture.runAsync(() -> {
            try {
                save(entity);
            } catch (DatabaseException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public <T> void insert(@NotNull T entity) throws DatabaseException {
        Class<?> entityClass = entity.getClass();
        String tableName = getTableName(entityClass);
        List<Field> columns = getColumnFields(entityClass);

        StringBuilder sql = new StringBuilder("INSERT INTO ").append(tableName).append(" (");
        StringBuilder values = new StringBuilder("VALUES (");
        List<Object> params = new ArrayList<>();

        boolean first = true;
        for (Field field : columns) {
            field.setAccessible(true);
            // Skip auto-generated primary keys
            if (field.isAnnotationPresent(PrimaryKey.class)) {
                PrimaryKey pk = field.getAnnotation(PrimaryKey.class);
                if (pk.autoGenerate()) continue;
            }

            if (!first) {
                sql.append(", ");
                values.append(", ");
            }
            sql.append(getColumnName(field));
            values.append("?");
            
            try {
                params.add(field.get(entity));
            } catch (IllegalAccessException e) {
                throw new DatabaseException("Failed to access field: " + field.getName(), e);
            }
            first = false;
        }

        sql.append(") ").append(values).append(")");
        execute(sql.toString(), params.toArray());
    }

    @Override
    public <T> void update(@NotNull T entity) throws DatabaseException {
        Class<?> entityClass = entity.getClass();
        String tableName = getTableName(entityClass);
        Field pkField = getPrimaryKeyField(entityClass);
        List<Field> columns = getColumnFields(entityClass);

        StringBuilder sql = new StringBuilder("UPDATE ").append(tableName).append(" SET ");
        List<Object> params = new ArrayList<>();

        boolean first = true;
        for (Field field : columns) {
            if (field.equals(pkField)) continue; // Skip primary key in SET clause
            field.setAccessible(true);

            if (!first) sql.append(", ");
            sql.append(getColumnName(field)).append(" = ?");
            
            try {
                params.add(field.get(entity));
            } catch (IllegalAccessException e) {
                throw new DatabaseException("Failed to access field: " + field.getName(), e);
            }
            first = false;
        }

        sql.append(" WHERE ").append(getColumnName(pkField)).append(" = ?");
        pkField.setAccessible(true);
        try {
            params.add(pkField.get(entity));
        } catch (IllegalAccessException e) {
            throw new DatabaseException("Failed to access primary key", e);
        }

        execute(sql.toString(), params.toArray());
    }

    @Override
    public <T> void delete(@NotNull T entity) throws DatabaseException {
        Class<?> entityClass = entity.getClass();
        Field pkField = getPrimaryKeyField(entityClass);
        pkField.setAccessible(true);

        try {
            Object pkValue = pkField.get(entity);
            deleteById(entityClass, pkValue);
        } catch (IllegalAccessException e) {
            throw new DatabaseException("Failed to access primary key", e);
        }
    }

    @Override
    public @NotNull <T> CompletableFuture<Void> deleteAsync(@NotNull T entity) {
        return CompletableFuture.runAsync(() -> {
            try {
                delete(entity);
            } catch (DatabaseException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public <T> boolean deleteById(@NotNull Class<T> entityClass, @NotNull Object primaryKey) throws DatabaseException {
        String tableName = getTableName(entityClass);
        Field pkField = getPrimaryKeyField(entityClass);
        String pkColumn = getColumnName(pkField);

        String sql = "DELETE FROM " + tableName + " WHERE " + pkColumn + " = ?";
        return execute(sql, primaryKey) > 0;
    }

    @Override
    public <T> void createTable(@NotNull Class<T> entityClass) throws DatabaseException {
        String tableName = getTableName(entityClass);
        List<Field> columns = getColumnFields(entityClass);
        Field pkField = getPrimaryKeyField(entityClass);

        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ").append(tableName).append(" (");
        
        boolean first = true;
        for (Field field : columns) {
            if (!first) sql.append(", ");
            sql.append(getColumnName(field)).append(" ").append(getSqlType(field));
            
            // Handle primary key
            if (field.equals(pkField)) {
                sql.append(" PRIMARY KEY");
                PrimaryKey pk = field.getAnnotation(PrimaryKey.class);
                if (pk != null && pk.autoGenerate()) {
                    if (type == DatabaseType.SQLITE) {
                        sql.append(" AUTOINCREMENT");
                    } else if (type == DatabaseType.MYSQL || type == DatabaseType.MARIADB) {
                        sql.append(" AUTO_INCREMENT");
                    }
                }
            } else if (field.isAnnotationPresent(Column.class)) {
                Column col = field.getAnnotation(Column.class);
                if (!col.nullable()) sql.append(" NOT NULL");
                if (col.unique()) sql.append(" UNIQUE");
                if (!col.defaultValue().isEmpty()) {
                    sql.append(" DEFAULT ").append(col.defaultValue());
                }
            }
            first = false;
        }
        sql.append(")");

        execute(sql.toString());
    }

    @Override
    public <T> void dropTable(@NotNull Class<T> entityClass) throws DatabaseException {
        String tableName = getTableName(entityClass);
        execute("DROP TABLE IF EXISTS " + tableName);
    }

    @Override
    public boolean tableExists(@NotNull String tableName) throws DatabaseException {
        try (Connection conn = getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getTables(null, null, tableName, new String[]{"TABLE"})) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to check table existence", e);
        }
    }

    @Override
    public void transaction(@NotNull Consumer<IonDatabase> transaction) throws DatabaseException {
        totalTransactions.incrementAndGet();
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                transaction.accept(this);
                conn.commit();
                committedTransactions.incrementAndGet();
            } catch (Exception e) {
                conn.rollback();
                rolledBackTransactions.incrementAndGet();
                throw new DatabaseException("Transaction failed", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Transaction connection error", e);
        }
    }

    @Override
    public @NotNull CompletableFuture<Void> transactionAsync(@NotNull Consumer<IonDatabase> transaction) {
        return CompletableFuture.runAsync(() -> {
            try {
                transaction(transaction);
            } catch (DatabaseException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public <T> T transactionWithResult(@NotNull Function<IonDatabase, T> transaction) throws DatabaseException {
        totalTransactions.incrementAndGet();
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                T result = transaction.apply(this);
                conn.commit();
                committedTransactions.incrementAndGet();
                return result;
            } catch (Exception e) {
                conn.rollback();
                rolledBackTransactions.incrementAndGet();
                throw new DatabaseException("Transaction failed", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Transaction connection error", e);
        }
    }

    @Override
    public @NotNull Transaction beginTransaction() throws DatabaseException {
        totalTransactions.incrementAndGet();
        return new TransactionImpl(this);
    }

    @Override
    public @NotNull DatabaseStats getStats() {
        long queries = totalQueries.get();
        long avgTime = queries > 0 ? totalQueryTime.get() / queries : 0;
        long fastest = fastestQuery.get() == Long.MAX_VALUE ? 0 : fastestQuery.get();
        
        return new DatabaseStats(
            dataSource.getMaximumPoolSize(),
            dataSource.getHikariPoolMXBean() != null ? dataSource.getHikariPoolMXBean().getActiveConnections() : 0,
            dataSource.getHikariPoolMXBean() != null ? dataSource.getHikariPoolMXBean().getIdleConnections() : 0,
            queries,
            successfulQueries.get(),
            failedQueries.get(),
            avgTime,
            slowestQuery.get(),
            fastest,
            totalTransactions.get(),
            committedTransactions.get(),
            rolledBackTransactions.get(),
            System.currentTimeMillis() - startTime
        );
    }

    @Override
    public void setQueryLogging(boolean enabled) {
        this.queryLogging = enabled;
    }

    @Override
    public boolean isQueryLoggingEnabled() {
        return queryLogging;
    }

    // ==================== Helper Methods ====================

    void incrementCommitted() {
        committedTransactions.incrementAndGet();
    }

    void incrementRolledBack() {
        rolledBackTransactions.incrementAndGet();
    }

    private void recordQueryTime(long elapsed) {
        totalQueryTime.addAndGet(elapsed);
        slowestQuery.updateAndGet(current -> Math.max(current, elapsed));
        fastestQuery.updateAndGet(current -> Math.min(current, elapsed));
    }

    private void setParameters(PreparedStatement stmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            Object param = params[i];
            if (param instanceof UUID) {
                stmt.setString(i + 1, param.toString());
            } else {
                stmt.setObject(i + 1, param);
            }
        }
    }

    String getTableName(Class<?> entityClass) {
        Table table = entityClass.getAnnotation(Table.class);
        if (table == null || table.value().isEmpty()) {
            return entityClass.getSimpleName().toLowerCase();
        }
        return table.value();
    }

    Field getPrimaryKeyField(Class<?> entityClass) throws DatabaseException {
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(PrimaryKey.class)) {
                return field;
            }
        }
        throw new DatabaseException("No @PrimaryKey field found in " + entityClass.getSimpleName());
    }

    String getColumnName(Field field) {
        if (field.isAnnotationPresent(PrimaryKey.class)) {
            PrimaryKey pk = field.getAnnotation(PrimaryKey.class);
            if (!pk.columnName().isEmpty()) return pk.columnName();
        }
        if (field.isAnnotationPresent(Column.class)) {
            Column col = field.getAnnotation(Column.class);
            if (!col.name().isEmpty()) return col.name();
        }
        return field.getName();
    }

    List<Field> getColumnFields(Class<?> entityClass) {
        List<Field> fields = new ArrayList<>();
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(PrimaryKey.class) || field.isAnnotationPresent(Column.class)) {
                fields.add(field);
            }
        }
        // If no annotations, include all non-transient fields
        if (fields.isEmpty()) {
            for (Field field : entityClass.getDeclaredFields()) {
                if (!java.lang.reflect.Modifier.isTransient(field.getModifiers())) {
                    fields.add(field);
                }
            }
        }
        return fields;
    }

    <T> T mapResultSetToEntity(ResultSet rs, Class<T> entityClass) throws DatabaseException {
        try {
            T instance = entityClass.getDeclaredConstructor().newInstance();
            List<Field> fields = getColumnFields(entityClass);

            for (Field field : fields) {
                field.setAccessible(true);
                String columnName = getColumnName(field);
                Object value = rs.getObject(columnName);
                
                // Handle UUID conversion
                if (field.getType() == UUID.class && value instanceof String) {
                    value = UUID.fromString((String) value);
                }
                
                field.set(instance, value);
            }
            return instance;
        } catch (Exception e) {
            throw new DatabaseException("Failed to map ResultSet to " + entityClass.getSimpleName(), e);
        }
    }

    private String getSqlType(Field field) {
        Class<?> type = field.getType();
        
        // Check for custom column definition
        if (field.isAnnotationPresent(Column.class)) {
            Column col = field.getAnnotation(Column.class);
            if (!col.columnDefinition().isEmpty()) {
                return col.columnDefinition();
            }
        }

        int length = 255;
        if (field.isAnnotationPresent(Column.class)) {
            length = field.getAnnotation(Column.class).length();
        }

        if (type == int.class || type == Integer.class) return "INTEGER";
        if (type == long.class || type == Long.class) return "BIGINT";
        if (type == double.class || type == Double.class) return "DOUBLE";
        if (type == float.class || type == Float.class) return "FLOAT";
        if (type == boolean.class || type == Boolean.class) return "BOOLEAN";
        if (type == String.class) return "VARCHAR(" + length + ")";
        if (type == UUID.class) return "VARCHAR(36)";
        if (type == byte[].class) return "BLOB";
        if (type == java.util.Date.class || type == java.sql.Timestamp.class) return "TIMESTAMP";
        
        return "TEXT";
    }
}
