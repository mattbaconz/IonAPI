# 💡 IonAPI Examples

Quick, practical examples to get you started fast.

---

## 🎨 Item Builder

### Basic Item
```java
ItemStack sword = IonItem.builder(Material.DIAMOND_SWORD)
    .name("<red>Legendary Sword")
    .lore("<gray>Forged in dragon fire")
    .enchant(Enchantment.SHARPNESS, 5)
    .unbreakable()
    .build();
```

### Complex Item
```java
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
```

---

## 📦 GUI System

### Shop GUI
```java
public void openShop(Player player) {
    IonGui.builder()
        .title("<gold><bold>✨ Shop")
        .rows(3)
        .item(11, IonItem.of(Material.DIAMOND, "<aqua>Diamond", "<gray>Price: $100"),
            click -> {
                if (buyItem(click.getPlayer(), 100)) {
                    click.getPlayer().sendMessage("<green>✓ Purchased!");
                    click.close();
                } else {
                    click.getPlayer().sendMessage("<red>✗ Not enough money!");
                }
            })
        .item(13, IonItem.of(Material.GOLD_INGOT, "<yellow>Gold", "<gray>Price: $50"),
            click -> buyItem(click.getPlayer(), 50))
        .fillBorderBuilder(IonItem.of(Material.GRAY_STAINED_GLASS_PANE, " "))
        .build()
        .open(player);
}
```

### Profile GUI
```java
public void openProfile(Player player) {
    ItemStack profile = IonItem.builder(Material.PLAYER_HEAD)
        .name("<gold><bold>" + player.getName())
        .lore(
            "<gray>Your Profile",
            "",
            "<yellow>Level: <white>" + getLevel(player),
            "<green>Coins: <white>" + getCoins(player)
        )
        .glow()
        .build();
    
    IonGui.builder()
        .title("<gold>Your Profile")
        .rows(3)
        .item(13, profile)
        .fillBorderBuilder(IonItem.of(Material.BLUE_STAINED_GLASS_PANE, " "))
        .build()
        .open(player);
}
```

---

## 📊 Scoreboard

### Dynamic Stats
```java
private final Map<UUID, IonScoreboard> scoreboards = new HashMap<>();

public void showStatsBoard(Player player) {
    IonScoreboard board = IonScoreboard.create(player)
        .title("<gold><bold>⚡ Your Stats")
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
}
```

---

## 🔗 Task Chains

### Load Player Data
```java
@EventHandler
public void onJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    
    TaskChain.create(plugin)
        .async(() -> database.loadPlayerData(player.getUniqueId()))
        .syncAt(player, data -> {
            if (data != null) {
                player.setLevel(data.level);
                player.sendMessage("<green>✓ Data loaded!");
            } else {
                createNewPlayer(player);
            }
        })
        .exceptionally(ex -> {
            player.sendMessage("<red>✗ Failed to load data!");
            getLogger().severe("Error: " + ex.getMessage());
        })
        .execute();
}
```

### Shop Purchase
```java
private void purchaseItem(Player player, Material item, int cost) {
    TaskChain.create(plugin)
        .async(() -> economy.getBalance(player.getUniqueId()))
        .syncAt(player, balance -> {
            if (balance >= cost) {
                economy.withdraw(player.getUniqueId(), cost);
                player.getInventory().addItem(new ItemStack(item));
                player.sendMessage("<green>✓ Purchase successful!");
            } else {
                player.sendMessage("<red>✗ Not enough money!");
            }
        })
        .execute();
}
```

---

## ⚡ Scheduler (Folia-Safe)

### Entity-Specific Tasks
```java
// ✅ Folia-safe - runs on player's region thread
getScheduler().runAt(player, () -> {
    player.damage(5.0);
    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1));
});

// Delayed
getScheduler().runAtLater(player, () -> {
    player.sendMessage("<yellow>5 seconds passed!");
}, 5, TimeUnit.SECONDS);
```

### Location-Specific Tasks
```java
// ✅ Folia-safe - runs on location's region thread
Location spawn = world.getSpawnLocation();
getScheduler().runAt(spawn, () -> {
    world.spawnParticle(Particle.FLAME, spawn, 10);
    world.createExplosion(spawn, 4.0f);
});
```

---

## 🎮 Complete Plugin Example

```java
public class MyPlugin implements IonPlugin {
    
    private final Map<UUID, IonScoreboard> scoreboards = new HashMap<>();
    
    @Override
    public void onEnable() {
        // Register commands
        getCommandRegistry().register(new ShopCommand(this));
        
        // Register events
        Bukkit.getPluginManager().registerEvents(new JoinListener(this), this);
        
        getLogger().info("✓ Plugin enabled!");
    }
    
    @Override
    public void onDisable() {
        scoreboards.values().forEach(IonScoreboard::destroy);
        scoreboards.clear();
        getScheduler().cancelAll();
    }
    
    @Override
    public String getName() {
        return "MyPlugin";
    }
    
    private class JoinListener implements Listener {
        private final MyPlugin plugin;
        
        public JoinListener(MyPlugin plugin) {
            this.plugin = plugin;
        }
        
        @EventHandler
        public void onJoin(PlayerJoinEvent event) {
            Player player = event.getPlayer();
            
            // Show scoreboard
            IonScoreboard board = IonScoreboard.create(player)
                .title("<gold>Welcome!")
                .line("")
                .dynamicLine(0, p -> "<yellow>Players: " + Bukkit.getOnlinePlayers().size())
                .autoUpdate(20L)
                .show();
            
            scoreboards.put(player.getUniqueId(), board);
        }
    }
    
    private class ShopCommand implements IonCommand {
        private final MyPlugin plugin;
        
        public ShopCommand(MyPlugin plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public boolean execute(CommandContext ctx) {
            Player player = (Player) ctx.getSender();
            
            IonGui.builder()
                .title("<gold>Shop")
                .rows(3)
                .item(13, IonItem.of(Material.DIAMOND, "<aqua>Diamond - $100"),
                    click -> {
                        click.getPlayer().sendMessage("<green>Purchased!");
                        click.close();
                    })
                .fillBorderBuilder(IonItem.of(Material.GRAY_STAINED_GLASS_PANE, " "))
                .build()
                .open(player);
            
            return true;
        }
        
        @Override
        public String getName() { return "shop"; }
        @Override
        public String getDescription() { return "Open shop"; }
        @Override
        public String getUsage() { return "/shop"; }
        @Override
        public String getPermission() { return "myplugin.shop"; }
    }
}
```

---

## 📚 More Examples

For more detailed examples, see:
- [Getting Started Guide](GETTING_STARTED.md) - Complete tutorial
- [API Reference](API_REFERENCE.md) - Full API documentation
- [Quick Reference](QUICK_REFERENCE.md) - Quick cheatsheet

---

**Need help?** Join our [Discord](https://discord.com/invite/VQjTVKjs46)!
