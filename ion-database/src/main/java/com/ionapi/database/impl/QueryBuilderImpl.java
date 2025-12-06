package com.ionapi.database.impl;

import com.ionapi.database.DatabaseException;
import com.ionapi.database.QueryBuilder;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

/**
 * Implementation of QueryBuilder for type-safe queries.
 */
public class QueryBuilderImpl<T> implements QueryBuilder<T> {

    // Allowed SQL operators to prevent injection
    private static final Set<String> ALLOWED_OPERATORS = Set.of(
        "=", "!=", "<>", ">", "<", ">=", "<=", "LIKE", "NOT LIKE", "IN", "NOT IN", "IS", "IS NOT"
    );
    
    // Allowed ORDER BY directions
    private static final Set<String> ALLOWED_DIRECTIONS = Set.of("ASC", "DESC");
    
    // Pattern for valid SQL identifiers (table/column names)
    private static final Pattern VALID_IDENTIFIER = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

    private final IonDatabaseImpl database;
    private final Class<T> entityClass;
    private final String tableName;
    private final List<String> conditions = new ArrayList<>();
    private final List<Object> parameters = new ArrayList<>();
    private String orderByClause = "";
    private int limitValue = -1;
    private int offsetValue = -1;

    public QueryBuilderImpl(IonDatabaseImpl database, Class<T> entityClass) {
        this.database = database;
        this.entityClass = entityClass;
        this.tableName = sanitizeIdentifier(database.getTableName(entityClass));
    }
    
    /**
     * Sanitizes a SQL identifier (table/column name) to prevent SQL injection.
     * Only allows alphanumeric characters and underscores, starting with a letter or underscore.
     *
     * @param identifier the identifier to sanitize
     * @return the sanitized identifier
     * @throws IllegalArgumentException if the identifier is invalid
     */
    private static String sanitizeIdentifier(@NotNull String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            throw new IllegalArgumentException("SQL identifier cannot be null or empty");
        }
        
        // Remove any backticks or quotes that might be used for escaping
        String cleaned = identifier.replace("`", "").replace("\"", "").replace("'", "").trim();
        
        if (!VALID_IDENTIFIER.matcher(cleaned).matches()) {
            throw new IllegalArgumentException("Invalid SQL identifier: " + identifier + 
                ". Only alphanumeric characters and underscores are allowed, starting with a letter or underscore.");
        }
        
