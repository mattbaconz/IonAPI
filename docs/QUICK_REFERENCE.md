# IonAPI Quick Reference

A quick cheatsheet for all IonAPI features.

## Item Builder

```java
// Basic item
ItemStack item = IonItem.builder(Material.DIAMOND_SWORD)
    .name("<gradient:red:blue>Legendary Sword")
    .lore("Line 1", "Line 2", "Line 3")
    .amount(1)
    .build();

// With enchantments
ItemStack enchanted = IonItem.builder(Material.DIAMOND_SWORD)
    .enchant(Enchantment.SHARPNESS, 5)
    .enchantUnsafe(Enchantment.MENDING, 1)
    .unbreakable()
    .glow()
    .hideAll()
    .build();

// Quick creation
ItemStack simple = IonItem.of(Material.STICK, "<yellow>Magic Wand");
ItemStack withLore = IonItem.of(Material.BOOK, "Title", "Lore 1", "Lore 2");

// Modify existing
ItemStack modified = IonItem.modify(existingItem, b -> b.name("New Name").glow());
```

## GUI System

```java
// Create GUI
IonGui gui = IonGui.builder()
    .title("<gold>Shop")
    .rows(3)
    .item(10, diamondItem, click -> {
        click.getPlayer().sendMessage("Clicked!");
        click.close();
    })
    .fillBorderBuilder(borderItem)
    .onOpenHandler(event -> player.sendMessage("Opened!"))
    .onCloseHandler(event -> player.sendMessage("Closed!"))
    .onClickHandler(event -> {
        // Global click handler
    })
    .build();

// Open GUI
gui.open(player);

// Update after creation
gui.setItem(10, newItem);
gui.fill(fillerItem);
gui.fillBorder(borderItem);
gui.update();  // Refresh display

// Clean up
gui.destroy();
```

## Scoreboard

```java
// Basic scoreboard
IonScoreboard board = IonScoreboard.create(player)
    .title("<gold>Server Stats")
    .line("<gray>Players: 10")
    .line("<yellow>Level: 5")
    .line("")
    .line("<aqua>example.com")
    .show();

// Dynamic scoreboard
IonScoreboard dynamic = IonScoreboard.create(player)
    .title("<gold>Stats")
    .line("") // Placeholder
    .line("") // Placeholder
    .dynamicLine(0, p -> "<gray>Health: " + p.getHealth())
    .dynamicLine(1, p -> "<yellow>Level: " + p.getLevel())
    .autoUpdate(20L)  // Update every second
    .show();

// Update
board.updateLine(0, "<gray>Players: 15");
board.update();  // Update all dynamic content
board.hide();
board.destroy();
```

## BossBar

```java
// Basic boss bar
IonBossBar bar = IonBossBar.create()
    .title("<red>Boss Health")
    .progress(0.75f)
    .color(BossBar.Color.RED)
    .overlay(BossBar.Overlay.PROGRESS)
    .show(player);

// Dynamic boss bar
IonBossBar dynamic = IonBossBar.create()
    .dynamicTitle(b -> "Health: " + boss.getHealth())
    .dynamicProgress(b -> (float) (boss.getHealth() / boss.getMaxHealth()))
    .autoUpdate(5L)
    .show(player);

// Update
bar.progress(0.5f);
bar.progressPercent(50);
bar.hide(player);
bar.destroy();

// Colors: PINK, BLUE, RED, GREEN, YELLOW, PURPLE, WHITE
// Overlays: PROGRESS, NOTCHED_6, NOTCHED_10, NOTCHED_12, NOTCHED_20
```

## Task Chains

```java
// Basic chain
TaskChain.create(plugin)
    .async(() -> fetchFromDatabase(uuid))
    .syncAt(player, data -> applyData(player, data))
    .delay(1, TimeUnit.SECONDS)
    .sync(() -> broadcastMessage())
    .exceptionally(ex -> handleError(ex))
    .execute();

// With transformations
TaskChain.create(plugin)
    .async(() -> fetchData())           // Returns Data
    .syncAt(player, data -> {           // Receives Data
        player.sendMessage("Loaded!");
        return data;
    })
    .async(data -> saveData(data))      // Receives Data
    .execute();

// Error handling
TaskChain.create(plugin)
    .async(() -> riskyOperation())
    .exceptionally(ex -> {
        plugin.getLogger().severe("Error: " + ex);
    })
    .recover(ex -> fallbackValue)
    .finallyDo(() -> cleanup())
    .execute();

// Conditional
TaskChain.create(plugin)
    .syncIf(() -> player.hasPermission("admin"), () -> {
        // Only runs if condition is true
    })
    .execute();
```

