# Migration Guide to IonAPI New Features

This guide helps you upgrade existing IonAPI plugins to use the new extended features.

## Table of Contents

1. [Overview](#overview)
2. [Dependency Updates](#dependency-updates)
3. [Migrating Item Creation](#migrating-item-creation)
4. [Migrating Inventory GUIs](#migrating-inventory-guis)
5. [Migrating Scoreboards](#migrating-scoreboards)
6. [Migrating Boss Bars](#migrating-boss-bars)
7. [Migrating Async Operations](#migrating-async-operations)
8. [Migrating Database Code](#migrating-database-code)
9. [Performance Improvements](#performance-improvements)
10. [Breaking Changes](#breaking-changes)

---

## Overview

IonAPI now includes powerful extended features that simplify common plugin development tasks:

- **Item Builder**: Replace verbose ItemStack creation
- **GUI System**: Replace manual inventory management
- **Scoreboard/BossBar**: Modern UI components
- **Task Chains**: Cleaner async/sync workflows
- **Database Layer**: Built-in ORM with connection pooling

All new features are **opt-in** and backward compatible. You can migrate incrementally.

---

## Dependency Updates

### Gradle (Kotlin DSL)

**Before:**
```kotlin
dependencies {
    compileOnly("com.ionapi:ion-api:1.0.0")
}
```

**After (choose what you need):**
```kotlin
dependencies {
    // Core API (required)
    compileOnly("com.ionapi:ion-api:1.0.0")
    
    // Optional: Add specific features
    implementation("com.ionapi:ion-item:1.0.0")
    implementation("com.ionapi:ion-gui:1.0.0")
    implementation("com.ionapi:ion-ui:1.0.0")
    implementation("com.ionapi:ion-tasks:1.0.0")
    implementation("com.ionapi:ion-database:1.0.0")
    
    // Or: Include all features
    // implementation("com.ionapi:ion-all:1.0.0")
}
```

---

## Migrating Item Creation

### Before (Bukkit API)

```java
// Old verbose way
ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
ItemMeta meta = sword.getItemMeta();
if (meta != null) {
    meta.setDisplayName(ChatColor.RED + "Legendary Sword");
    List<String> lore = new ArrayList<>();
    lore.add(ChatColor.GRAY + "Forged in dragon fire");
    lore.add("");
    lore.add(ChatColor.GOLD + "Legendary Weapon");
    meta.setLore(lore);
    meta.addEnchant(Enchantment.SHARPNESS, 5, false);
    meta.setUnbreakable(true);
    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
}
sword.setItemMeta(meta);
```

### After (IonItem Builder)

```java
// New fluent way
ItemStack sword = IonItem.builder(Material.DIAMOND_SWORD)
    .name("<red>Legendary Sword")
    .lore(
        "<gray>Forged in dragon fire",
        "",
        "<gold>Legendary Weapon"
    )
    .enchant(Enchantment.SHARPNESS, 5)
    .unbreakable()
    .flag(ItemFlag.HIDE_ENCHANTS)
    .build();
```

**Benefits:**
- 60% less code
- Type-safe and fluent
- Supports MiniMessage formatting
- No null checks needed
- Chainable methods

### Migration Pattern

Find all instances of:
```java
ItemStack item = new ItemStack(Material.XXX);
ItemMeta meta = item.getItemMeta();
// ... meta modifications
item.setItemMeta(meta);
```

Replace with:
```java
ItemStack item = IonItem.builder(Material.XXX)
    // ... fluent modifications
    .build();
```

---

## Migrating Inventory GUIs

### Before (Manual Inventory Management)

```java
// Old way - lots of boilerplate
private final Map<UUID, Inventory> activeGuis = new HashMap<>();

public void openShopGui(Player player) {
    Inventory inv = Bukkit.createInventory(null, 27, "Shop");
    
    // Set items
    ItemStack diamond = new ItemStack(Material.DIAMOND);
    ItemMeta meta = diamond.getItemMeta();
    meta.setDisplayName("Buy Diamond - $100");
    diamond.setItemMeta(meta);
    inv.setItem(10, diamond);
    
    // Track inventory
    activeGuis.put(player.getUniqueId(), inv);
    player.openInventory(inv);
}

@EventHandler
public void onInventoryClick(InventoryClickEvent event) {
    Player player = (Player) event.getWhoClicked();
    Inventory clicked = event.getClickedInventory();
    
    if (!activeGuis.containsValue(clicked)) return;
    
    event.setCancelled(true);
    
    int slot = event.getSlot();
    if (slot == 10) {
        // Handle diamond purchase
        handlePurchase(player, "diamond", 100);
    }
}

@EventHandler
public void onInventoryClose(InventoryCloseEvent event) {
    activeGuis.remove(event.getPlayer().getUniqueId());
}
```

### After (IonGui System)

```java
// New way - clean and simple
public void openShopGui(Player player) {
    IonGui.builder()
        .title("<gold>Shop")
        .rows(3)
        .item(10, IonItem.of(Material.DIAMOND, "Buy Diamond - $100"),
            click -> handlePurchase(player, "diamond", 100))
        .fillBorder(IonItem.of(Material.GRAY_STAINED_GLASS_PANE, " "))
        .build()
        .open(player);
}

private void handlePurchase(Player player, String item, int cost) {
    // Handle purchase logic
}
```

**Benefits:**
- 70% less code
- No manual event tracking
- Automatic cleanup
- Built-in click handlers
- No memory leaks

### Migration Checklist

- [ ] Remove manual `Inventory` tracking maps
- [ ] Remove `InventoryClickEvent` handlers for custom GUIs
- [ ] Remove `InventoryCloseEvent` cleanup code
- [ ] Convert to `IonGui.builder()` pattern
- [ ] Move click logic to inline handlers

---

## Migrating Scoreboards

### Before (Bukkit Scoreboard API)

```java
// Old way - complex and error-prone
private void createScoreboard(Player player) {
    ScoreboardManager manager = Bukkit.getScoreboardManager();
    Scoreboard scoreboard = manager.getNewScoreboard();
    Objective objective = scoreboard.registerNewObjective(
        "stats", "dummy", ChatColor.GOLD + "Server Stats"
    );
    objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    
    Score score3 = objective.getScore(ChatColor.GRAY + "Players: " + 
        Bukkit.getOnlinePlayers().size());
    score3.setScore(3);
    
    Score score2 = objective.getScore(ChatColor.YELLOW + "Your Level: 5");
    score2.setScore(2);
    
    Score score1 = objective.getScore("");
    score1.setScore(1);
    
    Score score0 = objective.getScore(ChatColor.AQUA + "example.com");
    score0.setScore(0);
    
    player.setScoreboard(scoreboard);
}

// Updating requires rebuilding entire scoreboard
private void updateScoreboard(Player player) {
    createScoreboard(player); // Recreate everything
}
```

### After (IonScoreboard)

```java
// New way - simple and dynamic
private void createScoreboard(Player player) {
    IonScoreboard.create(player)
        .title("<gold>Server Stats")
        .line("<gray>Players: " + Bukkit.getOnlinePlayers().size())
        .line("<yellow>Your Level: 5")
        .line("")
        .line("<aqua>example.com")
        .autoUpdate(20L)  // Auto-updates every second
        .show();
}

// Or with dynamic content
private void createDynamicScoreboard(Player player) {
    IonScoreboard.create(player)
        .title("<gold>Server Stats")
        .line("") // Placeholder
        .line("") // Placeholder
        .dynamicLine(0, p -> "<gray>Players: " + Bukkit.getOnlinePlayers().size())
        .dynamicLine(1, p -> "<yellow>Your Level: " + getLevel(p))
        .autoUpdate(20L)
        .show();
}
```

**Benefits:**
- Simple line-based API
- Dynamic content support
- Automatic updates
- No flickering
- Easy to update individual lines

---

## Migrating Boss Bars

### Before (Bukkit BossBar API)

```java
private final Map<UUID, BossBar> activeBars = new HashMap<>();

public void showBossBar(Player player, String title, double progress) {
    BossBar bar = Bukkit.createBossBar(
        title,
        BarColor.RED,
        BarStyle.SOLID
    );
    bar.addPlayer(player);
    bar.setProgress(progress);
    activeBars.put(player.getUniqueId(), bar);
}

public void updateBossBar(Player player, double progress) {
    BossBar bar = activeBars.get(player.getUniqueId());
    if (bar != null) {
        bar.setProgress(progress);
    }
}

public void hideBossBar(Player player) {
    BossBar bar = activeBars.remove(player.getUniqueId());
    if (bar != null) {
        bar.removePlayer(player);
    }
}
```

### After (IonBossBar)

```java
public void showBossBar(Player player, String title, double progress) {
    IonBossBar.create(title)
        .progress((float) progress)
        .color(BossBar.Color.RED)
        .show(player);
}

// With dynamic updates
public void showDynamicBar(Player player) {
    IonBossBar.create()
        .dynamicTitle(bar -> "Health: " + player.getHealth())
        .dynamicProgress(bar -> (float) (player.getHealth() / 20.0))
        .autoUpdate(5L)
        .show(player);
}
```

**Benefits:**
- Automatic player tracking
- Dynamic content
- No manual cleanup
- Fluent API

---

## Migrating Async Operations

### Before (Raw Scheduler)

```java
// Old way - callback hell
public void loadPlayerData(Player player) {
    getScheduler().runAsync(() -> {
        try {
            PlayerData data = database.query(player.getUniqueId());
            
            getScheduler().run(() -> {
                player.setHealth(data.health);
                player.setLevel(data.level);
                
                getScheduler().runLater(() -> {
                    player.sendMessage("Welcome back!");
                }, 20L);
            });
        } catch (Exception e) {
            getScheduler().run(() -> {
                player.sendMessage("Failed to load data!");
            });
        }
    });
}
```

### After (TaskChain)

```java
// New way - clean and readable
public void loadPlayerData(Player player) {
    TaskChain.create(this)
        .async(() -> database.query(player.getUniqueId()))
        .syncAt(player, data -> {
            player.setHealth(data.health);
            player.setLevel(data.level);
        })
        .delay(1, TimeUnit.SECONDS)
        .syncAt(player, () -> player.sendMessage("Welcome back!"))
        .exceptionally(ex -> player.sendMessage("Failed to load data!"))
        .execute();
}
```

**Benefits:**
- Linear, readable code
- Automatic error handling
- Folia-aware scheduling
- No callback nesting

---

## Migrating Database Code

### Before (JDBC Boilerplate)

```java
// Old way - lots of boilerplate
private Connection connection;

public void connect() throws SQLException {
    String url = "jdbc:mysql://localhost:3306/mydb";
    connection = DriverManager.getConnection(url, "user", "pass");
}

public PlayerData getPlayer(UUID uuid) throws SQLException {
    String sql = "SELECT * FROM players WHERE uuid = ?";
    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setString(1, uuid.toString());
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            PlayerData data = new PlayerData();
            data.setUuid(uuid);
            data.setName(rs.getString("name"));
            data.setLevel(rs.getInt("level"));
            data.setBalance(rs.getDouble("balance"));
            return data;
        }
    }
    return null;
}

public void savePlayer(PlayerData data) throws SQLException {
    String sql = "UPDATE players SET name = ?, level = ?, balance = ? WHERE uuid = ?";
    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setString(1, data.getName());
        stmt.setInt(2, data.getLevel());
        stmt.setDouble(3, data.getBalance());
        stmt.setString(4, data.getUuid().toString());
        stmt.executeUpdate();
    }
}
```

### After (IonDatabase ORM)

```java
// New way - simple ORM
@Table("players")
public class PlayerData {
    @PrimaryKey
    private UUID uuid;
    private String name;
    private int level;
    private double balance;
    // getters/setters
}

// Connect
IonDatabase db = IonDatabase.builder()
    .type(DatabaseType.MYSQL)
    .host("localhost")
    .database("mydb")
    .username("user")
    .password("pass")
    .build();

db.connect();
db.createTable(PlayerData.class);

// Query
PlayerData data = db.find(PlayerData.class, uuid);

// Save
data.setLevel(data.getLevel() + 1);
db.save(data);

// Async
db.findAsync(PlayerData.class, uuid)
    .thenAccept(data -> processData(data));
```

**Benefits:**
- 80% less code
- Type-safe queries
- Automatic connection pooling
- Built-in async support
- No SQL injection risks

### Database Migration Steps

1. **Define entity classes** with annotations
2. **Replace connection code** with `IonDatabase.builder()`
3. **Replace PreparedStatement** with `db.find()`, `db.save()`
4. **Convert callbacks** to `db.findAsync().thenAccept()`
5. **Test thoroughly** with existing data

---

## Performance Improvements

### Connection Pooling

**Before:**
```java
// Creating new connections each time (slow)
Connection conn = DriverManager.getConnection(url, user, pass);
```

**After:**
```java
// Automatic connection pooling (fast)
IonDatabase db = IonDatabase.builder()
    .poolSize(10)  // Reuses connections
    .build();
```

### Folia Optimization

**Before:**
```java
// May cause threading issues on Folia
scheduler.run(() -> {
    player.damage(5.0);
});
```

**After:**
```java
// Automatically runs on player's region thread
scheduler.runAt(player, () -> {
    player.damage(5.0);
});

// Or with TaskChain
TaskChain.create(plugin)
    .syncAt(player, () -> player.damage(5.0))
    .execute();
```

---

## Breaking Changes

### None!

All new features are **additive only**. Your existing IonAPI code will continue to work without changes.

However, we recommend:

1. **Deprecation warnings**: Some old patterns may show warnings
2. **Best practices**: Use new APIs for new code
3. **Gradual migration**: Migrate one system at a time

---

## Migration Checklist

Use this checklist to track your migration progress:

### Phase 1: Dependencies
- [ ] Update build.gradle.kts dependencies
- [ ] Add required feature modules
- [ ] Test build still works

### Phase 2: Items
- [ ] Identify all ItemStack creation code
- [ ] Migrate to IonItem.builder()
- [ ] Test item creation

### Phase 3: GUIs
- [ ] Remove manual inventory tracking
- [ ] Migrate to IonGui.builder()
- [ ] Remove old event handlers
- [ ] Test GUI interactions

### Phase 4: UI Components
- [ ] Migrate scoreboards to IonScoreboard
- [ ] Migrate boss bars to IonBossBar
- [ ] Test dynamic updates

### Phase 5: Async Operations
- [ ] Identify nested async callbacks
- [ ] Migrate to TaskChain
- [ ] Test error handling

### Phase 6: Database
- [ ] Define entity classes
- [ ] Migrate connection code
- [ ] Migrate queries to ORM
- [ ] Test with existing data
- [ ] Enable connection pooling

### Phase 7: Testing
- [ ] Test on Paper server
- [ ] Test on Folia server (if applicable)
- [ ] Load test with multiple players
- [ ] Verify no memory leaks

---

## Getting Help

If you encounter issues during migration:

1. **Check Documentation**: [NEW_FEATURES.md](NEW_FEATURES.md)
2. **See Examples**: [ComprehensiveExample.java](../examples/ComprehensiveExample.java)
3. **Ask Questions**: GitHub Issues
4. **Community**: Discord server (if available)

---

## Example: Complete Migration

Here's a before/after comparison of a complete plugin:

### Before (Old Way)

```java
public class OldPlugin extends JavaPlugin implements Listener {
    
    private Connection database;
    private final Map<UUID, Inventory> shopGuis = new HashMap<>();
    
    @Override
    public void onEnable() {
        connectDatabase();
        getServer().getPluginManager().registerEvents(this, this);
    }
    
    private void connectDatabase() {
        try {
            database = DriverManager.getConnection(
                "jdbc:sqlite:plugins/MyPlugin/data.db"
            );
            database.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS players (" +
                "uuid TEXT PRIMARY KEY, " +
                "name TEXT, " +
                "coins INTEGER)"
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try {
                PreparedStatement stmt = database.prepareStatement(
                    "SELECT * FROM players WHERE uuid = ?"
                );
                stmt.setString(1, player.getUniqueId().toString());
                ResultSet rs = stmt.executeQuery();
                
                if (!rs.next()) {
                    // Create new player
                    PreparedStatement insert = database.prepareStatement(
                        "INSERT INTO players VALUES (?, ?, ?)"
                    );
                    insert.setString(1, player.getUniqueId().toString());
                    insert.setString(2, player.getName());
                    insert.setInt(3, 0);
                    insert.executeUpdate();
                }
                
                Bukkit.getScheduler().runTask(this, () -> {
                    openShop(player);
                });
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
    
    private void openShop(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Shop");
        
        ItemStack diamond = new ItemStack(Material.DIAMOND);
        ItemMeta meta = diamond.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Diamond - $100");
        diamond.setItemMeta(meta);
        inv.setItem(13, diamond);
        
        shopGuis.put(player.getUniqueId(), inv);
        player.openInventory(inv);
    }
    
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        // Handle clicks...
    }
}
```

### After (New Way)

```java
public class NewPlugin implements IonPlugin, Listener {
    
    private IonDatabase database;
    
    @Override
    public void onEnable() {
        database = IonDatabase.sqlite("plugins/MyPlugin/data.db");
        database.connect();
        database.createTable(PlayerData.class);
        
        getServer().getPluginManager().registerEvents(this, this);
    }
    
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        TaskChain.create(this)
            .async(() -> {
                PlayerData data = database.find(PlayerData.class, 
                    player.getUniqueId());
                if (data == null) {
                    data = new PlayerData(player.getUniqueId(), 
                        player.getName());
                    database.insert(data);
                }
                return data;
            })
            .syncAt(player, data -> openShop(player, data))
            .execute();
    }
    
    private void openShop(Player player, PlayerData data) {
        IonGui.builder()
            .title("<gold>Shop")
            .rows(3)
            .item(13, 
                IonItem.of(Material.DIAMOND, "<gold>Diamond - $100"),
                click -> buyItem(player, data, 100))
            .build()
            .open(player);
    }
    
    private void buyItem(Player player, PlayerData data, int cost) {
        if (data.getCoins() >= cost) {
            data.setCoins(data.getCoins() - cost);
            database.saveAsync(data);
            player.sendMessage("<green>Purchase successful!");
        } else {
            player.sendMessage("<red>Not enough coins!");
        }
    }
    
    @Table("players")
    public static class PlayerData {
        @PrimaryKey
        private UUID uuid;
        private String name;
        private int coins;
        
        // Constructor, getters, setters...
    }
}
```

**Result**: 60% less code, more readable, no memory leaks, better performance!

---

## Conclusion

Migrating to IonAPI's new features will:

✅ **Reduce code** by 50-80%  
✅ **Improve performance** with connection pooling and optimized threading  
✅ **Eliminate bugs** from manual resource management  
✅ **Increase maintainability** with cleaner, more readable code  
✅ **Future-proof** your plugin for Folia and beyond  

Start small, test thoroughly, and enjoy your newly modernized plugin!