        return cleaned;
    }
    
    /**
     * Sanitizes a SQL operator to prevent SQL injection.
     *
     * @param operator the operator to sanitize
     * @return the sanitized operator
     * @throws IllegalArgumentException if the operator is not allowed
     */
    private static String sanitizeOperator(@NotNull String operator) {
        String upper = operator.toUpperCase().trim();
        if (!ALLOWED_OPERATORS.contains(upper)) {
            throw new IllegalArgumentException("Invalid SQL operator: " + operator + 
                ". Allowed operators: " + ALLOWED_OPERATORS);
        }
        return upper;
    }
    
    /**
     * Sanitizes an ORDER BY direction to prevent SQL injection.
     *
     * @param direction the direction to sanitize
     * @return the sanitized direction
     * @throws IllegalArgumentException if the direction is not allowed
     */
    private static String sanitizeDirection(@NotNull String direction) {
        String upper = direction.toUpperCase().trim();
        if (!ALLOWED_DIRECTIONS.contains(upper)) {
            throw new IllegalArgumentException("Invalid ORDER BY direction: " + direction + 
                ". Allowed directions: ASC, DESC");
        }
        return upper;
    }

    @Override
    public @NotNull QueryBuilder<T> where(@NotNull String column, @NotNull String operator, @NotNull Object value) {
        String safeColumn = sanitizeIdentifier(column);
        String safeOperator = sanitizeOperator(operator);
        conditions.add(safeColumn + " " + safeOperator + " ?");
        parameters.add(value);
        return this;
    }

    @Override
    public @NotNull QueryBuilder<T> where(@NotNull String column, @NotNull Object value) {
        return where(column, "=", value);
    }

    @Override
    public @NotNull QueryBuilder<T> and(@NotNull String column, @NotNull String operator, @NotNull Object value) {
        if (conditions.isEmpty()) {
            return where(column, operator, value);
        }
        String safeColumn = sanitizeIdentifier(column);
        String safeOperator = sanitizeOperator(operator);
        conditions.add("AND " + safeColumn + " " + safeOperator + " ?");
        parameters.add(value);
        return this;
    }

    @Override
    public @NotNull QueryBuilder<T> and(@NotNull String column, @NotNull Object value) {
        return and(column, "=", value);
    }

    @Override
    public @NotNull QueryBuilder<T> or(@NotNull String column, @NotNull String operator, @NotNull Object value) {
        if (conditions.isEmpty()) {
            return where(column, operator, value);
        }
        String safeColumn = sanitizeIdentifier(column);
        String safeOperator = sanitizeOperator(operator);
        conditions.add("OR " + safeColumn + " " + safeOperator + " ?");
        parameters.add(value);
        return this;
    }

    @Override
    public @NotNull QueryBuilder<T> or(@NotNull String column, @NotNull Object value) {
        return or(column, "=", value);
    }

    @Override
    public @NotNull QueryBuilder<T> orderBy(@NotNull String column) {
        return orderBy(column, "ASC");
    }

    @Override
    public @NotNull QueryBuilder<T> orderBy(@NotNull String column, @NotNull String direction) {
        String safeColumn = sanitizeIdentifier(column);
        String safeDirection = sanitizeDirection(direction);
        this.orderByClause = " ORDER BY " + safeColumn + " " + safeDirection;
        return this;
    }

    @Override
    public @NotNull QueryBuilder<T> limit(int limit) {
        this.limitValue = limit;
        return this;
    }

    @Override
    public @NotNull QueryBuilder<T> offset(int offset) {
        this.offsetValue = offset;
        return this;
    }

    @Override
    public @NotNull List<T> execute() throws DatabaseException {
        List<T> results = new ArrayList<>();
        String sql = toSql();

        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParameters(stmt);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(database.mapResultSetToEntity(rs, entityClass));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Query execution failed: " + sql, e);
        }
        return results;
    }

    @Override
    public @NotNull CompletableFuture<List<T>> executeAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return execute();
            } catch (DatabaseException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public T first() throws DatabaseException {
        limit(1);
        List<T> results = execute();
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public @NotNull CompletableFuture<T> firstAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return first();
            } catch (DatabaseException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public int count() throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM " + tableName + buildWhereClause();
        
        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParameters(stmt);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Count query failed", e);
        }
        return 0;
    }

    @Override
    public @NotNull CompletableFuture<Integer> countAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return count();
            } catch (DatabaseException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public boolean exists() throws DatabaseException {
        return count() > 0;
    }

    @Override
    public int delete() throws DatabaseException {
        String sql = "DELETE FROM " + tableName + buildWhereClause();
        return database.execute(sql, parameters.toArray());
    }

    @Override
    public @NotNull String toSql() {
        StringBuilder sql = new StringBuilder("SELECT * FROM ").append(tableName);
        sql.append(buildWhereClause());
        sql.append(orderByClause);
        
        if (limitValue > 0) {
            sql.append(" LIMIT ").append(limitValue);
        }
        if (offsetValue > 0) {
            sql.append(" OFFSET ").append(offsetValue);
        }
        return sql.toString();
    }

    private String buildWhereClause() {
        if (conditions.isEmpty()) return "";
        return " WHERE " + String.join(" ", conditions);
    }

    private void setParameters(PreparedStatement stmt) throws SQLException {
        for (int i = 0; i < parameters.size(); i++) {
            Object param = parameters.get(i);
            if (param instanceof java.util.UUID) {
                stmt.setString(i + 1, param.toString());
            } else {
                stmt.setObject(i + 1, param);
            }
        }
    }
}
