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
import java.util.function.Function;

/**
 * Implementation of IonScoreboard for managing player scoreboards.
 */
public class IonScoreboardBuilder implements IonScoreboard {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final Plugin PLUGIN = JavaPlugin.getProvidingPlugin(IonScoreboardBuilder.class);
    private static final int MAX_LINES = 15;

    private final Player player;
    private Component title;
    private final List<Component> lines;
    private final Map<Integer, Function<Player, String>> dynamicLines;
    private Function<Player, String> dynamicTitleSupplier;

    private Scoreboard scoreboard;
    private Objective objective;
    private boolean visible;
    private boolean autoUpdate;
    private BukkitTask updateTask;
    private long updateInterval;

    /**
     * Creates a new scoreboard builder for the specified player.
     *
     * @param player the player
     */
    public IonScoreboardBuilder(@NotNull Player player) {
        this.player = player;
        this.title = Component.text("Scoreboard");
        this.lines = new ArrayList<>();
        this.dynamicLines = new HashMap<>();
        this.visible = false;
        this.autoUpdate = false;
        this.updateInterval = 20L;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }

    @Override
    public @NotNull IonScoreboard title(@NotNull String title) {
        this.title = MINI_MESSAGE.deserialize(title);
        if (visible && objective != null) {
            objective.displayName(this.title);
        }
        return this;
    }

    @Override
    public @NotNull IonScoreboard title(@NotNull Component title) {
        this.title = title;
        if (visible && objective != null) {
            objective.displayName(this.title);
        }
        return this;
    }

    @Override
    public @NotNull Component getTitle() {
        return title;
    }

    @Override
    public @NotNull IonScoreboard line(@NotNull String text) {
        if (lines.size() < MAX_LINES) {
            lines.add(MINI_MESSAGE.deserialize(text));
            if (visible) {
                rebuildScoreboard();
            }
        }
        return this;
    }

    @Override
    public @NotNull IonScoreboard line(@NotNull Component component) {
        if (lines.size() < MAX_LINES) {
            lines.add(component);
            if (visible) {
                rebuildScoreboard();
            }
        }
        return this;
    }

    @Override
    public @NotNull IonScoreboard lines(@NotNull String... lines) {
        return lines(Arrays.asList(lines));
    }

    @Override
    public @NotNull IonScoreboard lines(@NotNull List<String> lines) {
        this.lines.clear();
        for (String line : lines) {
            if (this.lines.size() >= MAX_LINES) break;
            this.lines.add(MINI_MESSAGE.deserialize(line));
        }
        if (visible) {
            rebuildScoreboard();
        }
        return this;
    }

    @Override
    public @NotNull IonScoreboard linesComponents(@NotNull List<Component> lines) {
        this.lines.clear();
        for (Component line : lines) {
            if (this.lines.size() >= MAX_LINES) break;
            this.lines.add(line);
        }
        if (visible) {
            rebuildScoreboard();
        }
        return this;
    }

    @Override
    public @NotNull IonScoreboard updateLine(int index, @NotNull String text) {
        return updateLine(index, MINI_MESSAGE.deserialize(text));
    }

    @Override
    public @NotNull IonScoreboard updateLine(int index, @NotNull Component component) {
        if (index >= 0 && index < lines.size()) {
            lines.set(index, component);
            if (visible) {
                updateScoreboardLine(index);
            }
        }
        return this;
    }

    @Override
    public @NotNull IonScoreboard removeLine(int index) {
        if (index >= 0 && index < lines.size()) {
            lines.remove(index);
            if (visible) {
                rebuildScoreboard();
            }
        }
        return this;
    }

    @Override
    public @NotNull IonScoreboard insertLine(int index, @NotNull String text) {
        if (index >= 0 && index <= lines.size() && lines.size() < MAX_LINES) {
            lines.add(index, MINI_MESSAGE.deserialize(text));
            if (visible) {
                rebuildScoreboard();
            }
        }
        return this;
    }

    @Override
    public @NotNull IonScoreboard clearLines() {
        lines.clear();
        dynamicLines.clear();
        if (visible) {
            rebuildScoreboard();
        }
        return this;
    }

    @Override
    public @NotNull List<Component> getLines() {
        return new ArrayList<>(lines);
    }

    @Override
    public int getLineCount() {
        return lines.size();
    }

