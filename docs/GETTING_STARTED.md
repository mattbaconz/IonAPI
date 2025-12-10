# Getting Started with IonAPI

A comprehensive guide to building Minecraft plugins with IonAPI.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Installation](#installation)
3. [Your First Plugin](#your-first-plugin)
4. [Core Features](#core-features)
5. [Extended Features](#extended-features)
6. [Common Patterns](#common-patterns)
7. [Best Practices](#best-practices)
8. [Next Steps](#next-steps)


### Step 1: Add IonAPI to Your Project

**Gradle (Kotlin DSL):**
```kotlin
repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    // All-in-one (recommended)
    implementation("com.github.mattbaconz:IonAPI:1.3.0")
    
    // OR individual modules:
    // implementation("com.ionapi:ion-api:1.2.6")
    // implementation("com.ionapi:ion-database:1.2.6")
    // implementation("com.ionapi:ion-economy:1.2.6")
}
```

**Gradle (Groovy):**
```groovy
repositories {
    mavenCentral()
    maven { url 'https://repo.papermc.io/repository/maven-public/' }
}

dependencies {
    implementation 'com.github.mattbaconz:IonAPI:1.2.6'
}
```

**Maven:**
```xml
<dependencies>
    <dependency>
        <groupId>com.github.mattbaconz</groupId>
        <artifactId>IonAPI</artifactId>
        <version>1.3.0</version>
    </dependency>
</dependencies>
```

### Step 2: Create Your Plugin Class

```java
package com.example.myplugin;

import com.ionapi.api.IonPlugin;
import org.jetbrains.annotations.NotNull;

public class MyPlugin implements IonPlugin {
    
    @Override
    public void onEnable() {
        getLogger().info("MyPlugin has been enabled!");
        
        // Initialize your plugin
        loadConfig();
        registerCommands();
        registerEvents();
    }
    
    @Override
    public void onDisable() {
        getLogger().info("MyPlugin has been disabled!");
        
        // Cleanup resources
        getScheduler().cancelAll();
        getConfigProvider().saveAll();
    }
    
    @Override
    public @NotNull String getName() {
        return "MyPlugin";
    }
    
    private void loadConfig() {
        // Config loading code
    }
    
    private void registerCommands() {
        // Command registration code
    }
    
    private void registerEvents() {
        // Event registration code
    }
}
```

---

## Your First Plugin

Let's create a simple plugin with a command, configuration, and scheduled task.

### Step 1: Create a Hello Command

```java
package com.example.myplugin.commands;

import com.ionapi.api.command.CommandContext;
import com.ionapi.api.command.IonCommand;
import org.jetbrains.annotations.NotNull;

public class HelloCommand implements IonCommand {
    
    @Override
    public boolean execute(@NotNull CommandContext ctx) {
        // Get optional argument with default value
        String name = ctx.getArg(0, "World");
        
        // Send colored message using MiniMessage format
        ctx.reply("<green>Hello, <bold>" + name + "</bold>!");
        
        return true;
    }
    
    @Override
    public @NotNull String getName() {
        return "hello";
    }
    
    @Override
    public @NotNull String getDescription() {
        return "Sends a greeting message";
    }
    
    @Override
    public @NotNull String getUsage() {
        return "/hello [name]";
    }
    
    @Override
    public @NotNull String getPermission() {
        return "myplugin.hello";
    }
}
```

### Step 2: Register the Command

```java
private void registerCommands() {
    getCommandRegistry().register(new HelloCommand());
}
```

### Step 3: Test Your Plugin

1. Build your plugin: `./gradlew build`
2. Copy JAR to server's `plugins/` folder
3. Start server
4. Run `/hello` or `/hello Steve`

---

## Core Features

### Scheduler (Thread-Safe, Folia-Compatible)

The IonAPI scheduler provides a unified interface that works on both Paper and Folia.

#### Synchronous Tasks

```java
// Run immediately on main thread
getScheduler().run(() -> {
    player.sendMessage("<green>This runs on main thread!");
});

// Run after delay
getScheduler().runLater(() -> {
    player.sendMessage("<green>5 seconds have passed!");
}, 5, TimeUnit.SECONDS);

// Run repeatedly
IonTask task = getScheduler().runTimer(() -> {
    player.sendMessage("<green>This runs every second!");
}, 0, 1, TimeUnit.SECONDS);

// Cancel task later
task.cancel();
```

#### Asynchronous Tasks

```java
// Run async (for database, API calls, file I/O)
getScheduler().runAsync(() -> {
    // This runs off the main thread
    String data = fetchFromDatabase();
    
    // Switch back to main thread for Bukkit API
    getScheduler().run(() -> {
        player.sendMessage("<green>Data: " + data);
    });
});
```

#### Folia-Aware Scheduling

For optimal performance on Folia, use context-aware scheduling:

```java
// Entity-specific tasks (runs on entity's region thread)
Player player = event.getPlayer();
getScheduler().runAt(player, () -> {
    player.damage(5.0);
    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1));
});

// Location-specific tasks (runs on location's region thread)
Location spawn = world.getSpawnLocation();
getScheduler().runAt(spawn, () -> {
    world.spawnParticle(Particle.FLAME, spawn, 10);
    world.setBlockData(spawn, Material.DIAMOND_BLOCK.createBlockData());
});

// Delayed context-aware tasks
getScheduler().runAtLater(player, () -> {
    player.sendMessage("<yellow>5 seconds have passed!");
}, 5, TimeUnit.SECONDS);

// Repeating context-aware tasks
IonTask task = getScheduler().runAtTimer(player, () -> {
    player.sendActionBar("<gold>Score: " + getScore(player));
}, 0, 1, TimeUnit.SECONDS);
```

### Configuration

#### Create config.yml

Create a `config.yml` in your plugin's resources folder:

```yaml
# config.yml
prefix: "<gray>[<green>MyPlugin<gray>]"

messages:
  welcome: "<green>Welcome to the server!"
  goodbye: "<red>See you later!"

database:
  enabled: true
  host: "localhost"
  port: 3306
  name: "mydb"

features:
  - economy
  - shops
  - teleports
```

#### Load Configuration

```java
private void loadConfig() {
    IonConfig config = getConfigProvider().getConfig();
    
    // Read values
    String prefix = config.getString("prefix", "<gray>[<green>MyPlugin<gray>]");
    String welcome = config.getString("messages.welcome");
    boolean dbEnabled = config.getBoolean("database.enabled");
    
    if (dbEnabled) {
        String host = config.getString("database.host");
        int port = config.getInt("database.port");
        connectToDatabase(host, port);
    }
    
    // Read list
    List<String> features = config.getStringList("features");
    getLogger().info("Enabled features: " + String.join(", ", features));
}
```

#### Save Configuration

```java
// Modify values
config.set("last-updated", System.currentTimeMillis());
config.set("player-count", Bukkit.getOnlinePlayers().size());

// Save to disk
config.save();

// Reload from disk
config.reload();
```

### Custom Events

#### Define Your Event

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

#### Listen to Events

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

#### Fire Events

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

---

## Extended Features

### Item Builder

Replace verbose ItemStack creation with a fluent builder API.

**Before (Old Way):**
```java
ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
ItemMeta meta = sword.getItemMeta();
if (meta != null) {
    meta.setDisplayName(ChatColor.RED + "Legendary Sword");
    List<String> lore = new ArrayList<>();
    lore.add(ChatColor.GRAY + "Forged in dragon fire");
    meta.setLore(lore);
    meta.addEnchant(Enchantment.SHARPNESS, 5, false);
    meta.setUnbreakable(true);
}
sword.setItemMeta(meta);
```

**After (New Way):**
```java
import com.ionapi.item.IonItem;

ItemStack sword = IonItem.builder(Material.DIAMOND_SWORD)
    .name("<red>Legendary Sword")
    .lore("<gray>Forged in dragon fire")
    .enchant(Enchantment.SHARPNESS, 5)
    .unbreakable()
    .build();
```

**More Examples:**
```java
// Complex item with multiple features
ItemStack item = IonItem.builder(Material.DIAMOND_SWORD)
    .name("<gradient:red:blue><bold>Flame Reaver")
    .lore(
        "<gray>Forged in dragon fire",
        "",
        "<red>+ 15 Damage",
        "<gold>+ 10% Critical Strike"
    )
    .enchant(Enchantment.SHARPNESS, 5)
    .enchant(Enchantment.FIRE_ASPECT, 2)
    .unbreakable()
    .glow()
    .hideAll()
    .build();

// Quick creation
ItemStack simple = IonItem.of(Material.STICK, "<yellow>Magic Wand");
ItemStack withLore = IonItem.of(Material.BOOK, "Title", "Lore 1", "Lore 2");

// Modify existing item
ItemStack modified = IonItem.modify(existingItem, builder -> 
    builder.name("New Name").addLore("<gold>Enhanced!"));
```

### GUI System

Create interactive inventory menus without manual event handling.

```java
import com.ionapi.gui.IonGui;

public void openShop(Player player) {
    // Create shop items
    ItemStack diamond = IonItem.of(Material.DIAMOND, 
        "<aqua>Diamond", 
        "<gray>Price: <gold>$100");
    
    ItemStack gold = IonItem.of(Material.GOLD_INGOT,
        "<yellow>Gold Ingot",
        "<gray>Price: <gold>$50");
    
    // Create GUI
    IonGui.builder()
        .title("<gold><bold>Item Shop")
        .rows(3)
        .item(11, diamond, click -> {
            Player p = click.getPlayer();
            if (hasEnoughMoney(p, 100)) {
                takeMoney(p, 100);
                p.getInventory().addItem(new ItemStack(Material.DIAMOND));
                p.sendMessage("<green>Purchased diamond!");
                click.close();
            } else {
                p.sendMessage("<red>Not enough money!");
            }
        })
        .item(13, gold, click -> {
            Player p = click.getPlayer();
            if (hasEnoughMoney(p, 50)) {
                takeMoney(p, 50);
                p.getInventory().addItem(new ItemStack(Material.GOLD_INGOT));
                p.sendMessage("<green>Purchased gold!");
                click.close();
            } else {
                p.sendMessage("<red>Not enough money!");
            }
        })
        .fillBorderBuilder(IonItem.of(Material.GRAY_STAINED_GLASS_PANE, " "))
        .build()
        .open(player);
}
```

### Scoreboard

Create dynamic scoreboards with auto-updating content.

```java
import com.ionapi.ui.IonScoreboard;

public void showStatsBoard(Player player) {
    IonScoreboard board = IonScoreboard.create(player)
        .title("<gold><bold>Your Stats")
        .line("<gray>━━━━━━━━━━━━━━")
        .line("")  // Placeholder for dynamic line
        .line("")  // Placeholder for dynamic line
        .line("")  // Placeholder for dynamic line
        .line("<gray>━━━━━━━━━━━━━━")
        .dynamicLine(1, p -> "<yellow>Online: <white>" + Bukkit.getOnlinePlayers().size())
        .dynamicLine(2, p -> "<green>Health: <white>" + (int) p.getHealth() + "/20")
        .dynamicLine(3, p -> "<aqua>Level: <white>" + p.getLevel())
        .autoUpdate(20L)  // Update every second
        .show();
    
    // Store for cleanup later
    scoreboards.put(player.getUniqueId(), board);
}

// Update manually
board.updateLine(1, "<yellow>New text");
board.update();

// Cleanup
@Override
public void onDisable() {
    scoreboards.values().forEach(IonScoreboard::destroy);
    scoreboards.clear();
}
```

### BossBar

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

### Task Chains

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

---

## Common Patterns

### Pattern 1: Load Player Data on Join

```java
@EventHandler
public void onJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    
    TaskChain.create(plugin)
        .async(() -> database.loadPlayerData(player.getUniqueId()))
        .syncAt(player, data -> {
            if (data != null) {
                applyData(player, data);
                showWelcomeGUI(player);
            } else {
                createNewPlayer(player);
            }
        })
        .exceptionally(ex -> {
            player.sendMessage("<red>Failed to load data!");
            getLogger().severe("Error: " + ex.getMessage());
        })
        .execute();
}
```

### Pattern 2: Shop GUI with Purchase Logic

```java
public void openShop(Player player) {
    IonGui.builder()
        .title("<gold>Item Shop")
        .rows(3)
        .item(11, createShopItem(Material.DIAMOND, "Diamond", 100),
            click -> purchaseItem(click.getPlayer(), Material.DIAMOND, 100))
        .item(13, createShopItem(Material.GOLD_INGOT, "Gold", 50),
            click -> purchaseItem(click.getPlayer(), Material.GOLD_INGOT, 50))
        .item(15, createShopItem(Material.IRON_INGOT, "Iron", 25),
            click -> purchaseItem(click.getPlayer(), Material.IRON_INGOT, 25))
        .fillBorderBuilder(IonItem.of(Material.GRAY_STAINED_GLASS_PANE, " "))
        .build()
        .open(player);
}

private ItemStack createShopItem(Material material, String name, int price) {
    return IonItem.builder(material)
        .name("<yellow>" + name)
        .lore("<gray>Price: <gold>$" + price)
        .build();
}

private void purchaseItem(Player player, Material item, int cost) {
    TaskChain.create(plugin)
        .async(() -> economy.getBalance(player.getUniqueId()))
        .syncAt(player, balance -> {
            if (balance >= cost) {
                economy.withdraw(player.getUniqueId(), cost);
                player.getInventory().addItem(new ItemStack(item));
                player.sendMessage("<green>Purchase successful!");
            } else {
                player.sendMessage("<red>Not enough money!");
            }
        })
        .execute();
}
```

### Pattern 3: Dynamic Stats Scoreboard

```java
private final Map<UUID, IonScoreboard> scoreboards = new HashMap<>();

public void showStatsBoard(Player player) {
    IonScoreboard board = IonScoreboard.create(player)
        .title("<gold><bold>Your Stats")
        .line("<gray>━━━━━━━━━━━━━━")
        .line("")
        .line("")
        .line("")
        .line("<gray>━━━━━━━━━━━━━━")
        .dynamicLine(1, p -> "<yellow>Level: <white>" + getLevel(p))
        .dynamicLine(2, p -> "<green>Coins: <white>" + getCoins(p))
        .dynamicLine(3, p -> "<aqua>Rank: <white>" + getRank(p))
        .autoUpdate(20L)
        .show();
    
    scoreboards.put(player.getUniqueId(), board);
}

@Override
public void onDisable() {
    scoreboards.values().forEach(IonScoreboard::destroy);
    scoreboards.clear();
}
```

---

## Best Practices

### 1. Thread Safety

```java
// ✅ GOOD - Async for I/O operations
TaskChain.create(plugin)
    .async(() -> loadFromFile())
    .syncAt(player, data -> applyData(data))
    .execute();

// ❌ BAD - I/O on main thread
Data data = loadFromFile();  // Blocks server!
applyData(player, data);
```

### 2. Resource Cleanup

```java
@Override
public void onDisable() {
    // Cancel all scheduled tasks
    getScheduler().cancelAll();
    
    // Clean up GUIs
    openGuis.forEach(IonGui::destroy);
    
    // Clean up scoreboards
    scoreboards.values().forEach(IonScoreboard::destroy);
    
    // Hide boss bars
    bossBars.values().forEach(IonBossBar::hideAll);
    
    // Save configs
    getConfigProvider().saveAll();
    
    getLogger().info("Resources cleaned up!");
}
```

### 3. Error Handling

```java
// ✅ GOOD - Proper error handling
TaskChain.create(plugin)
    .async(() -> riskyOperation())
    .exceptionally(ex -> {
        getLogger().severe("Error: " + ex.getMessage());
        notifyPlayer("Operation failed!");
    })
    .execute();

// ❌ BAD - No error handling
TaskChain.create(plugin)
    .async(() -> riskyOperation())
    .execute();  // Silent failures!
```

### 4. Folia Compatibility

```java
// ✅ GOOD - Folia-safe
getScheduler().runAt(player, () -> {
    player.damage(5.0);
});

// ❌ BAD - May crash on Folia
getScheduler().run(() -> {
    player.damage(5.0);
});
```

### 5. Cache Frequently Used Objects

```java
// ✅ GOOD - Cache and reuse
Map<UUID, IonScoreboard> boards = new HashMap<>();

public IonScoreboard getBoard(Player player) {
    return boards.computeIfAbsent(player.getUniqueId(), 
        uuid -> IonScoreboard.create(player).show());
}

// ❌ BAD - Create new every time
public void updateBoard(Player player) {
    IonScoreboard.create(player).show();  // Memory leak!
}
```

---

## Next Steps

### Learn More

- **[API Reference](API_REFERENCE.md)** - Complete API documentation
- **[Quick Reference](QUICK_REFERENCE.md)** - Quick cheatsheet
- **[Examples](EXAMPLES.md)** - Real-world code examples
- **[Folia Guide](FOLIA_GUIDE.md)** - Folia compatibility guide
- **[Javadoc Guide](JAVADOC_GUIDE.md)** - Generate and view Javadocs
- **[Shading Guide](SHADING.md)** - Shading and relocation guide
- **[Migration Guide](MIGRATION_GUIDE.md)** - Migrate from old code

### Join the Community

- **GitHub Issues** - Report bugs or request features
- **GitHub Discussions** - Ask questions and share ideas
- **Discord** - Chat with other developers (if available)

### Contribute

- Read [Contributing Guide](../CONTRIBUTING.md)
- Check out [open issues](https://github.com/YourOrg/IonAPI/issues)
- Submit pull requests

---

## Troubleshooting

### GUI not opening?
- Make sure you called `.build()` before `.open()`
- Check that the player is online
- Verify the GUI has at least 1 row

### Scoreboard not updating?
- Did you call `.show()` after building?
- Is `.autoUpdate()` enabled with an interval?
- Check that dynamic suppliers don't throw exceptions

### Task chain not executing?
- Make sure you call `.execute()` at the end
- Check logs for exceptions
- Verify the plugin is enabled

### Items not showing custom name?
- Use MiniMessage format: `<red>Name` not `§cName`
- Make sure you called `.build()` on the item builder
- Check that the material is valid

---

**Happy coding with IonAPI!** 🚀

---

## 💬 Community & Support

<div align="center">

[![Discord](https://img.shields.io/badge/Discord-Join%20Server-5865F2?style=for-the-badge&logo=discord&logoColor=white)](https://discord.com/invite/VQjTVKjs46)
[![GitHub](https://img.shields.io/badge/GitHub-mattbaconz-181717?style=for-the-badge&logo=github)](https://github.com/mattbaconz)

**Need help?** Join our Discord server!

**Support the project:**
- ☕ [Ko-fi](https://ko-fi.com/mbczishim/tip)
- 💰 [PayPal](https://www.paypal.com/paypalme/MatthewWatuna)

</div>
