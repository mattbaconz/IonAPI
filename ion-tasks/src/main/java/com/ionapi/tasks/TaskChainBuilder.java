package com.ionapi.tasks;

import com.ionapi.api.IonPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Implementation of TaskChain for building and executing task chains.
 */
public class TaskChainBuilder<T> implements TaskChain<T> {

    private final IonPlugin plugin;
    private final List<ChainLink<?, ?>> links;
    private T currentValue;
    private Consumer<Throwable> exceptionHandler;
    private Function<Throwable, T> recoveryFunction;
    private Runnable finallyBlock;
    private Consumer<T> successCallback;
    private Runnable completeCallback;
    private CompletableFuture<T> future;
    private volatile boolean cancelled;

    /**
     * Creates a new task chain builder.
     *
     * @param plugin the plugin instance
     */
    public TaskChainBuilder(@NotNull IonPlugin plugin) {
        this(plugin, null);
    }

    /**
     * Creates a new task chain builder with an initial value.
     *
     * @param plugin the plugin instance
     * @param initialValue the initial value
     */
    public TaskChainBuilder(@NotNull IonPlugin plugin, T initialValue) {
        this.plugin = plugin;
        this.currentValue = initialValue;
        this.links = new ArrayList<>();
        this.cancelled = false;
    }

    @Override
    public @NotNull TaskChain<T> sync(@NotNull Runnable task) {
        links.add(new ChainLink<>(LinkType.SYNC, v -> {
            task.run();
            return v;
        }));
        return this;
    }

