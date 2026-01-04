# BossBars

Show progress bars and notifications to players.

```java
import com.ionapi.ui.IonBossBar;
import net.kyori.adventure.bossbar.BossBar;

public void startCountdown(Player player) {
    IonBossBar bar = IonBossBar.create()
        .title("<yellow><bold>Event Starting...")
        .progress(1.0f)
        .color(BossBar.Color.YELLOW)
        .overlay(BossBar.Overlay.NOTCHED_10)
        .show(player);
    
    // Countdown from 10 to 0
    final int[] countdown = {10};
    getScheduler().runTimer(() -> {
        if (countdown[0] <= 0) {
            bar.hide(player);
            player.sendMessage("<green>Event started!");
        } else {
            bar.title("<yellow><bold>Starting in " + countdown[0] + "...");
            bar.progress(countdown[0] / 10.0f);
            countdown[0]--;
        }
    }, 0, 1, TimeUnit.SECONDS);
}

// Dynamic boss bar
IonBossBar bossHealth = IonBossBar.create()
    .dynamicTitle(b -> "<red>Boss: " + boss.getName() + " <white>" + boss.getHealth() + "❤")
    .dynamicProgress(b -> (float) (boss.getHealth() / boss.getMaxHealth()))
    .color(BossBar.Color.RED)
    .autoUpdate(5L)
    .show(playersInArena);
```
