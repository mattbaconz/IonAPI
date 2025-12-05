package com.ionapi.tasks;

import com.ionapi.api.IonPlugin;
import com.ionapi.api.scheduler.IonTask;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Fluent API for chaining async and sync tasks together.
 * <p>
 * TaskChain provides a clean, readable way to build complex workflows that mix
 * asynchronous operations (database queries, API calls) with synchronous Minecraft
 * operations (entity manipulation, world changes).
 * <p>
 * Example usage:
 * <pre>{@code
 * TaskChain.create(plugin)
 *     .async(() -> fetchFromDatabase(uuid))      // Off thread
 *     .syncAt(player, data -> applyData(data))   // Player's region
 *     .delay(5, TimeUnit.SECONDS)                // Wait
 *     .sync(() -> broadcastMessage())            // Global scheduler
 *     .exceptionally(ex -> handleError(ex))      // Error handling
 *     .execute();
 * }</pre>
 * <p>
 * All tasks in the chain are executed in order. Each task receives the result
 * of the previous task (if any). The chain supports proper error handling and
 * can be cancelled at any point.
 */
public interface TaskChain<T> {

    /**
     * Creates a new task chain with no initial value.
     *
     * @param plugin the plugin instance
     * @return a new task chain
     */
    @NotNull
    static TaskChain<Void> create(@NotNull IonPlugin plugin) {
        return new TaskChainBuilder<>(plugin);
    }

    /**
     * Creates a new task chain with an initial value.
     *
     * @param plugin the plugin instance
     * @param initialValue the initial value
     * @param <T> the value type
     * @return a new task chain
     */
    @NotNull
    static <T> TaskChain<T> create(@NotNull IonPlugin plugin, @NotNull T initialValue) {
        return new TaskChainBuilder<>(plugin, initialValue);
    }

    /**
     * Adds a synchronous task to the chain (runs on main/global thread).
     *
     * @param task the task to run
     * @return this chain
     */
    @NotNull
    TaskChain<T> sync(@NotNull Runnable task);

    /**
     * Adds a synchronous task that transforms the current value.
     *
     * @param task the transformation function
     * @param <R> the result type
     * @return a new chain with the transformed value
     */
    @NotNull
    <R> TaskChain<R> sync(@NotNull Function<T, R> task);

    /**
     * Adds a synchronous task that consumes the current value.
     *
     * @param task the consumer task
     * @return this chain
     */
    @NotNull
    TaskChain<T> syncConsume(@NotNull Consumer<T> task);

    /**
     * Adds a synchronous task that produces a new value.
     *
     * @param task the supplier task
     * @param <R> the result type
     * @return a new chain with the supplied value
     */
    @NotNull
    <R> TaskChain<R> syncSupply(@NotNull Supplier<R> task);

    /**
     * Adds a synchronous task that runs on an entity's region thread (Folia-aware).
     *
     * @param entity the entity whose region thread to use
     * @param task the task to run
     * @return this chain
     */
    @NotNull
    TaskChain<T> syncAt(@NotNull Entity entity, @NotNull Runnable task);

    /**
     * Adds a synchronous task that runs on an entity's region thread and transforms the value.
     *
     * @param entity the entity whose region thread to use
     * @param task the transformation function
     * @param <R> the result type
     * @return a new chain with the transformed value
     */
    @NotNull
    <R> TaskChain<R> syncAt(@NotNull Entity entity, @NotNull Function<T, R> task);

    /**
     * Adds a synchronous task that runs on an entity's region thread and consumes the value.
     *
     * @param entity the entity whose region thread to use
     * @param task the consumer task
     * @return this chain
     */
    @NotNull
    TaskChain<T> syncAtConsume(@NotNull Entity entity, @NotNull Consumer<T> task);

    /**
     * Adds a synchronous task that runs on a location's region thread (Folia-aware).
     *
     * @param location the location whose region thread to use
     * @param task the task to run
     * @return this chain
     */
    @NotNull
    TaskChain<T> syncAt(@NotNull Location location, @NotNull Runnable task);

    /**
     * Adds a synchronous task that runs on a location's region thread and transforms the value.
     *
     * @param location the location whose region thread to use
     * @param task the transformation function
     * @param <R> the result type
     * @return a new chain with the transformed value
     */
    @NotNull
    <R> TaskChain<R> syncAt(@NotNull Location location, @NotNull Function<T, R> task);

