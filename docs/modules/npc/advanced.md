# Advanced NPC Usage

### Custom Skins

You can apply a skin using a texture value and signature (from Mojang API):

```java
IonNPC.builder(plugin)
    .skin(
        "eyJ0ZXh0dXJlcyI...", // Base64 Value
        "signature_string"    // Signature
    )
    .build();
```

### Visibility Control

You can control exactly who sees the NPC:

```java
// Only show to admins
npc.hideAll();
for (Player p : Bukkit.getOnlinePlayers()) {
    if (p.hasPermission("admin")) {
        npc.show(p);
    }
}
```

### Persistent NPCs

By default, NPCs are transient. Enable persistence to keep them across restarts (requires storage implementation not shown here but the flag exists):

```java
IonNPC.builder(plugin)
    .persistent(true)
    .build();
```