    @Override
    public @NotNull <R> TaskChain<R> sync(@NotNull Function<T, R> task) {
        @SuppressWarnings("unchecked")
        TaskChainBuilder<R> newChain = (TaskChainBuilder<R>) this;
        newChain.links.add(new ChainLink<>(LinkType.SYNC, task));
        return newChain;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull TaskChain<T> syncConsume(@NotNull Consumer<T> task) {
        links.add(new ChainLink<>(LinkType.SYNC, v -> {
            task.accept((T) v);
            return v;
        }));
        return this;
    }

    @Override
    public @NotNull <R> TaskChain<R> syncSupply(@NotNull Supplier<R> task) {
        @SuppressWarnings("unchecked")
        TaskChainBuilder<R> newChain = (TaskChainBuilder<R>) this;
        newChain.links.add(new ChainLink<>(LinkType.SYNC, v -> task.get()));
        return newChain;
    }

    @Override
    public @NotNull TaskChain<T> syncAt(@NotNull Entity entity, @NotNull Runnable task) {
        links.add(new ChainLink<>(LinkType.SYNC_AT_ENTITY, v -> {
            task.run();
            return v;
        }, entity));
        return this;
    }

    @Override
    public @NotNull <R> TaskChain<R> syncAt(@NotNull Entity entity, @NotNull Function<T, R> task) {
        @SuppressWarnings("unchecked")
        TaskChainBuilder<R> newChain = (TaskChainBuilder<R>) this;
        newChain.links.add(new ChainLink<>(LinkType.SYNC_AT_ENTITY, task, entity));
        return newChain;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull TaskChain<T> syncAtConsume(@NotNull Entity entity, @NotNull Consumer<T> task) {
        links.add(new ChainLink<>(LinkType.SYNC_AT_ENTITY, v -> {
            task.accept((T) v);
            return v;
        }, entity));
        return this;
    }

    @Override
    public @NotNull TaskChain<T> syncAt(@NotNull Location location, @NotNull Runnable task) {
        links.add(new ChainLink<>(LinkType.SYNC_AT_LOCATION, v -> {
            task.run();
            return v;
        }, location));
        return this;
    }

    @Override
    public @NotNull <R> TaskChain<R> syncAt(@NotNull Location location, @NotNull Function<T, R> task) {
        @SuppressWarnings("unchecked")
        TaskChainBuilder<R> newChain = (TaskChainBuilder<R>) this;
        newChain.links.add(new ChainLink<>(LinkType.SYNC_AT_LOCATION, task, location));
        return newChain;
    }

    @Override
    public @NotNull TaskChain<T> async(@NotNull Runnable task) {
        links.add(new ChainLink<>(LinkType.ASYNC, v -> {
            task.run();
            return v;
        }));
        return this;
    }

    @Override
    public @NotNull <R> TaskChain<R> async(@NotNull Function<T, R> task) {
        @SuppressWarnings("unchecked")
        TaskChainBuilder<R> newChain = (TaskChainBuilder<R>) this;
        newChain.links.add(new ChainLink<>(LinkType.ASYNC, task));
        return newChain;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull TaskChain<T> asyncConsume(@NotNull Consumer<T> task) {
        links.add(new ChainLink<>(LinkType.ASYNC, v -> {
            task.accept((T) v);
            return v;
        }));
        return this;
    }

    @Override
    public @NotNull <R> TaskChain<R> asyncSupply(@NotNull Supplier<R> task) {
        @SuppressWarnings("unchecked")
        TaskChainBuilder<R> newChain = (TaskChainBuilder<R>) this;
        newChain.links.add(new ChainLink<>(LinkType.ASYNC, v -> task.get()));
        return newChain;
    }

    @Override
    public @NotNull TaskChain<T> delay(long delay, @NotNull TimeUnit unit) {
        links.add(new ChainLink<>(LinkType.DELAY, Function.identity(), unit.toMillis(delay) / 50)); // Convert to ticks
        return this;
    }

    @Override
    public @NotNull TaskChain<T> syncIf(@NotNull Supplier<Boolean> condition, @NotNull Runnable task) {
        links.add(new ChainLink<>(LinkType.SYNC, v -> {
            if (condition.get()) {
                task.run();
            }
            return v;
        }));
        return this;
    }

    @Override
    public @NotNull <R> TaskChain<R> syncIf(@NotNull Supplier<Boolean> condition, @NotNull Function<T, R> task) {
        @SuppressWarnings("unchecked")
        TaskChainBuilder<R> newChain = (TaskChainBuilder<R>) this;
        newChain.links.add(new ChainLink<>(LinkType.SYNC, v -> condition.get() ? task.apply((T) v) : (R) v));
        return newChain;
    }

    @Override
    public @NotNull TaskChain<T> exceptionally(@NotNull Consumer<Throwable> handler) {
        this.exceptionHandler = handler;
        return this;
    }

    @Override
    public @NotNull TaskChain<T> recover(@NotNull Function<Throwable, T> recovery) {
        this.recoveryFunction = recovery;
        return this;
    }

    @Override
    public @NotNull TaskChain<T> finallyDo(@NotNull Runnable task) {
        this.finallyBlock = task;
        return this;
    }

    @Override
    public @NotNull TaskChain<T> thenAccept(@NotNull Consumer<T> callback) {
        this.successCallback = callback;
        return this;
    }

    @Override
    public @NotNull TaskChain<T> whenComplete(@NotNull Runnable callback) {
        this.completeCallback = callback;
        return this;
    }

    @Override
    public @NotNull CompletableFuture<T> execute() {
        future = new CompletableFuture<>();
        executeChain(0, currentValue);
        return future;
    }

    @Override
    public T executeAndWait() throws Exception {
        return execute().get();
    }

    @Override
    public boolean cancel() {
        if (!isDone()) {
            cancelled = true;
            if (future != null) {
                future.cancel(true);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public boolean isDone() {
        return future != null && future.isDone();
    }

    @Override
    public T getCurrentValue() {
        return currentValue;
    }

    @Override
    public @NotNull CompletableFuture<T> toCompletableFuture() {
        if (future == null) {
            return execute();
        }
        return future;
    }

    // Private helper methods

    @SuppressWarnings("unchecked")
    private void executeChain(int index, Object value) {
        if (cancelled) {
            future.cancel(true);
            runFinally();
            return;
        }

        if (index >= links.size()) {
            // Chain completed successfully
            currentValue = (T) value;
            if (successCallback != null) {
                try {
                    successCallback.accept(currentValue);
                } catch (Exception e) {
                    handleException(e);
                    return;
                }
            }
            future.complete(currentValue);
            runFinally();
            if (completeCallback != null) {
                completeCallback.run();
            }
            return;
        }

        ChainLink<Object, Object> link = (ChainLink<Object, Object>) links.get(index);

        try {
            switch (link.type) {
                case SYNC -> plugin.getScheduler().run(() -> executeLinkAndContinue(link, index, value));
                case ASYNC -> plugin.getScheduler().runAsync(() -> executeLinkAndContinue(link, index, value));
                case SYNC_AT_ENTITY -> {
                    if (link.entity != null) {
                        plugin.getScheduler().runAt(link.entity, () -> executeLinkAndContinue(link, index, value));
                    } else {
                        executeChain(index + 1, value);
                    }
                }
                case SYNC_AT_LOCATION -> {
                    if (link.location != null) {
                        plugin.getScheduler().runAt(link.location, () -> executeLinkAndContinue(link, index, value));
                    } else {
                        executeChain(index + 1, value);
                    }
                }
                case DELAY -> {
                    long ticks = link.delayTicks;
                    plugin.getScheduler().runLater(() -> executeChain(index + 1, value), ticks, TimeUnit.MILLISECONDS);
                }
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    private void executeLinkAndContinue(ChainLink<Object, Object> link, int index, Object value) {
        try {
            Object result = link.function.apply(value);
            executeChain(index + 1, result);
        } catch (Exception e) {
            handleException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private void handleException(Throwable throwable) {
        if (exceptionHandler != null) {
            try {
                exceptionHandler.accept(throwable);
            } catch (Exception e) {
                plugin.getLogger().severe("Error in exception handler: " + e.getMessage());
                e.printStackTrace();
            }
        }

        if (recoveryFunction != null) {
            try {
                T recovered = recoveryFunction.apply(throwable);
                future.complete(recovered);
                runFinally();
                if (completeCallback != null) {
                    completeCallback.run();
                }
                return;
            } catch (Exception e) {
                plugin.getLogger().severe("Error in recovery function: " + e.getMessage());
                e.printStackTrace();
            }
        }

        future.completeExceptionally(throwable);
        runFinally();
        if (completeCallback != null) {
            completeCallback.run();
        }
    }

    private void runFinally() {
        if (finallyBlock != null) {
            try {
                finallyBlock.run();
            } catch (Exception e) {
                plugin.getLogger().severe("Error in finally block: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Helper classes

    private enum LinkType {
        SYNC,
        ASYNC,
        SYNC_AT_ENTITY,
        SYNC_AT_LOCATION,
        DELAY
    }

    private static class ChainLink<I, O> {
        final LinkType type;
        final Function<I, O> function;
        final Entity entity;
        final Location location;
        final long delayTicks;

        ChainLink(LinkType type, Function<I, O> function) {
            this(type, function, null, null, 0);
        }

        ChainLink(LinkType type, Function<I, O> function, Entity entity) {
            this(type, function, entity, null, 0);
        }

        ChainLink(LinkType type, Function<I, O> function, Location location) {
            this(type, function, null, location, 0);
        }

        ChainLink(LinkType type, Function<I, O> function, long delayTicks) {
            this(type, function, null, null, delayTicks);
        }

        ChainLink(LinkType type, Function<I, O> function, Entity entity, Location location, long delayTicks) {
            this.type = type;
            this.function = function;
            this.entity = entity;
            this.location = location;
            this.delayTicks = delayTicks;
        }
    }
}
