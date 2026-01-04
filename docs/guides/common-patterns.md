# Common Patterns

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
    IonScoreboard board = IonScoreboard.builder()
        .title("<gold><bold>Your Stats")
        .line(15, "<gray>━━━━━━━━━━━━━━")
        .line(14, "<yellow>Level: <white>{level}")
        .line(13, "<green>Coins: <white>{coins}")
        .line(12, "<aqua>Rank: <white>{rank}")
        .line(11, "<gray>━━━━━━━━━━━━━━")
        .placeholder("level", p -> String.valueOf(getLevel(p)))
        .placeholder("coins", p -> String.valueOf(getCoins(p)))
        .placeholder("rank", p -> getRank(p))
        .updateInterval(20)
        .build();
    
    board.show(player);
    scoreboards.put(player.getUniqueId(), board);
}

@Override
public void onDisable() {
    scoreboards.values().forEach(IonScoreboard::destroy);
    scoreboards.clear();
}
```
