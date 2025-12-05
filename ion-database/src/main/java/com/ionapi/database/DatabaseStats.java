package com.ionapi.database;

import org.jetbrains.annotations.NotNull;

/**
 * Statistics and metrics for database operations.
 * <p>
 * Provides insights into connection pool usage, query performance,
 * and overall database health.
 */
public class DatabaseStats {

    private final int totalConnections;
    private final int activeConnections;
    private final int idleConnections;
    private final long totalQueries;
    private final long successfulQueries;
    private final long failedQueries;
    private final long averageQueryTime;
    private final long slowestQueryTime;
    private final long fastestQueryTime;
    private final long totalTransactions;
    private final long committedTransactions;
    private final long rolledBackTransactions;
    private final long uptimeMillis;

    /**
     * Creates a new database statistics object.
     *
     * @param totalConnections the total number of connections in the pool
     * @param activeConnections the number of currently active connections
     * @param idleConnections the number of idle connections
     * @param totalQueries the total number of queries executed
     * @param successfulQueries the number of successful queries
     * @param failedQueries the number of failed queries
     * @param averageQueryTime the average query execution time in milliseconds
     * @param slowestQueryTime the slowest query time in milliseconds
     * @param fastestQueryTime the fastest query time in milliseconds
     * @param totalTransactions the total number of transactions
     * @param committedTransactions the number of committed transactions
     * @param rolledBackTransactions the number of rolled back transactions
     * @param uptimeMillis the database connection uptime in milliseconds
     */
    public DatabaseStats(int totalConnections, int activeConnections, int idleConnections,
                         long totalQueries, long successfulQueries, long failedQueries,
                         long averageQueryTime, long slowestQueryTime, long fastestQueryTime,
                         long totalTransactions, long committedTransactions, long rolledBackTransactions,
                         long uptimeMillis) {
        this.totalConnections = totalConnections;
        this.activeConnections = activeConnections;
        this.idleConnections = idleConnections;
        this.totalQueries = totalQueries;
        this.successfulQueries = successfulQueries;
        this.failedQueries = failedQueries;
        this.averageQueryTime = averageQueryTime;
        this.slowestQueryTime = slowestQueryTime;
        this.fastestQueryTime = fastestQueryTime;
        this.totalTransactions = totalTransactions;
        this.committedTransactions = committedTransactions;
        this.rolledBackTransactions = rolledBackTransactions;
        this.uptimeMillis = uptimeMillis;
    }

    /**
     * Gets the total number of connections in the pool.
     *
     * @return the total connections
     */
    public int getTotalConnections() {
        return totalConnections;
    }

    /**
     * Gets the number of currently active connections.
     *
     * @return the active connections
     */
    public int getActiveConnections() {
        return activeConnections;
    }

    /**
     * Gets the number of idle connections.
     *
     * @return the idle connections
     */
    public int getIdleConnections() {
        return idleConnections;
    }

    /**
     * Gets the connection pool utilization as a percentage.
     *
     * @return the utilization percentage (0.0 to 100.0)
     */
    public double getPoolUtilization() {
        if (totalConnections == 0) return 0.0;
        return (activeConnections / (double) totalConnections) * 100.0;
    }

    /**
     * Gets the total number of queries executed.
     *
     * @return the total queries
     */
    public long getTotalQueries() {
        return totalQueries;
    }

    /**
     * Gets the number of successful queries.
     *
     * @return the successful queries
     */
    public long getSuccessfulQueries() {
        return successfulQueries;
    }

    /**
     * Gets the number of failed queries.
     *
     * @return the failed queries
     */
    public long getFailedQueries() {
        return failedQueries;
    }

    /**
     * Gets the query success rate as a percentage.
     *
     * @return the success rate (0.0 to 100.0)
     */
    public double getSuccessRate() {
        if (totalQueries == 0) return 100.0;
        return (successfulQueries / (double) totalQueries) * 100.0;
    }

    /**
     * Gets the average query execution time in milliseconds.
     *
     * @return the average query time
     */
    public long getAverageQueryTime() {
        return averageQueryTime;
    }

    /**
     * Gets the slowest query execution time in milliseconds.
     *
     * @return the slowest query time
     */
    public long getSlowestQueryTime() {
        return slowestQueryTime;
    }

    /**
     * Gets the fastest query execution time in milliseconds.
     *
     * @return the fastest query time
     */
    public long getFastestQueryTime() {
        return fastestQueryTime;
    }

    /**
     * Gets the total number of transactions.
     *
     * @return the total transactions
     */
    public long getTotalTransactions() {
        return totalTransactions;
    }

    /**
     * Gets the number of committed transactions.
     *
     * @return the committed transactions
     */
    public long getCommittedTransactions() {
        return committedTransactions;
    }

