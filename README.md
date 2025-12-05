# IonAPI

A comprehensive, multi-platform Minecraft plugin API that provides a unified interface for Paper and Folia servers.

## Installation

### Gradle (Kotlin DSL)
```kotlin
plugins {
    id("com.gradleup.shadow") version "8.3.0"
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.YourOrg:IonAPI:1.0.0")
}

tasks.shadowJar {
    // IMPORTANT: Relocate to avoid conflicts with other plugins
    relocate("com.ionapi", "your.plugin.libs.ionapi")
}
```

### Gradle (Groovy)
```groovy
plugins {
    id 'com.gradleup.shadow' version '8.3.0'
}

repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.YourOrg:IonAPI:1.0.0'
}

shadowJar {
    relocate 'com.ionapi', 'your.plugin.libs.ionapi'
}
```

### Maven
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.YourOrg</groupId>
        <artifactId>IonAPI</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>

<!-- Add shade plugin for relocation -->
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version>3.5.1</version>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals><goal>shade</goal></goals>
                    <configuration>
                        <relocations>
                            <relocation>
                                <pattern>com.ionapi</pattern>
                                <shadedPattern>your.plugin.libs.ionapi</shadedPattern>
                            </relocation>
                        </relocations>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

> ⚠️ **Important**: Always relocate IonAPI to avoid conflicts when multiple plugins use different versions!

## Features

### Core Features

#### 🕒 Unified Scheduler
Cross-platform scheduler that abstracts Paper and Folia's threading differences:
```java
// Context-free (works everywhere, uses global scheduler on Folia)
plugin.getScheduler().run(() -> {
    // General task
});

// Context-aware (optimal for Folia - runs on entity's region thread)
Player player = event.getPlayer();
plugin.getScheduler().runAt(player, () -> {
    player.damage(5.0); // Safe and performant on Folia
});

plugin.getScheduler().runLater(() -> {
    // Delayed execution
}, 20, TimeUnit.SECONDS);
```

#### ⚡ Modern Command Framework
Simple, fluent command registration:
```java
CommandRegistry registry = plugin.getCommandRegistry();
registry.register(new IonCommand() {
    @Override
    public boolean execute(CommandContext ctx) {
        ctx.reply("Hello, " + ctx.getSender() + "!");
        return true;
    }
    
    @Override
    public String getName() { return "hello"; }
});
```

#### ⚙️ Configuration System
Easy configuration management:
```java
IonConfig config = plugin.getConfigProvider().getConfig();
String host = config.getString("database.host", "localhost");
int port = config.getInt("database.port", 3306);
```

#### 📢 Event Bus
Custom event system with priority support:
```java
plugin.getEventBus().subscribe(MyCustomEvent.class, event -> {
    // Handle event
});
```

#### 🛠️ Utilities
Common utilities for plugin development:
```java
Component message = TextUtil.parse("<green>Hello <bold>World</bold>!");
```

### 🆕 Extended Features

#### 🎨 Item Builder
Fluent API for creating ItemStacks:
```java
ItemStack sword = IonItem.builder(Material.DIAMOND_SWORD)
    .name("<gradient:red:blue>Legendary Sword")
    .lore("Deals massive damage", "", "§7Rarity: §6Legendary")
    .enchant(Enchantment.SHARPNESS, 5)
    .unbreakable()
    .glow()
    .build();
```

#### 📦 GUI System
Create interactive inventory menus:
```java
IonGui gui = IonGui.builder()
    .title("<gold><bold>Shop Menu")
    .rows(3)
    .item(10, diamondItem, click -> {
        click.getPlayer().sendMessage("Bought diamond!");
        click.close();
    })
    .fillBorder(borderItem)
    .build();
gui.open(player);
```

#### ✏️ Text Input (Anvil & Sign)
Capture player text input easily:
```java
// Anvil-based input
InputGui.create(plugin)
    .title("Enter player name")
    .placeholder("Type here...")
    .onComplete((player, input) -> {
        player.sendMessage("You entered: " + input);
    })
    .open(player);

// Sign-based input
SignInput.create(plugin)
    .lines("", "Enter amount", "above", "")
    .onInput((player, input) -> {
        int amount = Integer.parseInt(input);
        giveItems(player, amount);
    })
    .open(player);

// Async/Future style
SignInput.create(plugin)
    .lines("", "Enter name", "", "")
    .required()
    .openForInput(player)
    .thenAccept(name -> processName(name));
```

