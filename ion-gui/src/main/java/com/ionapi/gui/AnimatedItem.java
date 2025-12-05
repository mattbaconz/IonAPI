package com.ionapi.gui;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Represents an animated item that cycles through multiple states.
 * 
 * Example:
 * <pre>{@code
 * AnimatedItem loading = AnimatedItem.create()
 *     .frame(loadingFrame1)
 *     .frame(loadingFrame2)
 *     .frame(loadingFrame3)
 *     .interval(5) // 5 ticks per frame
 *     .loop(true)
 *     .build();
 * }</pre>
 */
public class AnimatedItem {

    private final List<ItemStack> frames;
    private final long intervalTicks;
    private final boolean loop;
    private final Consumer<AnimatedItem> onComplete;
    
    private int currentFrame;
    private long tickCounter;
    private boolean running;
    private boolean completed;

    private AnimatedItem(List<ItemStack> frames, long intervalTicks, boolean loop, Consumer<AnimatedItem> onComplete) {
        this.frames = new ArrayList<>(frames);
        this.intervalTicks = intervalTicks;
        this.loop = loop;
        this.onComplete = onComplete;
        this.currentFrame = 0;
        this.tickCounter = 0;
        this.running = true;
        this.completed = false;
    }

    public static Builder create() {
        return new Builder();
    }

    public static AnimatedItem of(@NotNull ItemStack... frames) {
        return new Builder().frames(frames).build();
    }

    public static AnimatedItem of(long intervalTicks, @NotNull ItemStack... frames) {
        return new Builder().frames(frames).interval(intervalTicks).build();
    }

    /**
     * Gets the current frame to display.
     */
    @NotNull
    public ItemStack getCurrentFrame() {
        if (frames.isEmpty()) {
            throw new IllegalStateException("AnimatedItem has no frames");
        }
        return frames.get(currentFrame);
    }

    /**
     * Advances the animation by one tick.
     * @return true if the frame changed
     */
    public boolean tick() {
        if (!running || completed || frames.isEmpty()) {
            return false;
        }

        tickCounter++;
        if (tickCounter >= intervalTicks) {
            tickCounter = 0;
            return nextFrame();
        }
        return false;
    }

    private boolean nextFrame() {
        currentFrame++;
        if (currentFrame >= frames.size()) {
            if (loop) {
                currentFrame = 0;
            } else {
                currentFrame = frames.size() - 1;
                completed = true;
                running = false;
                if (onComplete != null) {
                    onComplete.accept(this);
                }
            }
        }
        return true;
    }

    public void reset() {
        currentFrame = 0;
        tickCounter = 0;
        completed = false;
        running = true;
    }

    public void pause() { running = false; }
    public void resume() { running = true; }
    public void stop() { running = false; completed = true; }

    public boolean isRunning() { return running; }
    public boolean isCompleted() { return completed; }
    public int getFrameCount() { return frames.size(); }
    public int getCurrentFrameIndex() { return currentFrame; }

    public void setFrame(int index) {
        if (index >= 0 && index < frames.size()) {
            currentFrame = index;
        }
    }

    public static class Builder {
        private final List<ItemStack> frames = new ArrayList<>();
        private long intervalTicks = 10;
        private boolean loop = true;
        private Consumer<AnimatedItem> onComplete;

        public Builder frame(@NotNull ItemStack item) {
            frames.add(item.clone());
            return this;
        }

        public Builder frames(@NotNull ItemStack... items) {
            frames.addAll(Arrays.asList(items));
            return this;
        }

        public Builder frames(@NotNull List<ItemStack> items) {
            frames.addAll(items);
            return this;
        }

        public Builder interval(long ticks) {
            this.intervalTicks = Math.max(1, ticks);
            return this;
        }

        public Builder loop(boolean loop) {
            this.loop = loop;
            return this;
        }

        public Builder onComplete(@NotNull Consumer<AnimatedItem> callback) {
            this.onComplete = callback;
            return this;
        }

        public AnimatedItem build() {
            if (frames.isEmpty()) {
                throw new IllegalStateException("AnimatedItem must have at least one frame");
            }
            return new AnimatedItem(frames, intervalTicks, loop, onComplete);
        }
    }
}