    /**
     * Adds an asynchronous task to the chain (runs off main thread).
     *
     * @param task the task to run
     * @return this chain
     */
    @NotNull
    TaskChain<T> async(@NotNull Runnable task);

    /**
     * Adds an asynchronous task that transforms the current value.
     *
     * @param task the transformation function
     * @param <R> the result type
     * @return a new chain with the transformed value
     */
    @NotNull
    <R> TaskChain<R> async(@NotNull Function<T, R> task);

    /**
     * Adds an asynchronous task that consumes the current value.
     *
     * @param task the consumer task
     * @return this chain
     */
    @NotNull
    TaskChain<T> asyncConsume(@NotNull Consumer<T> task);

    /**
     * Adds an asynchronous task that produces a new value.
     *
     * @param task the supplier task
     * @param <R> the result type
     * @return a new chain with the supplied value
     */
    @NotNull
    <R> TaskChain<R> asyncSupply(@NotNull Supplier<R> task);

    /**
     * Adds a delay to the chain.
     *
     * @param delay the delay amount
     * @param unit the time unit
     * @return this chain
     */
    @NotNull
    TaskChain<T> delay(long delay, @NotNull TimeUnit unit);

    /**
     * Adds a conditional task that only runs if the predicate is true.
     *
     * @param condition the condition to check
     * @param task the task to run if true
     * @return this chain
     */
    @NotNull
    TaskChain<T> syncIf(@NotNull Supplier<Boolean> condition, @NotNull Runnable task);

    /**
     * Adds a conditional transformation that only runs if the predicate is true.
     *
     * @param condition the condition to check
     * @param task the transformation to apply if true
     * @param <R> the result type
     * @return a new chain
     */
    @NotNull
    <R> TaskChain<R> syncIf(@NotNull Supplier<Boolean> condition, @NotNull Function<T, R> task);

    /**
     * Adds an error handler to the chain.
     * If any previous task throws an exception, this handler will be called.
     *
     * @param handler the error handler
     * @return this chain
     */
    @NotNull
    TaskChain<T> exceptionally(@NotNull Consumer<Throwable> handler);

    /**
     * Adds an error recovery function.
     * If any previous task throws an exception, this function can provide a fallback value.
     *
     * @param recovery the recovery function
     * @return this chain
     */
    @NotNull
    TaskChain<T> recover(@NotNull Function<Throwable, T> recovery);

    /**
     * Adds a finally block that always runs, regardless of success or failure.
     *
     * @param task the task to run
     * @return this chain
     */
    @NotNull
    TaskChain<T> finallyDo(@NotNull Runnable task);

    /**
     * Adds a callback to run when the chain completes successfully.
     *
     * @param callback the success callback
     * @return this chain
     */
    @NotNull
    TaskChain<T> thenAccept(@NotNull Consumer<T> callback);

    /**
     * Adds a callback to run when the chain completes (success or failure).
     *
     * @param callback the completion callback
     * @return this chain
     */
    @NotNull
    TaskChain<T> whenComplete(@NotNull Runnable callback);

    /**
     * Executes the task chain.
     * Tasks will be executed in the order they were added.
     *
     * @return a completable future that completes when the chain finishes
     */
    @NotNull
    CompletableFuture<T> execute();

    /**
     * Executes the task chain and blocks until completion.
     * WARNING: This will block the current thread. Only use when appropriate.
     *
     * @return the final result value
     * @throws Exception if any task in the chain fails
     */
    T executeAndWait() throws Exception;

    /**
     * Cancels the task chain if it's still running.
     *
     * @return true if the chain was cancelled, false if already complete
     */
    boolean cancel();

    /**
     * Checks if the chain has been cancelled.
     *
     * @return true if cancelled
     */
    boolean isCancelled();

    /**
     * Checks if the chain has completed (successfully or with error).
     *
     * @return true if completed
     */
    boolean isDone();

    /**
     * Gets the current value in the chain (may be null if not yet computed).
     *
     * @return the current value
     */
    T getCurrentValue();

    /**
     * Gets the underlying CompletableFuture for advanced usage.
     *
     * @return the completable future
     */
    @NotNull
    CompletableFuture<T> toCompletableFuture();
}
