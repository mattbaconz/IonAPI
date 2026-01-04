# Best Practices

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
IonScoreboard template = IonScoreboard.builder()
    .title("<gold>Server")
    .line(15, "Cached content")
    .build();
```