    @Override
    public @NotNull IonScoreboard dynamicLine(int index, @NotNull Function<Player, String> supplier) {
        dynamicLines.put(index, supplier);
        return this;
    }

    @Override
    public @NotNull IonScoreboard dynamicTitle(@NotNull Function<Player, String> supplier) {
        this.dynamicTitleSupplier = supplier;
        return this;
    }

    @Override
    public @NotNull IonScoreboard autoUpdate(long intervalTicks) {
        this.autoUpdate = true;
        this.updateInterval = intervalTicks;
        startAutoUpdate();
        return this;
    }

    @Override
    public @NotNull IonScoreboard stopAutoUpdate() {
        this.autoUpdate = false;
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
        return this;
    }

    @Override
    public boolean isAutoUpdating() {
        return autoUpdate && updateTask != null;
    }

    @Override
    public @NotNull IonScoreboard update() {
        // Update dynamic title
        if (dynamicTitleSupplier != null) {
            title(dynamicTitleSupplier.apply(player));
        }

        // Update dynamic lines
        for (Map.Entry<Integer, Function<Player, String>> entry : dynamicLines.entrySet()) {
            int index = entry.getKey();
            String text = entry.getValue().apply(player);
            if (index >= 0 && index < lines.size()) {
                updateLine(index, text);
            }
        }

        return this;
    }

    @Override
    public @NotNull IonScoreboard show() {
        if (!visible) {
            createScoreboard();
            player.setScoreboard(scoreboard);
            visible = true;

            if (autoUpdate) {
                startAutoUpdate();
            }
        }
        return this;
    }

    @Override
    public @NotNull IonScoreboard hide() {
        if (visible) {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            visible = false;
            stopAutoUpdate();
        }
        return this;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void destroy() {
        hide();
        if (scoreboard != null && objective != null) {
            objective.unregister();
            scoreboard = null;
            objective = null;
        }
        lines.clear();
        dynamicLines.clear();
        dynamicTitleSupplier = null;
    }

    @Override
    public @Nullable Scoreboard getBukkitScoreboard() {
        return scoreboard;
    }

    // Private helper methods

    private void createScoreboard() {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;

        scoreboard = manager.getNewScoreboard();
        objective = scoreboard.registerNewObjective(
                "ion_board_" + player.getUniqueId().toString().substring(0, 8),
                Criteria.DUMMY,
                title
        );
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        updateScoreboardLines();
    }

    private void rebuildScoreboard() {
        if (!visible || scoreboard == null || objective == null) return;

        // Unregister old objective
        objective.unregister();

        // Create new objective
        objective = scoreboard.registerNewObjective(
                "ion_board_" + player.getUniqueId().toString().substring(0, 8),
                Criteria.DUMMY,
                title
        );
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        updateScoreboardLines();
    }

    private void updateScoreboardLines() {
        if (objective == null) return;

        // Clear existing scores
        for (String entry : scoreboard.getEntries()) {
            scoreboard.resetScores(entry);
        }

        // Add lines (in reverse order, as scoreboard counts from bottom)
        int score = lines.size();
        for (int i = 0; i < lines.size(); i++) {
            Component line = lines.get(i);

            // Create unique entry using color codes for uniqueness
            String entry = createUniqueEntry(i);
            Team team = scoreboard.getTeam("line_" + i);
            if (team == null) {
                team = scoreboard.registerNewTeam("line_" + i);
            }

            team.addEntry(entry);
            team.prefix(line);

            objective.getScore(entry).setScore(score--);
        }
    }

    private void updateScoreboardLine(int index) {
        if (objective == null || index < 0 || index >= lines.size()) return;

        String entry = createUniqueEntry(index);
        Team team = scoreboard.getTeam("line_" + index);
        if (team != null) {
            team.prefix(lines.get(index));
        }
    }

    private String createUniqueEntry(int index) {
        // Create unique entry using invisible characters
        StringBuilder sb = new StringBuilder("§r");
        for (int i = 0; i < index; i++) {
            sb.append("§r");
        }
        return sb.toString();
    }

    private void startAutoUpdate() {
        if (autoUpdate && visible && updateTask == null) {
            updateTask = Bukkit.getScheduler().runTaskTimer(PLUGIN, this::update, updateInterval, updateInterval);
        }
    }
}
