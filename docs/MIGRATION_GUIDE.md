# 🔄 Migration Guide to IonAPI

Quick guide to migrate from Bukkit API to IonAPI.

---

## 🎯 Why Migrate?

- ✅ **50-80% less code**
- ✅ **Type-safe fluent APIs**
- ✅ **Folia compatible**
- ✅ **Modern design patterns**
- ✅ **Better maintainability**

---

## 📦 Quick Migration

### 1. Item Creation

**Before (Bukkit):**
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

**After (IonAPI):**
```java
ItemStack sword = IonItem.builder(Material.DIAMOND_SWORD)
    .name("<red>Legendary Sword")
    .lore("<gray>Forged in dragon fire")
    .enchant(Enchantment.SHARPNESS, 5)
    .unbreakable()
    .build();
```

**Result:** 60% less code! ✨

---

### 2. GUI Creation

**Before (Bukkit):**
```java
private final Map<UUID, Inventory> activeGuis = new HashMap<>();

public void openShop(Player player) {
    Inventory inv = Bukkit.createInventory(null, 27, "Shop");
    
    ItemStack diamond = new ItemStack(Material.DIAMOND);
    ItemMeta meta = diamond.getItemMeta();
    meta.setDisplayName("Buy Diamond - $100");
    diamond.setItemMeta(meta);
    inv.setItem(10, diamond);
    
    activeGuis.put(player.getUniqueId(), inv);
    player.openInventory(inv);
}

@EventHandler
public void onInventoryClick(InventoryClickEvent event) {
    Player player = (Player) event.getWhoClicked();
    if (!activeGuis.containsValue(event.getClickedInventory())) return;
    
    event.setCancelled(true);
    if (event.getSlot() == 10) {
        handlePurchase(player, "diamond", 100);
    }
}

@EventHandler
public void onInventoryClose(InventoryCloseEvent event) {
    activeGuis.remove(event.getPlayer().getUniqueId());
}
```

**After (IonAPI):**
```java
public void openShop(Player player) {
    IonGui.builder()
        .title("<gold>Shop")
        .rows(3)
        .item(10, IonItem.of(Material.DIAMOND, "Buy Diamond - $100"),
            click -> handlePurchase(player, "diamond", 100))
        .fillBorderBuilder(IonItem.of(Material.GRAY_STAINED_GLASS_PANE, " "))
        .build()
        .open(player);
}
```

**Result:** 70% less code! No manual event tracking! ✨

---

### 3. Async Operations

**Before (Bukkit):**
```java
public void loadPlayerData(Player player) {
    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
        try {
            PlayerData data = database.query(player.getUniqueId());
            
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.setHealth(data.health);
                player.setLevel(data.level);
                
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    player.sendMessage("Welcome back!");
                }, 20L);
            });
        } catch (Exception e) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.sendMessage("Failed to load data!");
            });
        }
    });
}
```

**After (IonAPI):**
```java
public void loadPlayerData(Player player) {
    TaskChain.create(plugin)
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

**Result:** Cleaner, more readable, Folia-safe! ✨

---

### 4. Folia Compatibility

**Before (Paper only):**
```java
Bukkit.getScheduler().runTask(plugin, () -> {
    player.damage(5.0); // ❌ May crash on Folia
});
```

**After (Paper & Folia):**
```java
plugin.getScheduler().runAt(player, () -> {
    player.damage(5.0); // ✅ Works on both!
});
```

---

## 🚀 Migration Steps

### 1. Add IonAPI Dependency

```kotlin
dependencies {
    implementation("com.github.mattbaconz:IonAPI:1.2.6")
}

tasks.shadowJar {
    relocate("com.ionapi", "your.plugin.libs.ionapi")
}
```

### 2. Update Plugin Class

```java
// Before
public class MyPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        // ...
    }
}

// After
public class MyPlugin implements IonPlugin {
    @Override
    public void onEnable() {
        // ...
    }
    
    @Override
    public String getName() {
        return "MyPlugin";
    }
}
```

### 3. Replace Bukkit APIs

- ✅ Replace `ItemStack` creation with `IonItem.builder()`
- ✅ Replace manual GUIs with `IonGui.builder()`
- ✅ Replace `Bukkit.getScheduler()` with `getScheduler()`
- ✅ Use `TaskChain` for async/sync workflows

### 4. Test Thoroughly

- ✅ Test on Paper server
- ✅ Test on Folia server (if applicable)
- ✅ Verify all features work

---

## 📊 Benefits Summary

| Feature | Before | After | Improvement |
|---------|--------|-------|-------------|
| Item Creation | 15+ lines | 6 lines | 60% less |
| GUI System | 40+ lines | 8 lines | 80% less |
| Async Tasks | Nested callbacks | Linear chain | Much cleaner |
| Folia Support | Manual | Automatic | Built-in |

---

## 💡 Tips

1. **Migrate incrementally** - One feature at a time
2. **Test frequently** - Catch issues early
3. **Use examples** - Check [Examples](EXAMPLES.md) for patterns
4. **Ask for help** - Join our [Discord](https://discord.com/invite/VQjTVKjs46)

---

## 📚 More Resources

- [Getting Started](GETTING_STARTED.md) - Complete tutorial
- [API Reference](API_REFERENCE.md) - Full API docs
- [Examples](EXAMPLES.md) - Code examples
- [Quick Reference](QUICK_REFERENCE.md) - Cheatsheet

---

**Ready to migrate?** Start with [Getting Started](GETTING_STARTED.md)!
