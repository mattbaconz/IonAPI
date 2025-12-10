package com.ionapi.ui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Fluent API for creating and managing scoreboards.
 * Supports MiniMessage formatting, dynamic updates, and animations.
 *
 * <p>
 * This implementation uses per-line Team prefix updates to avoid
 * the flashing effect caused by clearing and re-adding entries.
 * </p>
 *
 * <p>
 * Example usage:
 * 
 * <pre>{@code
 * IonScoreboard board = IonScoreboard.builder()
 *         .title("<gradient:gold:yellow><bold>My Server")
 *         .line(15, "<gray>Welcome, <white>{player}")
 *         .line(14, "")
 *         .line(13, "<gold>Coins: <yellow>{coins}")
 *         .line(12, "<green>Online: <white>{online}")
 *         .placeholder("player", p -> p.getName())
 *         .placeholder("coins", p -> String.valueOf(getCoins(p)))
 *         .placeholder("online", p -> String.valueOf(Bukkit.getOnlinePlayers().size()))
 *         .updateInterval(20) // Auto-update every second
 *         .build();
 *
 * board.show(player);
 * }</pre>
 *
 * @since 1.2.0
 */
public class IonScoreboard {

    private static final Map<UUID, IonScoreboard> ACTIVE_BOARDS = new ConcurrentHashMap<>();
    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final Plugin PLUGIN = JavaPlugin.getProvidingPlugin(IonScoreboard.class);

    // Configuration
    private final String title;
    private final Map<Integer, String> lines;
    private final Map<Integer, AnimatedLine> animatedLines;
    private final Map<String, Function<Player, String>> placeholders;
    private final long updateInterval;

    // State per player
    private final Map<UUID, PlayerBoard> playerBoards = new ConcurrentHashMap<>();

    // Auto-update task
    private BukkitTask updateTask;
    private BukkitTask animationTask;

    private IonScoreboard(Builder builder) {
        this.title = builder.title;
        this.lines = new LinkedHashMap<>(builder.lines);
        this.animatedLines = new LinkedHashMap<>(builder.animatedLines);
        this.placeholders = new HashMap<>(builder.placeholders);
        this.updateInterval = builder.updateInterval;
    }

    /**
     * Creates a new scoreboard builder.
     *
     * @return a new builder
     */
    @NotNull
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Gets the active scoreboard for a player.
     *
     * @param player the player
     * @return the active scoreboard, or null
     */
    @Nullable
    public static IonScoreboard getActive(@NotNull Player player) {
        return ACTIVE_BOARDS.get(player.getUniqueId());
    }

    /**
     * Shows this scoreboard to a player.
     *
     * @param player the player
     */
    public void show(@NotNull Player player) {
        // Remove any existing scoreboard
        IonScoreboard existing = ACTIVE_BOARDS.get(player.getUniqueId());
        if (existing != null && existing != this) {
            existing.hide(player);
        }

        // Create new scoreboard
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = board.registerNewObjective("ionboard", Criteria.DUMMY, parseText(title, player));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Create player board state
        PlayerBoard playerBoard = new PlayerBoard(board, objective);
        playerBoards.put(player.getUniqueId(), playerBoard);
        ACTIVE_BOARDS.put(player.getUniqueId(), this);

        // Initialize all lines (this sets up teams and entries)
        initializeLines(player, playerBoard);

        // Apply scoreboard to player
        player.setScoreboard(board);

        // Start auto-update if configured
        startTasks();
    }

    /**
     * Hides this scoreboard from a player.
     *
     * @param player the player
     */
    public void hide(@NotNull Player player) {
        playerBoards.remove(player.getUniqueId());
        ACTIVE_BOARDS.remove(player.getUniqueId());
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());

