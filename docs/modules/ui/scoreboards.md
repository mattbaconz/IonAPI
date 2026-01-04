# Scoreboards

Create dynamic scoreboards with auto-updating content.

```java
import com.ionapi.ui.IonScoreboard;

public void showStatsBoard(Player player) {
    IonScoreboard board = IonScoreboard.builder()
        .title("<gold><bold>Your Stats")
        .line(15, "<gray>━━━━━━━━━━━━━━")
        .line(14, "<yellow>Online: <white>{online}")
        .line(13, "<green>Health: <white>{health}")
        .line(12, "<aqua>Level: <white>{level}")
        .line(11, "<gray>━━━━━━━━━━━━━━")
        .placeholder("online", p -> String.valueOf(Bukkit.getOnlinePlayers().size()))
        .placeholder("health", p -> String.valueOf((int) p.getHealth()) + "/20")
        .placeholder("level", p -> String.valueOf(p.getLevel()))
        .updateInterval(20) // Update every second
        .build();
    
    board.show(player);
    // Store for cleanup later
    scoreboards.put(player.getUniqueId(), board);
}

// Update single line manually
board.setLine(player, 14, "<yellow>New text");
board.update(player);

// Cleanup
@Override
public void onDisable() {
    scoreboards.values().forEach(IonScoreboard::destroy);
    scoreboards.clear();
}
```