    /**
     * Gets the number of rolled back transactions.
     *
     * @return the rolled back transactions
     */
    public long getRolledBackTransactions() {
        return rolledBackTransactions;
    }

    /**
     * Gets the transaction commit rate as a percentage.
     *
     * @return the commit rate (0.0 to 100.0)
     */
    public double getCommitRate() {
        if (totalTransactions == 0) return 100.0;
        return (committedTransactions / (double) totalTransactions) * 100.0;
    }

    /**
     * Gets the database connection uptime in milliseconds.
     *
     * @return the uptime in milliseconds
     */
    public long getUptimeMillis() {
        return uptimeMillis;
    }

    /**
     * Gets the database connection uptime in seconds.
     *
     * @return the uptime in seconds
     */
    public long getUptimeSeconds() {
        return uptimeMillis / 1000;
    }

    /**
     * Gets the queries per second rate.
     *
     * @return the queries per second
     */
    public double getQueriesPerSecond() {
        long uptimeSeconds = getUptimeSeconds();
        if (uptimeSeconds == 0) return 0.0;
        return totalQueries / (double) uptimeSeconds;
    }

    /**
     * Checks if the connection pool is under high load.
     * High load is defined as more than 80% utilization.
     *
     * @return true if under high load
     */
    public boolean isHighLoad() {
        return getPoolUtilization() > 80.0;
    }

    /**
     * Checks if the database is healthy.
     * A database is considered healthy if:
     * - Success rate is above 95%
     * - Pool utilization is below 90%
     * - Average query time is below 1000ms
     *
     * @return true if healthy
     */
    public boolean isHealthy() {
        return getSuccessRate() > 95.0
            && getPoolUtilization() < 90.0
            && averageQueryTime < 1000;
    }

    @Override
    @NotNull
    public String toString() {
        return String.format(
            "DatabaseStats{" +
            "connections=%d/%d (%.1f%% used), " +
            "queries=%d (%.1f%% success), " +
            "avgQueryTime=%dms, " +
            "transactions=%d (%.1f%% committed), " +
            "uptime=%ds, " +
            "qps=%.2f, " +
            "healthy=%s}",
            activeConnections, totalConnections, getPoolUtilization(),
            totalQueries, getSuccessRate(),
            averageQueryTime,
            totalTransactions, getCommitRate(),
            getUptimeSeconds(),
            getQueriesPerSecond(),
            isHealthy()
        );
    }

    /**
     * Returns a human-readable formatted statistics report.
     *
     * @return the formatted report
     */
    @NotNull
    public String toDetailedString() {
        StringBuilder sb = new StringBuilder();
        sb.append("╔══════════════════════════════════════════╗\n");
        sb.append("║       Database Statistics Report        ║\n");
        sb.append("╠══════════════════════════════════════════╣\n");
        sb.append(String.format("║ Connections: %3d active / %3d total    ║\n",
            activeConnections, totalConnections));
        sb.append(String.format("║ Pool Utilization: %5.1f%%               ║\n",
            getPoolUtilization()));
        sb.append(String.format("║ Idle Connections: %3d                  ║\n",
            idleConnections));
        sb.append("╠══════════════════════════════════════════╣\n");
        sb.append(String.format("║ Total Queries: %,10d              ║\n",
            totalQueries));
        sb.append(String.format("║ Successful: %,10d (%5.1f%%)       ║\n",
            successfulQueries, getSuccessRate()));
        sb.append(String.format("║ Failed: %,10d                     ║\n",
            failedQueries));
        sb.append(String.format("║ Queries/Second: %6.2f                ║\n",
            getQueriesPerSecond()));
        sb.append("╠══════════════════════════════════════════╣\n");
        sb.append(String.format("║ Avg Query Time: %5dms               ║\n",
            averageQueryTime));
        sb.append(String.format("║ Fastest Query: %5dms                ║\n",
            fastestQueryTime));
        sb.append(String.format("║ Slowest Query: %5dms                ║\n",
            slowestQueryTime));
        sb.append("╠══════════════════════════════════════════╣\n");
        sb.append(String.format("║ Transactions: %,10d              ║\n",
            totalTransactions));
        sb.append(String.format("║ Committed: %,10d (%5.1f%%)        ║\n",
            committedTransactions, getCommitRate()));
        sb.append(String.format("║ Rolled Back: %,10d               ║\n",
            rolledBackTransactions));
        sb.append("╠══════════════════════════════════════════╣\n");
        sb.append(String.format("║ Uptime: %,10d seconds           ║\n",
            getUptimeSeconds()));
        sb.append(String.format("║ Status: %-30s   ║\n",
            isHealthy() ? "✓ Healthy" : "⚠ Issues Detected"));
        sb.append("╚══════════════════════════════════════════╝");
        return sb.toString();
    }
}
