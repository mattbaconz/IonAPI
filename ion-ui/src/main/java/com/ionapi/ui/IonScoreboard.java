package com.ionapi.ui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Fluent API for creating and managing scoreboards.
 * Supports MiniMessage formatting and dynamic updates.
 *
 * <p>Example usage:
 * <pre>{@code
 * IonScoreboard board = IonScoreboard.builder()
 *     .title("<gradient:gold:yellow><bold>My Server")
 *     .line(15, "<gray>Welcome, <white>{player}")
 *     .line(14, "")
 *     .line(13, "<gold>Coins: <yellow>{coins}")
 *     .line(12, "<green>Online: <white>{online}")
 *     .placeholder("player", p -> p.getName())
 *     .placeholder("coins", p -> String.valueOf(getCoins(p)))
 *     .placeholder("online", p -> String.valueOf(Bukkit.getOnlinePlayers().size()))
 *     .build();
 *
 * board.show(player);
 * board.update(player); // Call periodically to refresh
 * }</pre>
 *
 * @since 1.2.0
 */
public class IonScoreboard {

    private static final Map<UUID, IonScoreboard> ACTIVE_BOARDS = new ConcurrentHashMap<>();
    private static final MiniMessage MINI = MiniMessage.miniMessage();

    private final String title;
    private final Map<Integer, String> lines;
    private final Map<String, Function<Player, String>> placeholders;
    private final Map<UUID, Scoreboard> playerBoards = new ConcurrentHashMap<>();

    private IonScoreboard(Builder builder) {
        this.title = builder.title;
        this.lines = new LinkedHashMap<>(builder.lines);
        this.placeholders = new HashMap<>(builder.placeholders);
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

        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = board.registerNewObjective("ionboard", Criteria.DUMMY, parseText(title, player));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        updateLines(player, board, objective);

        player.setScoreboard(board);
        playerBoards.put(player.getUniqueId(), board);
        ACTIVE_BOARDS.put(player.getUniqueId(), this);
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
    }

    /**
     * Updates the scoreboard for a player.
     *
     * @param player the player
     */
    public void update(@NotNull Player player) {
        Scoreboard board = playerBoards.get(player.getUniqueId());
        if (board == null) return;

        Objective objective = board.getObjective("ionboard");
        if (objective == null) return;

        // Update title
        objective.displayName(parseText(title, player));

        // Clear and re-add lines
        updateLines(player, board, objective);
    }

    /**
     * Updates a specific line for a player.
     *
     * @param player the player
     * @param score the line score
     * @param text the new text
     */
    public void setLine(@NotNull Player player, int score, @NotNull String text) {
        lines.put(score, text);
        update(player);
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
        for (UUID uuid : new HashSet<>(playerBoards.keySet())) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                hide(player);
            }
        }
        playerBoards.clear();
    }

    private void updateLines(Player player, Scoreboard board, Objective objective) {
        // Remove old entries
        for (String entry : board.getEntries()) {
            board.resetScores(entry);
        }

        // Add new lines
        Set<String> usedEntries = new HashSet<>();
        for (Map.Entry<Integer, String> entry : lines.entrySet()) {
            String text = parsePlaceholders(entry.getValue(), player);
            Component component = parseText(text, player);

            // Create unique entry for each line
            String entryName = createUniqueEntry(text, usedEntries);
            usedEntries.add(entryName);

            Team team = board.getTeam("line" + entry.getKey());
            if (team == null) {
                team = board.registerNewTeam("line" + entry.getKey());
            }
            team.addEntry(entryName);
            team.prefix(component);

            objective.getScore(entryName).setScore(entry.getKey());
        }
    }

    private String createUniqueEntry(String text, Set<String> used) {
        // Use color codes to create unique invisible entries
        String base = "§" + Integer.toHexString(used.size() % 16);
        while (used.contains(base)) {
            base += "§r";
        }
        return base;
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

    /**
     * Builder for IonScoreboard.
     */
    public static class Builder {
        private String title = "<white>Scoreboard";
        private final Map<Integer, String> lines = new LinkedHashMap<>();
        private final Map<String, Function<Player, String>> placeholders = new HashMap<>();

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
         * @param text the line text (supports MiniMessage and placeholders)
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
         * @param texts the line texts
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
         * Registers a placeholder.
         *
         * @param key the placeholder key (without braces)
         * @param resolver the function to resolve the value
         * @return this builder
         */
        @NotNull
        public Builder placeholder(@NotNull String key, @NotNull Function<Player, String> resolver) {
            placeholders.put(key, resolver);
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
