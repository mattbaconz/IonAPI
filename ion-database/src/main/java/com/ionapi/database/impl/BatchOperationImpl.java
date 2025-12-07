package com.ionapi.database.impl;

import com.ionapi.database.BatchOperation;
import com.ionapi.database.DatabaseException;
import com.ionapi.database.cache.ReflectionCache;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of batch operations for efficient bulk database operations.
 *
 * @param <T> the entity type
 * @since 1.2.0
 */
public class BatchOperationImpl<T> implements BatchOperation<T> {

    private final IonDatabaseImpl database;
    private final Class<T> entityClass;
    private final List<T> inserts = new ArrayList<>();
    private final List<T> updates = new ArrayList<>();
    private final List<T> deletes = new ArrayList<>();
    private int batchSize = 1000;

    public BatchOperationImpl(IonDatabaseImpl database, Class<T> entityClass) {
        this.database = database;
        this.entityClass = entityClass;
    }

    @Override
    public @NotNull BatchOperation<T> insert(@NotNull T entity) {
        inserts.add(entity);
        return this;
    }

    @Override
    public @NotNull BatchOperation<T> insertAll(@NotNull List<T> entities) {
        inserts.addAll(entities);
        return this;
    }

    @Override
    public @NotNull BatchOperation<T> update(@NotNull T entity) {
        updates.add(entity);
        return this;
    }

    @Override
    public @NotNull BatchOperation<T> updateAll(@NotNull List<T> entities) {
        updates.addAll(entities);
        return this;
    }

    @Override
    public @NotNull BatchOperation<T> delete(@NotNull T entity) {
        deletes.add(entity);
        return this;
    }

    @Override
    public @NotNull BatchOperation<T> deleteAll(@NotNull List<T> entities) {
        deletes.addAll(entities);
        return this;
    }

    @Override
    public @NotNull BatchOperation<T> batchSize(int size) {
        this.batchSize = size;
        return this;
    }

    @Override
    public @NotNull BatchResult execute() throws DatabaseException {
        long startTime = System.currentTimeMillis();
        int insertedCount = 0;
        int updatedCount = 0;
        int deletedCount = 0;

        ReflectionCache.EntityMetadata metadata = ReflectionCache.get(entityClass);

        try (Connection conn = database.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // Execute inserts
                if (!inserts.isEmpty()) {
                    insertedCount = executeBatchInserts(conn, metadata);
                }

                // Execute updates
                if (!updates.isEmpty()) {
                    updatedCount = executeBatchUpdates(conn, metadata);
                }

                // Execute deletes
                if (!deletes.isEmpty()) {
                    deletedCount = executeBatchDeletes(conn, metadata);
                }

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw new DatabaseException("Batch operation failed", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Batch connection error", e);
        }

        long executionTime = System.currentTimeMillis() - startTime;
        return new BatchResult(insertedCount, updatedCount, deletedCount, executionTime);
    }

    @Override
    public @NotNull CompletableFuture<BatchResult> executeAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return execute();
            } catch (DatabaseException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private int executeBatchInserts(Connection conn, ReflectionCache.EntityMetadata metadata) throws SQLException {
        StringBuilder sql = new StringBuilder("INSERT INTO ").append(metadata.tableName()).append(" (");
        StringBuilder values = new StringBuilder("VALUES (");

        List<ReflectionCache.FieldMetadata> insertFields = new ArrayList<>();
        boolean first = true;
        for (ReflectionCache.FieldMetadata fm : metadata.fields()) {
            if (fm.isPrimaryKey() && fm.isAutoGenerate()) continue;
            if (!first) {
                sql.append(", ");
                values.append(", ");
            }
            sql.append(fm.columnName());
            values.append("?");
            insertFields.add(fm);
            first = false;
        }
        sql.append(") ").append(values).append(")");

        int count = 0;
        try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int batchCount = 0;
            for (T entity : inserts) {
                setParameters(stmt, entity, insertFields);
                stmt.addBatch();
                batchCount++;

                if (batchCount >= batchSize) {
                    int[] results = stmt.executeBatch();
                    count += sumResults(results);
                    batchCount = 0;
                }
            }
            if (batchCount > 0) {
                int[] results = stmt.executeBatch();
                count += sumResults(results);
            }
        }
        return count;
    }

