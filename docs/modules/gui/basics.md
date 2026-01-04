# GUI Basics

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