## Database

```java
// Connect
IonDatabase db = IonDatabase.builder()
    .type(DatabaseType.MYSQL)
    .host("localhost")
    .port(3306)
    .database("mydb")
    .username("user")
    .password("pass")
    .poolSize(10)
    .build();

db.connect();

// Define entity
@Table("players")
public class PlayerData {
    @PrimaryKey
    private UUID uuid;
    
    @Column(nullable = false)
    private String name;
    
    @Column(defaultValue = "0")
    private int level;
    
    // Constructors, getters, setters
}

// Create table
db.createTable(PlayerData.class);

// CRUD operations
PlayerData data = db.find(PlayerData.class, uuid);
data.setLevel(5);
db.save(data);
db.delete(data);

// Async operations
db.findAsync(PlayerData.class, uuid)
    .thenAccept(data -> processData(data));

db.saveAsync(data)
    .thenRun(() -> plugin.getLogger().info("Saved!"));

// Transactions
db.transaction(database -> {
    PlayerData p1 = database.find(PlayerData.class, uuid1);
    PlayerData p2 = database.find(PlayerData.class, uuid2);
    p1.setLevel(p1.getLevel() + 1);
    p2.setLevel(p2.getLevel() + 1);
    database.save(p1);
    database.save(p2);
});

// Raw SQL
db.execute("UPDATE players SET level = level + 1 WHERE uuid = ?", uuid);
ResultSet rs = db.query("SELECT * FROM players WHERE level > ?", 10);

// Cleanup
db.disconnect();
```

## Common Patterns

### Load Player Data
```java
@EventHandler
public void onJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    
    TaskChain.create(plugin)
        .async(() -> db.find(PlayerData.class, player.getUniqueId()))
        .syncAt(player, data -> {
            if (data != null) {
                applyData(player, data);
            }
        })
        .exceptionally(ex -> {
            player.sendMessage("§cFailed to load data!");
        })
        .execute();
}
```

### Save Player Data
```java
@EventHandler
public void onQuit(PlayerQuitEvent event) {
    Player player = event.getPlayer();
    PlayerData data = getPlayerData(player);
    
    db.saveAsync(data)
        .thenRun(() -> plugin.getLogger().info("Saved " + player.getName()));
}
```

### Shop GUI
```java
public void openShop(Player player) {
    IonGui.builder()
        .title("<gold>Shop")
        .rows(3)
        .item(10, IonItem.of(Material.DIAMOND, "§bDiamond - $100"), 
            click -> buyItem(player, Material.DIAMOND, 100))
        .item(12, IonItem.of(Material.GOLD_INGOT, "§eGold - $50"),
            click -> buyItem(player, Material.GOLD_INGOT, 50))
        .fillBorderBuilder(IonItem.of(Material.GRAY_STAINED_GLASS_PANE, " "))
        .build()
        .open(player);
}
```

### Dynamic Scoreboard
```java
IonScoreboard board = IonScoreboard.create(player)
    .title("<gold>Server")
    .line("")
    .line("")
    .line("")
    .dynamicLine(0, p -> "§ePlayers: §f" + Bukkit.getOnlinePlayers().size())
    .dynamicLine(1, p -> "§aCoins: §f" + getCoins(p))
    .dynamicLine(2, p -> "§bLevel: §f" + getLevel(p))
    .autoUpdate(20L)
    .show();
```

### Progress BossBar
```java
void showProgress(Player player, int total, int current) {
    IonBossBar bar = IonBossBar.create("§6Processing...")
        .progress((float) current / total)
        .color(BossBar.Color.YELLOW)
        .show(player);
    
    // Update as needed
    bar.progress((float) (current + 1) / total);
    
    // Hide when done
    if (current >= total) {
        bar.hide(player);
    }
}
```

## Cross-Server Messaging (IonProxy)

```java
// Plugin messaging (Velocity/BungeeCord)
IonMessenger messenger = IonProxy.messenger(plugin);

// Subscribe to channels
messenger.subscribe("my:channel", (player, message) -> {
    getLogger().info("Received: " + message);
});

// Broadcast to all servers
messenger.broadcast("my:channel", "Hello!");

// Send to specific server
messenger.sendToServer("lobby", "my:channel", "Hello lobby!");

// Redis support (requires Jedis)
IonMessenger redis = IonProxy.redis(plugin, "localhost", 6379);
IonMessenger redisAuth = IonProxy.redis(plugin, "localhost", 6379, "password");

// Cleanup
messenger.close();
```