#### 📊 Scoreboard & BossBar
Modern UI components:
```java
// Scoreboard
IonScoreboard board = IonScoreboard.create(player)
    .title("<gold><bold>Server Stats")
    .line("Players: " + count)
    .dynamicLine(1, p -> "Health: " + p.getHealth())
    .autoUpdate(20L)
    .show();

// BossBar
IonBossBar bar = IonBossBar.create("<red>Boss Health")
    .progress(0.75)
    .color(BossBar.Color.RED)
    .show(player);
```

#### 🔗 Task Chains
Build complex async/sync workflows:
```java
TaskChain.create(plugin)
    .async(() -> fetchFromDatabase(uuid))
    .syncAt(player, data -> applyData(data))
    .delay(5, TimeUnit.SECONDS)
    .sync(() -> broadcastMessage())
    .exceptionally(ex -> handleError(ex))
    .execute();
```

#### 💾 Database Layer
Simple ORM with async support:
```java
@Table("players")
public class PlayerData {
    @PrimaryKey
    private UUID uuid;
    private String name;
    private int level;
}

// Query
PlayerData data = db.find(PlayerData.class, playerUuid);
data.setLevel(data.getLevel() + 1);
db.save(data);

// Async
db.findAsync(PlayerData.class, uuid)
    .thenAccept(data -> processData(data));
```

#### 🔌 Cross-Server Messaging (IonProxy)
Simple pub/sub messaging for Velocity/BungeeCord networks:
```java
IonMessenger messenger = IonProxy.messenger(plugin);

// Subscribe to channels
messenger.subscribe("my:channel", (player, message) -> {
    getLogger().info("Received: " + message);
});

// Broadcast to all servers
messenger.broadcast("my:channel", "Hello from " + serverName);

// Optional Redis support
IonMessenger redis = IonProxy.redis(plugin, "localhost", 6379);
```

#### 👻 Packet-Based NPCs (IonNPC)
Lightweight, zero-tick NPCs using packets:
```java
IonNPC npc = IonNPC.builder(plugin)
    .location(spawnLocation)
    .name("<gold>Shop Keeper")
    .skin("Notch")
    .lookAtPlayer(true)
    .onClick(player -> openShop(player))
    .persistent(true)
    .build();

npc.showAll();  // Show to all players
npc.destroy();  // Remove NPC
```

#### 🏷️ PlaceholderAPI Bridge
Auto-register placeholders without manual PAPI setup:
```java
IonPlaceholderRegistry.create(plugin)
    .register(SimplePlaceholder.create("myplugin")
        .staticPlaceholder("server", "My Server")
        .placeholder("name", player -> player.getName())
        .placeholder("level", player -> String.valueOf(getLevel(player)))
        .build())
    .build();
// Creates: %myplugin_server%, %myplugin_name%, %myplugin_level%
```

#### 💉 Dependency Injection
Clean up your main classes with micro-DI:
```java
// In onEnable()
IonInjector injector = IonInjector.create(this)
    .register(PlayerService.class)
    .register(EconomyService.class)
    .build();

// Create with injection
MyCommand cmd = injector.create(MyCommand.class);

// In your command
public class MyCommand implements IonCommand {
    @Inject private PlayerService playerService;
    @Inject private EconomyService economy;
    
    public boolean execute(CommandContext ctx) {
        // Dependencies are automatically injected!
    }
}
```

#### 🧪 Unit Testing (IonTest)
Test your plugin without a server:
```java
@Test
void testMyFeature() {
    MockIonPlugin plugin = IonTest.createMockPlugin("TestPlugin");
    plugin.getMockConfig().set("max-players", 50);
    
    MyService service = new MyService(plugin);
    service.initialize();
    
    // Verify scheduler was used
    assertEquals(1, plugin.getMockScheduler().getPendingTasks().size());
    
    // Execute pending tasks
    plugin.getMockScheduler().runPendingTasks();
    
    // Verify logging
    assertTrue(plugin.getMockLogger().hasMessage("Initialized"));
}
```

