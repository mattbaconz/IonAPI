# Confirmation Menus

Easily create "Are you sure?" dialogs.

### Simple Confirmation

```java
ConfirmationGui.simple("<gray>Delete all data?", player -> {
    // Action to run on confirm
    dataManager.deleteAll(player);
    player.sendMessage("<green>Data deleted.");
}).open(player);
```

### Custom Confirmation

```java
ConfirmationGui.create()
    .title("<red><bold>⚠ Hazardous Operation")
    .message("<gray>This will reset the world. Continue?")
    .confirmItem(IonItem.builder(Material.TNT).name("<red>EXPLODE").build())
    .cancelItem(IonItem.builder(Material.WATER_BUCKET).name("<aqua>Safe").build())
    .danger() // Applies red/warning styling
    .onConfirm(player -> {
        world.explode();
    })
    .onCancel(player -> {
        player.sendMessage("<green>Phew!");
    })
    .open(player);
```
