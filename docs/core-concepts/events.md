# Events

### Define Your Event

```java
package com.example.myplugin.events;

import com.ionapi.api.event.IonEvent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayerLevelUpEvent implements IonEvent {
    private final Player player;
    private final int oldLevel;
    private int newLevel;
    private boolean cancelled = false;
    
    public PlayerLevelUpEvent(Player player, int oldLevel, int newLevel) {
        this.player = player;
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
    }
    
    public Player getPlayer() { return player; }
    public int getOldLevel() { return oldLevel; }
    public int getNewLevel() { return newLevel; }
    public void setNewLevel(int newLevel) { this.newLevel = newLevel; }
    
    @Override
    public @NotNull String getEventName() { return "PlayerLevelUp"; }
    
    @Override
    public boolean isCancelled() { return cancelled; }
    
    @Override
    public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
    
    @Override
    public boolean isCancellable() { return true; }
}
```

### Listen to Events

```java
private void registerEvents() {
    getEventBus().subscribe(PlayerLevelUpEvent.class, event -> {
        Player player = event.getPlayer();
        int newLevel = event.getNewLevel();
        
        // Send message
        player.sendMessage("<gold>Congratulations! You reached level " + newLevel + "!");
        
        // Give reward every 10 levels
        if (newLevel % 10 == 0) {
            player.sendMessage("<green>You received a bonus reward!");
        }
    });
}
```

### Fire Events

```java
public void levelUpPlayer(Player player, int newLevel) {
    int oldLevel = getCurrentLevel(player);
    
    // Create and fire event
    PlayerLevelUpEvent event = new PlayerLevelUpEvent(player, oldLevel, newLevel);
    getEventBus().fire(event);
    
    // Check if cancelled
    if (!event.isCancelled()) {
        // Apply the level change
        setPlayerLevel(player, event.getNewLevel());
    }
}
```
