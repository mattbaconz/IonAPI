# IonAPI Technical Analysis & Design Decisions

This document addresses critical technical considerations and potential "gotchas" in the IonAPI implementation, particularly regarding Folia support and the custom Event Bus system.

---

## 1. The Folia Context Problem

### The Issue

The current [`IonScheduler`](file:///c:/mcplugins/api/IonAPI/ion-api/src/main/java/com/ionapi/api/scheduler/IonScheduler.java) interface defines a context-free `run(Runnable task)` method:

```java
@NotNull
IonTask run(@NotNull Runnable task);
```

**Problem**: In Folia's multi-threaded architecture, different types of schedulers exist:
- **Global Region Scheduler** - Runs on a global region thread
- **Entity Scheduler** - Runs on the thread owning a specific entity
- **Region Scheduler** - Runs on the thread owning a specific location/region

Without context (Entity or Location), the implementation cannot determine which region thread should execute the task.

### Current Implementation Status

> [!WARNING]
> **The Folia platform implementation is not yet complete.** The platform directories (`platforms/ion-folia` and `platforms/ion-paper`) currently contain only configuration files without actual Java implementations.

Based on the API design, here's what needs to be considered:

### Risk Assessment

| Risk | Severity | Impact |
|------|----------|---------|
| **Limited Concurrency** | HIGH | If `run()` always uses Global Scheduler, tasks won't benefit from Folia's region-based parallelism |
| **Thread Safety** | CRITICAL | Using wrong scheduler could cause cross-thread entity access, leading to crashes |
| **API Limitations** | MEDIUM | Current API doesn't expose Folia's full scheduling capabilities |

### Proposed Solutions

#### Option 1: Entity/Location-Aware Scheduler Methods (Recommended)

Extend the `IonScheduler` interface with context-aware methods:

```java
public interface IonScheduler {
    // Existing methods (kept for backward compatibility)
    @NotNull IonTask run(@NotNull Runnable task);
    
    // NEW: Context-aware methods for Folia
    @NotNull IonTask runAt(@NotNull Location location, @NotNull Runnable task);
    @NotNull IonTask runAt(@NotNull Entity entity, @NotNull Runnable task);
    @NotNull IonTask runAtLater(@NotNull Location location, @NotNull Runnable task, 
                                  long delay, @NotNull TimeUnit unit);
    @NotNull IonTask runAtLater(@NotNull Entity entity, @NotNull Runnable task, 
                                  long delay, @NotNull TimeUnit unit);
}
```

**Implementation Strategy**:
- On **Paper**: All `runAt()` methods delegate to standard main thread scheduler (location/entity ignored)
- On **Folia**: Use appropriate region/entity scheduler based on context

**Benefits**:
- ✅ Backward compatible (existing context-free methods still work)
- ✅ Enables full Folia optimization for new code
- ✅ Clear API that indicates Folia-aware programming
- ✅ Graceful degradation on Paper

#### Option 2: Smart Context Detection (Limited)

Try to detect context from stack trace or thread-local storage:

```java
// Pseudo-code
public IonTask run(Runnable task) {
    if (isFolia()) {
        Entity entity = getCurrentThreadEntity(); // Try to detect
        if (entity != null) {
            return entity.getScheduler().run(plugin, task);
        }
        // Fallback to global
        return Bukkit.getGlobalRegionScheduler().run(plugin, task);
    }
    return Bukkit.getScheduler().runTask(plugin, task);
}
```

**Problems**:
- ❌ Unreliable - context detection may fail
- ❌ Hidden behavior - developers don't know which scheduler is used
- ❌ Still falls back to Global Scheduler in many cases

#### Option 3: Separate Scheduler Types

Provide different scheduler instances based on context:

```java
public interface IonPlugin {
    IonScheduler getScheduler(); // Global scheduler
    IonScheduler getScheduler(Entity entity); // Entity scheduler
    IonScheduler getScheduler(Location location); // Region scheduler
}
```

**Problems**:
- ❌ More complex API
- ❌ Harder to use correctly
- ❌ Requires developers to manage multiple scheduler instances

### Recommended Approach

**Implement Option 1** with the following guidelines:

1. **Default Behavior**: Context-free methods (`run()`, `runLater()`, etc.) use Global Scheduler on Folia
   - Document this clearly with performance implications
   - Add `@ApiNote` warnings in Javadocs

2. **Best Practice Documentation**: Guide developers to use context-aware methods when possible
   ```java
   // ❌ Suboptimal on Folia (uses global scheduler)
   plugin.getScheduler().run(() -> entity.damage(5.0));
   
   // ✅ Optimal on Folia (uses entity's region thread)
   plugin.getScheduler().runAt(entity, () -> entity.damage(5.0));
   ```

3. **Migration Path**: Existing Paper plugins work immediately; Folia optimization requires code updates

---

## 2. Event Bus Redundancy

### The Question

IonAPI introduces a custom [`EventBus`](file:///c:/mcplugins/api/IonAPI/ion-api/src/main/java/com/ionapi/api/event/EventBus.java) and [`IonEvent`](file:///c:/mcplugins/api/IonAPI/ion-api/src/main/java/com/ionapi/api/event/IonEvent.java) system alongside Bukkit's existing event framework.

**Concern**: Is this redundant? Does it isolate plugins from the ecosystem?

### Purpose & Design Intent

The custom Event Bus serves a **different purpose** than Bukkit events:

| Aspect | Bukkit Events | IonAPI EventBus |
|--------|--------------|-----------------|
| **Scope** | Server-wide, global | Plugin-internal |
| **Purpose** | Minecraft/Bukkit core events | Custom plugin logic events |
| **Ecosystem** | Plugins interact with each other | Modules within a plugin communicate |
| **Examples** | `PlayerJoinEvent`, `BlockBreakEvent` | `BalanceChangeEvent`, `PlayerLevelUpEvent` |
| **Performance** | Reflection-based | Lambda-based (faster) |
| **Type Safety** | Weak (runtime checking) | Strong (compile-time) |

### Use Cases

#### ✅ **Good Use**: Internal Plugin Events

```java
// Within your economy plugin
public class EconomyPlugin implements IonPlugin {
    
    public void updateBalance(UUID player, double amount) {
        double oldBalance = getBalance(player);
        double newBalance = oldBalance + amount;
        
        // Fire internal event for other modules to react
        BalanceChangeEvent event = new BalanceChangeEvent(player, oldBalance, newBalance);
        getEventBus().fire(event);
        
        if (!event.isCancelled()) {
            saveBalance(player, event.getNewBalance());
        }
    }
    
    private void setupListeners() {
        // Quest module listens to balance changes
        getEventBus().subscribe(BalanceChangeEvent.class, event -> {
            checkQuestProgress(event.getPlayer(), "earn_money");
        });
        
        // Achievement module also listens
        getEventBus().subscribe(BalanceChangeEvent.class, event -> {
            if (event.getNewBalance() >= 10000) {
                grantAchievement(event.getPlayer(), "MILLIONAIRE");
            }
        });
    }
}
```

**Benefits**:
- Loose coupling between plugin modules (quests, achievements, economy)
- No Bukkit event registration overhead
- Clean, type-safe API
- Easy to test in isolation

#### ❌ **Bad Use**: Replacing Bukkit Events

```java
// DON'T DO THIS - Use Bukkit events for server events
getEventBus().subscribe(CustomPlayerJoinEvent.class, event -> {
    // Other plugins can't see this!
});
```

### Relationship with Bukkit Events

The two systems should **complement** each other:

```java
public class MyPlugin implements IonPlugin {
    
    @Override
    public void onEnable() {
        // Listen to BUKKIT events for server-wide events
        Bukkit.getPluginManager().registerEvents(new BukkitListener(), this);
        
        // Use ION EventBus for internal plugin events
        getEventBus().subscribe(CustomInternalEvent.class, this::handleInternal);
    }
    
    class BukkitListener implements Listener {
        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent event) {
            // Handle Bukkit event
            Player player = event.getPlayer();
            
            // Then fire internal event if needed
            getEventBus().fire(new PlayerDataLoadedEvent(player.getUniqueId()));
        }
    }
}
```

### Performance Advantages

The IonAPI EventBus uses **lambdas instead of reflection**, providing:
- Faster subscription: `O(1)` map lookup vs annotation scanning
- Faster invocation: Direct method calls vs reflection
- Better type safety: Compile-time checking vs runtime casting

### Documentation Clarity

> [!IMPORTANT]
> The documentation should clearly state:
> 
> **IonAPI EventBus is NOT a replacement for Bukkit events.** It is designed for **internal plugin architecture** to enable loose coupling between your plugin's modules. Use Bukkit events for interacting with the server and other plugins.

---

## 3. Recommendations

### Immediate Actions

1. **Complete Folia Implementation**
   - [ ] Implement context-aware scheduler methods
   - [ ] Create `FoliaSchedulerImpl` with proper region handling
   - [ ] Add comprehensive Javadoc warnings about context-free methods

2. **Update Documentation**
   - [ ] Add architectural decision records (ADR) for Event Bus design
   - [ ] Create "Best Practices" guide for Folia-aware development
   - [ ] Add examples showing EventBus vs Bukkit Events usage

3. **API Enhancements**
   - [ ] Add `runAt(Entity, Runnable)` and `runAt(Location, Runnable)` methods
   - [ ] Consider adding scheduler diagnostics: `getSchedulerType()`, `isFoliaEnabled()`

### Long-term Considerations

1. **Folia-First Design**: Consider making context-aware methods the primary API, with context-free methods deprecated
2. **Performance Metrics**: Add optional scheduler performance tracking for debugging
3. **Migration Tools**: Provide linting/analysis tools to detect non-Folia-safe code

---

## 4. Conclusion

Both concerns raised are **valid and important**:

| Concern | Severity | Status | Resolution |
|---------|----------|--------|------------|
| Folia Context Problem | **HIGH** | ⚠️ Needs Implementation | Add context-aware scheduler methods |
| EventBus Redundancy | **LOW** | ✅ By Design | Document purpose clearly; not redundant |

The IonAPI design is sound, but requires:
- Implementation of Folia-specific features
- Clear documentation of design decisions
- Best practice guides for developers

---

## See Also

- [Getting Started Guide](GETTING_STARTED.md)
- [API Reference](API_REFERENCE.md)
- [Examples](EXAMPLES.md)
