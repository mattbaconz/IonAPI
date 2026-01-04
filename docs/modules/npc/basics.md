# NPC Basics

Create lightweight, packet-based NPCs.

### Creating an NPC

```java
IonNPC npc = IonNPC.builder(plugin)
    .location(player.getLocation())
    .name("<gold><bold>Shop Keeper")
    .skin("Notch") // Fetch skin from username
    .lookAtPlayer(true)
    .build();

// Show to everyone
npc.showAll();
```

### Interaction

```java
IonNPC npc = IonNPC.builder(plugin)
    .location(loc)
    .name("Quest Giver")
    .onClick(player -> {
        player.sendMessage("Hello adventurer!");
        npc.swingMainHand(); // Play animation
    })
    .build();
```

### Management

```java
// Move NPC
npc.teleport(newLocation);

// Hide temporarily
npc.setVisible(false);

// Remove completely
npc.destroy();
```