    private int executeBatchUpdates(Connection conn, ReflectionCache.EntityMetadata metadata) throws SQLException {
        StringBuilder sql = new StringBuilder("UPDATE ").append(metadata.tableName()).append(" SET ");

        List<ReflectionCache.FieldMetadata> updateFields = new ArrayList<>();
        ReflectionCache.FieldMetadata pkField = null;

        boolean first = true;
        for (ReflectionCache.FieldMetadata fm : metadata.fields()) {
            if (fm.isPrimaryKey()) {
                pkField = fm;
                continue;
            }
            if (!first) sql.append(", ");
            sql.append(fm.columnName()).append(" = ?");
            updateFields.add(fm);
            first = false;
        }

        if (pkField == null) {
            throw new SQLException("No primary key found for update");
        }

        sql.append(" WHERE ").append(pkField.columnName()).append(" = ?");

        int count = 0;
        try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int batchCount = 0;
            for (T entity : updates) {
                int paramIndex = 1;
                for (ReflectionCache.FieldMetadata fm : updateFields) {
                    setParameter(stmt, paramIndex++, entity, fm);
                }
                setParameter(stmt, paramIndex, entity, pkField);
                stmt.addBatch();
                batchCount++;

                if (batchCount >= batchSize) {
                    int[] results = stmt.executeBatch();
                    count += sumResults(results);
                    batchCount = 0;
                }
            }
            if (batchCount > 0) {
                int[] results = stmt.executeBatch();
                count += sumResults(results);
            }
        }
        return count;
    }

    private int executeBatchDeletes(Connection conn, ReflectionCache.EntityMetadata metadata) throws SQLException {
        ReflectionCache.FieldMetadata pkField = null;
        for (ReflectionCache.FieldMetadata fm : metadata.fields()) {
            if (fm.isPrimaryKey()) {
                pkField = fm;
                break;
            }
        }

        if (pkField == null) {
            throw new SQLException("No primary key found for delete");
        }

        String sql = "DELETE FROM " + metadata.tableName() + " WHERE " + pkField.columnName() + " = ?";

        int count = 0;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int batchCount = 0;
            for (T entity : deletes) {
                setParameter(stmt, 1, entity, pkField);
                stmt.addBatch();
                batchCount++;

                if (batchCount >= batchSize) {
                    int[] results = stmt.executeBatch();
                    count += sumResults(results);
                    batchCount = 0;
                }
            }
            if (batchCount > 0) {
                int[] results = stmt.executeBatch();
                count += sumResults(results);
            }
        }
        return count;
    }

    private void setParameters(PreparedStatement stmt, T entity, List<ReflectionCache.FieldMetadata> fields) throws SQLException {
        int index = 1;
        for (ReflectionCache.FieldMetadata fm : fields) {
            setParameter(stmt, index++, entity, fm);
        }
    }

    private void setParameter(PreparedStatement stmt, int index, T entity, ReflectionCache.FieldMetadata fm) throws SQLException {
        try {
            Object value = fm.field().get(entity);
            if (value instanceof UUID) {
                stmt.setString(index, value.toString());
            } else {
                stmt.setObject(index, value);
            }
        } catch (IllegalAccessException e) {
            throw new SQLException("Failed to access field: " + fm.field().getName(), e);
        }
    }

    private int sumResults(int[] results) {
        int sum = 0;
        for (int r : results) {
            if (r > 0) sum += r;
            else if (r == PreparedStatement.SUCCESS_NO_INFO) sum++;
        }
        return sum;
    }
}
