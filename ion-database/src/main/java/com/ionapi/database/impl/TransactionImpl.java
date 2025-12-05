package com.ionapi.database.impl;

import com.ionapi.database.DatabaseException;
import com.ionapi.database.Transaction;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of Transaction for manual transaction control.
 */
public class TransactionImpl implements Transaction {

    private final IonDatabaseImpl database;
    private final Connection connection;
    private final Map<String, Savepoint> savepoints = new HashMap<>();
    private boolean active = true;
    private boolean committed = false;
    private boolean rolledBack = false;

    public TransactionImpl(IonDatabaseImpl database) throws DatabaseException {
        this.database = database;
        try {
            this.connection = database.getConnection();
            this.connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to begin transaction", e);
        }
    }

    @Override
    public void commit() throws DatabaseException {
        if (!active) {
            throw new DatabaseException("Transaction is no longer active");
        }
        try {
            connection.commit();
            committed = true;
            active = false;
            database.incrementCommitted();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to commit transaction", e);
        }
    }

    @Override
    public void rollback() throws DatabaseException {
        if (!active) {
            throw new DatabaseException("Transaction is no longer active");
        }
        try {
            connection.rollback();
            rolledBack = true;
            active = false;
            database.incrementRolledBack();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to rollback transaction", e);
        }
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public boolean isCommitted() {
        return committed;
    }

    @Override
    public boolean isRolledBack() {
        return rolledBack;
    }

    @Override
    public void setSavepoint(@NotNull String name) throws DatabaseException {
        if (!active) {
            throw new DatabaseException("Transaction is no longer active");
        }
        try {
            Savepoint savepoint = connection.setSavepoint(name);
            savepoints.put(name, savepoint);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to set savepoint: " + name, e);
        }
    }

    @Override
    public void rollbackToSavepoint(@NotNull String name) throws DatabaseException {
        if (!active) {
            throw new DatabaseException("Transaction is no longer active");
        }
        Savepoint savepoint = savepoints.get(name);
        if (savepoint == null) {
            throw new DatabaseException("Savepoint not found: " + name);
        }
        try {
            connection.rollback(savepoint);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to rollback to savepoint: " + name, e);
        }
    }

    @Override
    public void releaseSavepoint(@NotNull String name) throws DatabaseException {
        if (!active) {
            throw new DatabaseException("Transaction is no longer active");
        }
        Savepoint savepoint = savepoints.remove(name);
        if (savepoint == null) {
            throw new DatabaseException("Savepoint not found: " + name);
        }
        try {
            connection.releaseSavepoint(savepoint);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to release savepoint: " + name, e);
        }
    }

    @Override
    public @NotNull Connection getConnection() {
        return connection;
    }

    @Override
    public void close() {
        if (active) {
            try {
                rollback();
            } catch (DatabaseException ignored) {
            }
        }
        try {
            connection.setAutoCommit(true);
            connection.close();
        } catch (SQLException ignored) {
        }
    }
}
