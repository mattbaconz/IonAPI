# Task Chains

Build complex async/sync workflows with clean, readable code.

```java
import com.ionapi.tasks.TaskChain;

@EventHandler
public void onPlayerJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    
    TaskChain.create(this)
        // Step 1: Load data from database (async)
        .async(() -> {
            getLogger().info("Loading data for " + player.getName());
            return loadPlayerDataFromFile(player.getUniqueId());
        })
        // Step 2: Apply data to player (sync on player's thread)
        .syncAt(player, data -> {
            player.setLevel(data.level);
            player.setHealth(data.health);
            getLogger().info("Applied data for " + player.getName());
        })
        // Step 3: Wait 2 seconds
        .delay(2, TimeUnit.SECONDS)
        // Step 4: Send welcome message (sync on player's thread)
        .syncAt(player, () -> {
            player.sendMessage("<gold>Welcome back, " + player.getName() + "!");
            player.sendMessage("<gray>Your data has been loaded.");
        })
        // Handle errors
        .exceptionally(ex -> {
            getLogger().severe("Failed to load data: " + ex.getMessage());
            player.sendMessage("<red>Failed to load your data!");
        })
        // Execute the chain
        .execute();
}
```
