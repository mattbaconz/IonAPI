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
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of QueryBuilder for type-safe queries.
 */
public class QueryBuilderImpl<T> implements QueryBuilder<T> {

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
        this.tableName = database.getTableName(entityClass);
    }

    @Override
    public @NotNull QueryBuilder<T> where(@NotNull String column, @NotNull String operator, @NotNull Object value) {
        conditions.add(column + " " + operator + " ?");
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
        conditions.add("AND " + column + " " + operator + " ?");
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
        conditions.add("OR " + column + " " + operator + " ?");
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
        this.orderByClause = " ORDER BY " + column + " " + direction;
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