## Packet-Based NPCs (IonNPC)

```java
// Create NPC
IonNPC npc = IonNPC.builder(plugin)
    .location(spawnLocation)
    .name("<gold>Shop Keeper")
    .skin("Notch")                    // Fetch from Mojang
    .skin(texture, signature)         // Or use raw texture
    .lookAtPlayer(true)               // Auto look at nearby players
    .onClick(player -> openShop(player))
    .persistent(true)                 // Auto-show on join/range
    .viewDistance(48)
    .build();

// Show/hide
npc.show(player);
npc.showAll();
npc.hide(player);
npc.hideAll();

// Manipulation
npc.teleport(newLocation);
npc.lookAt(targetLocation);
npc.lookAt(targetPlayer);
npc.swingMainHand();
npc.swingOffHand();

// Cleanup
npc.destroy();
```

## Text Input GUI

```java
// Anvil-based text input
InputGui.create(plugin)
    .title("Search Item")
    .placeholder("Type here...")
    .minLength(2)
    .maxLength(32)
    .validator(text -> text.matches("[a-zA-Z]+"), "Letters only!")
    .onComplete((player, input) -> {
        player.sendMessage("You entered: " + input);
    })
    .onCancel(player -> {
        player.sendMessage("Cancelled!");
    })
    .open(player);
```

## Import Statements

```java
// Item Builder
import com.ionapi.item.IonItem;

// GUI System
import com.ionapi.gui.IonGui;
import com.ionapi.gui.InputGui;
import com.ionapi.gui.GuiClickEvent;

// UI Components
import com.ionapi.ui.IonScoreboard;
import com.ionapi.ui.IonBossBar;
import net.kyori.adventure.bossbar.BossBar;

// Task Chains
import com.ionapi.tasks.TaskChain;
import java.util.concurrent.TimeUnit;

// Database
import com.ionapi.database.*;
import com.ionapi.database.annotations.*;

// Cross-Server Messaging
import com.ionapi.proxy.IonProxy;
import com.ionapi.proxy.IonMessenger;

// NPCs
import com.ionapi.npc.IonNPC;
```

## Module Dependencies

```kotlin
dependencies {
    // Core (required)
    compileOnly("com.ionapi:ion-api:1.0.0")
    
    // Features (optional)
    implementation("com.ionapi:ion-item:1.0.0")
    implementation("com.ionapi:ion-gui:1.0.0")
    implementation("com.ionapi:ion-ui:1.0.0")
    implementation("com.ionapi:ion-tasks:1.0.0")
    implementation("com.ionapi:ion-database:1.0.0")
    implementation("com.ionapi:ion-proxy:1.0.0")
    implementation("com.ionapi:ion-npc:1.0.0")
}
```

## Tips

- Use `builder()` methods for construction, they return builder instances
- Use interface methods on built objects (e.g., `gui.fill()` after `build()`)
- Always call `destroy()` on GUIs, scoreboards, and boss bars in `onDisable()`
- Use `async()` for database/API calls, `sync()` for Minecraft operations
- Use `syncAt(entity)` or `syncAt(location)` for Folia compatibility
- Database entities need `@Table` annotation and no-arg constructor
- TaskChains automatically handle thread switching and error propagation

## Performance

- Cache scoreboards/boss bars per player, don't recreate
- Use connection pooling for databases (default: 10 connections)
- Batch database operations in transactions
- Use `autoUpdate()` sparingly, update only when needed
- Clean up resources in `onDisable()`

## Documentation

- [Getting Started](GETTING_STARTED.md)
- [API Reference](API_REFERENCE.md)
- [Examples](EXAMPLES.md)
- [Migration Guide](MIGRATION_GUIDE.md)
- [Folia Guide](FOLIA_GUIDE.md)

---

## 💬 Community & Support

[![Discord](https://img.shields.io/badge/Discord-Join%20Server-5865F2?style=flat-square&logo=discord&logoColor=white)](https://discord.com/invite/VQjTVKjs46)
[![GitHub](https://img.shields.io/badge/GitHub-mattbaconz-181717?style=flat-square&logo=github)](https://github.com/mattbaconz)

**Need help?** Join our [Discord](https://discord.com/invite/VQjTVKjs46)!

**Support:** [Ko-fi](https://ko-fi.com/mbczishim/tip) • [PayPal](https://www.paypal.com/paypalme/MatthewWatuna)