#### 🔄 Compatibility Layer (IonCompat)
Write modern Java code that runs on older JVMs:
```java
// Instead of List.of() (Java 9+)
List<String> list = IonList.of("a", "b", "c");
Map<String, Integer> map = IonMap.of("one", 1, "two", 2);
Set<String> set = IonSet.of("x", "y", "z");

// String utilities (Java 11+ backports)
boolean blank = IonCompat.isBlank("  ");      // true
String repeated = IonCompat.repeat("ab", 3);  // "ababab"

// Version-agnostic packets
IonPacket.actionBar(player, "<gold>Welcome!");
IonPacket.title(player, "<red>Alert!", "<gray>Check your inventory", 10, 70, 20);
IonPacket.tabList(player, "<gold>Server", "<gray>Players: " + count);

// Server version detection
if (ServerVersion.isAtLeast(1, 20)) { /* use 1.20+ features */ }
if (ServerVersion.isFolia()) { /* use Folia-specific code */ }
```

#### 🔀 Unified Events (Cross-Version)
Write event handlers once, they work on ALL server versions:
```java
// Register unified listeners
IonEvents.register(plugin, new MyListener());

public class MyListener implements Listener {
    // Works on 1.8-1.21+ - bridges legacy and modern events automatically
    @IonEventHandler
    public void onPickup(IonEntityPickupItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemStack();
        // Same code works everywhere!
    }
    
    // Only fires on 1.9+ (graceful degradation on 1.8)
    @IonEventHandler
    public void onSwap(IonPlayerSwapHandItemsEvent event) {
        // Handle off-hand swap
    }
}
```

## Quick Start

### Adding IonAPI to Your Project

**Gradle (Kotlin DSL)**
```kotlin
dependencies {
    compileOnly("com.ionapi:ion-api:1.0.0-SNAPSHOT")
}
```

### Creating a Plugin

```java
public class MyPlugin implements IonPlugin {
    
    @Override
    public void onEnable() {
        getLogger().info("MyPlugin enabled!");
        
        // Use scheduler
        getScheduler().run(() -> {
            getLogger().info("Task executed!");
        });
        
        // Register command
        getCommandRegistry().register(new MyCommand());
        
        // Load config
        IonConfig config = getConfigProvider().getConfig();
        String message = config.getString("welcome-message");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("MyPlugin disabled!");
    }
    
    @Override
    public String getName() {
        return "MyPlugin";
    }
}
```

## Modules

### Core Modules
- **ion-api**: Core API interfaces
- **ion-core**: Base implementations
- **platforms/ion-paper**: Paper-specific implementation
- **platforms/ion-folia**: Folia-specific implementation

### Feature Modules
- **ion-item**: Fluent ItemStack builder
- **ion-gui**: Interactive inventory GUI system (includes SignInput & InputGui)
- **ion-ui**: Scoreboard and BossBar components
- **ion-tasks**: Async/sync task chain builder
- **ion-database**: Database ORM layer with connection pooling
- **ion-proxy**: Cross-server messaging (Plugin Messages & Redis)
- **ion-npc**: Lightweight packet-based NPCs
- **ion-placeholder**: PlaceholderAPI bridge with auto-registration
- **ion-inject**: Lightweight dependency injection
- **ion-test**: Mock framework for unit testing
- **ion-compat**: Compatibility layer (Java 8 polyfills, version-agnostic packets)

## Platform Compatibility

| Feature | Paper | Folia |
|---------|-------|-------|
| Scheduler | ✅ Main thread | ✅ Region-aware |
| Commands | ✅ | ✅ |
| Configuration | ✅ | ✅ |
| Events | ✅ | ✅ |
| Utilities | ✅ | ✅ |

## Building

```bash
./gradlew build
```

## Documentation

- **[Getting Started Guide](docs/GETTING_STARTED.md)** - Quick start tutorial
- **[API Reference](docs/API_REFERENCE.md)** - Complete API documentation
- **[New Features Guide](docs/NEW_FEATURES.md)** - 🆕 Extended features documentation
- **[Examples](docs/EXAMPLES.md)** - Real-world usage examples
- **[Folia Guide](docs/FOLIA_GUIDE.md)** - Folia compatibility guide
- **[Contributing](CONTRIBUTING.md)** - How to contribute to IonAPI

## License

Open Source - MIT License

## Contributing

Contributions are welcome! Please read our [Contributing Guide](CONTRIBUTING.md) and check out the [API Reference](docs/API_REFERENCE.md) before getting started.

## Support

- 📖 [Documentation](docs/)
- 💬 GitHub Issues
- 🌟 Star this repo if you find it useful!