        // Stop tasks if no viewers
        if (playerBoards.isEmpty()) {
            stopTasks();
        }
    }

    /**
     * Updates the scoreboard for a player.
     * Only updates lines that have changed content.
     *
     * @param player the player
     */
    public void update(@NotNull Player player) {
        PlayerBoard playerBoard = playerBoards.get(player.getUniqueId());
        if (playerBoard == null)
            return;

        // Update title
        playerBoard.objective.displayName(parseText(title, player));

        // Update each line - only if content changed
        updateLinesForPlayer(player, playerBoard);
    }

    /**
     * Updates the scoreboard for all viewers.
     */
    public void updateAll() {
        for (UUID uuid : playerBoards.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                update(player);
            }
        }
    }

    /**
     * Updates a specific line for a player.
     * This is efficient as it only updates the team prefix.
     *
     * @param player the player
     * @param score  the line score
     * @param text   the new text (supports MiniMessage and placeholders)
     */
    public void setLine(@NotNull Player player, int score, @NotNull String text) {
        lines.put(score, text);

        PlayerBoard playerBoard = playerBoards.get(player.getUniqueId());
        if (playerBoard == null)
            return;

        String parsedText = parsePlaceholders(text, player);
        Component component = MINI.deserialize(parsedText);

        Team team = playerBoard.board.getTeam("line" + score);
        if (team != null) {
            // Only update if content actually changed
            String newContent = MINI.serialize(component);
            String oldContent = playerBoard.lineCache.get(score);

            if (!newContent.equals(oldContent)) {
                team.prefix(component);
                playerBoard.lineCache.put(score, newContent);
            }
        } else {
            // Line doesn't exist, create it
            createLine(player, playerBoard, score, text);
        }
    }

    /**
     * Removes a line from the scoreboard.
     *
     * @param score the line score to remove
     */
    public void removeLine(int score) {
        lines.remove(score);
        animatedLines.remove(score);

        for (PlayerBoard playerBoard : playerBoards.values()) {
            Team team = playerBoard.board.getTeam("line" + score);
            if (team != null) {
                String entry = playerBoard.lineEntries.remove(score);
                if (entry != null) {
                    playerBoard.board.resetScores(entry);
                }
                team.unregister();
            }
            playerBoard.lineCache.remove(score);
        }
    }

    /**
     * Checks if this scoreboard is shown to a player.
     *
     * @param player the player
     * @return true if shown
     */
    public boolean isShown(@NotNull Player player) {
        return playerBoards.containsKey(player.getUniqueId());
    }

    /**
     * Gets all players viewing this scoreboard.
     *
     * @return set of player UUIDs
     */
    @NotNull
    public Set<UUID> getViewers() {
        return Collections.unmodifiableSet(playerBoards.keySet());
    }

    /**
     * Destroys this scoreboard and removes it from all players.
     */
    public void destroy() {
        stopTasks();
        for (UUID uuid : new HashSet<>(playerBoards.keySet())) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                hide(player);
            }
        }
        playerBoards.clear();
    }

    // ==================== Private Methods ====================

    private void initializeLines(Player player, PlayerBoard playerBoard) {
        // Create entries for each line score
        for (Map.Entry<Integer, String> entry : lines.entrySet()) {
            createLine(player, playerBoard, entry.getKey(), entry.getValue());
        }

        // Handle animated lines
        for (Map.Entry<Integer, AnimatedLine> entry : animatedLines.entrySet()) {
            int score = entry.getKey();
            AnimatedLine animated = entry.getValue();
            String text = animated.getCurrentFrame();
            createLine(player, playerBoard, score, text);
        }
    }

    private void createLine(Player player, PlayerBoard playerBoard, int score, String text) {
        Scoreboard board = playerBoard.board;
        Objective objective = playerBoard.objective;

        String parsedText = parsePlaceholders(text, player);
        Component component = MINI.deserialize(parsedText);

        // Create unique entry for this line
        String entryName = createUniqueEntry(score);

        // Create or get team for this line
        Team team = board.getTeam("line" + score);
        if (team == null) {
            team = board.registerNewTeam("line" + score);
        }

        team.addEntry(entryName);
        team.prefix(component);

        // Set the score
        objective.getScore(entryName).setScore(score);

        // Cache the content and entry
        playerBoard.lineCache.put(score, MINI.serialize(component));
        playerBoard.lineEntries.put(score, entryName);
    }

    private void updateLinesForPlayer(Player player, PlayerBoard playerBoard) {
        // Update static lines
        for (Map.Entry<Integer, String> entry : lines.entrySet()) {
            int score = entry.getKey();
            String text = entry.getValue();
            updateSingleLine(player, playerBoard, score, text);
        }

        // Update animated lines
        for (Map.Entry<Integer, AnimatedLine> entry : animatedLines.entrySet()) {
            int score = entry.getKey();
            AnimatedLine animated = entry.getValue();
            String text = animated.getCurrentFrame();
            updateSingleLine(player, playerBoard, score, text);
        }
    }

    private void updateSingleLine(Player player, PlayerBoard playerBoard, int score, String text) {
        String parsedText = parsePlaceholders(text, player);
        Component component = MINI.deserialize(parsedText);
        String newContent = MINI.serialize(component);
        String oldContent = playerBoard.lineCache.get(score);

        // Only update if content actually changed - this prevents flashing!
        if (!newContent.equals(oldContent)) {
            Team team = playerBoard.board.getTeam("line" + score);
            if (team != null) {
                team.prefix(component);
                playerBoard.lineCache.put(score, newContent);
            }
        }
    }

    private String createUniqueEntry(int score) {
        // Use color codes and formatting to create unique invisible entries
        // Each line gets a unique entry based on its score
        StringBuilder entry = new StringBuilder();
        String hex = Integer.toHexString(Math.abs(score) % 16);
        entry.append("§").append(hex);

        // Add additional uniqueness for scores > 15
        int remaining = score / 16;
        while (remaining > 0) {
            entry.append("§r");
            remaining--;
        }

        return entry.toString();
    }

    private void startTasks() {
        // Start auto-update task
        if (updateInterval > 0 && updateTask == null) {
            updateTask = Bukkit.getScheduler().runTaskTimer(PLUGIN, this::updateAll, updateInterval, updateInterval);
        }

        // Start animation task (runs every tick)
        if (!animatedLines.isEmpty() && animationTask == null) {
            animationTask = Bukkit.getScheduler().runTaskTimer(PLUGIN, () -> {
                boolean needsUpdate = false;
                for (AnimatedLine animated : animatedLines.values()) {
                    if (animated.tick()) {
                        needsUpdate = true;
                    }
                }
                if (needsUpdate) {
                    updateAll();
                }
            }, 1L, 1L);
        }
    }

    private void stopTasks() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
        if (animationTask != null) {
            animationTask.cancel();
            animationTask = null;
        }
    }

    private String parsePlaceholders(String text, Player player) {
        for (Map.Entry<String, Function<Player, String>> entry : placeholders.entrySet()) {
            text = text.replace("{" + entry.getKey() + "}", entry.getValue().apply(player));
        }
        return text;
    }

    private Component parseText(String text, Player player) {
        return MINI.deserialize(parsePlaceholders(text, player));
    }

    // ==================== Inner Classes ====================

    /**
     * Per-player board state tracking.
     */
    private static class PlayerBoard {
        final Scoreboard board;
        final Objective objective;
        final Map<Integer, String> lineCache = new HashMap<>();
        final Map<Integer, String> lineEntries = new HashMap<>();

        PlayerBoard(Scoreboard board, Objective objective) {
            this.board = board;
            this.objective = objective;
        }
    }

    /**
     * Animated line that cycles through frames.
     */
    public static class AnimatedLine {
        private final String[] frames;
        private final long intervalTicks;
        private int currentFrame = 0;
        private long tickCounter = 0;

        public AnimatedLine(String[] frames, long intervalTicks) {
            this.frames = frames;
            this.intervalTicks = intervalTicks;
        }

        public String getCurrentFrame() {
            return frames[currentFrame];
        }

        public boolean tick() {
            tickCounter++;
            if (tickCounter >= intervalTicks) {
                tickCounter = 0;
                currentFrame = (currentFrame + 1) % frames.length;
                return true;
            }
            return false;
        }

        public void reset() {
            currentFrame = 0;
            tickCounter = 0;
        }
    }

    /**
     * Builder for IonScoreboard.
     */
    public static class Builder {
        private String title = "<white>Scoreboard";
        private final Map<Integer, String> lines = new LinkedHashMap<>();
        private final Map<Integer, AnimatedLine> animatedLines = new LinkedHashMap<>();
        private final Map<String, Function<Player, String>> placeholders = new HashMap<>();
        private long updateInterval = 0;

        /**
         * Sets the scoreboard title.
         *
         * @param title the title (supports MiniMessage)
         * @return this builder
         */
        @NotNull
        public Builder title(@NotNull String title) {
            this.title = title;
            return this;
        }

        /**
         * Adds a line at the specified score.
         *
         * @param score the score (higher = higher on board)
         * @param text  the line text (supports MiniMessage and placeholders)
         * @return this builder
         */
        @NotNull
        public Builder line(int score, @NotNull String text) {
            lines.put(score, text);
            return this;
        }

        /**
         * Adds multiple lines starting from a score.
         *
         * @param startScore the starting score
         * @param texts      the line texts
         * @return this builder
         */
        @NotNull
        public Builder lines(int startScore, @NotNull String... texts) {
            for (int i = 0; i < texts.length; i++) {
                lines.put(startScore - i, texts[i]);
            }
            return this;
        }

        /**
         * Adds an animated line that cycles through frames.
         *
         * @param score         the line score
         * @param intervalTicks ticks between frame changes
         * @param frames        the frames to cycle through
         * @return this builder
         */
        @NotNull
        public Builder animatedLine(int score, long intervalTicks, @NotNull String... frames) {
            if (frames.length > 0) {
                animatedLines.put(score, new AnimatedLine(frames, intervalTicks));
            }
            return this;
        }

        /**
         * Adds an animated line with default interval (10 ticks).
         *
         * @param score  the line score
         * @param frames the frames to cycle through
         * @return this builder
         */
        @NotNull
        public Builder animatedLine(int score, @NotNull String... frames) {
            return animatedLine(score, 10, frames);
        }

        /**
         * Registers a placeholder.
         *
         * @param key      the placeholder key (without braces)
         * @param resolver the function to resolve the value
         * @return this builder
         */
        @NotNull
        public Builder placeholder(@NotNull String key, @NotNull Function<Player, String> resolver) {
            placeholders.put(key, resolver);
            return this;
        }

        /**
         * Sets the auto-update interval.
         * When set, the scoreboard will automatically update for all viewers.
         *
         * @param ticks the update interval in ticks (20 ticks = 1 second)
         * @return this builder
         */
        @NotNull
        public Builder updateInterval(long ticks) {
            this.updateInterval = Math.max(0, ticks);
            return this;
        }

        /**
         * Builds the scoreboard.
         *
         * @return the built scoreboard
         */
        @NotNull
        public IonScoreboard build() {
            return new IonScoreboard(this);
        }
    }
}
