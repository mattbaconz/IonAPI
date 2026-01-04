# Working with Items

Replace verbose ItemStack creation with a fluent builder API.

### Before (Old Way)
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

### After (New Way)
```java
import com.ionapi.item.IonItem;

ItemStack sword = IonItem.builder(Material.DIAMOND_SWORD)
    .name("<red>Legendary Sword")
    .lore("<gray>Forged in dragon fire")
    .enchant(Enchantment.SHARPNESS, 5)
    .unbreakable()
    .build();
```

### More Examples
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
