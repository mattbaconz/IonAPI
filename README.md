<div align="center">

# ⚡ IonAPI

### Modern, Multi-Platform Minecraft Plugin API

*Write less code. Build better plugins. Support Paper & Folia.*

[![Discord](https://img.shields.io/badge/Discord-Join%20Server-5865F2?style=for-the-badge&logo=discord&logoColor=white)](https://discord.com/invite/VQjTVKjs46)
[![GitHub](https://img.shields.io/badge/GitHub-mattbaconz-181717?style=for-the-badge&logo=github)](https://github.com/mattbaconz)
[![Ko-fi](https://img.shields.io/badge/Ko--fi-Support-FF5E5B?style=for-the-badge&logo=ko-fi&logoColor=white)](https://ko-fi.com/mbczishim/tip)
[![PayPal](https://img.shields.io/badge/PayPal-Donate-00457C?style=for-the-badge&logo=paypal&logoColor=white)](https://www.paypal.com/paypalme/MatthewWatuna)

**Reduce boilerplate by 50-80%** • **Unified Paper/Folia API** • **Modern Fluent Design**

[📚 Documentation](docs/GETTING_STARTED.md) • [🚀 Quick Start](#-quick-start) • [💡 Examples](docs/EXAMPLES.md) • [🤝 Contributing](CONTRIBUTING.md)

</div>

---

## ✨ Why IonAPI?

```java
// ❌ Old Way (Bukkit API) - 15+ lines
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

// ✅ New Way (IonAPI) - 6 lines
ItemStack sword = IonItem.builder(Material.DIAMOND_SWORD)
    .name("<red>Legendary Sword")
    .lore("<gray>Forged in dragon fire")
    .enchant(Enchantment.SHARPNESS, 5)
    .unbreakable()
    .build();
```

**60% less code. 100% more readable. Fully type-safe.**

---

## 📊 IonAPI vs Others

<table>
<tr>
<th>Feature</th>
<th>IonAPI</th>
<th>Bukkit API</th>
<th>Other Libraries</th>
</tr>
<tr>
<td><strong>Folia Support</strong></td>
<td>✅ Native</td>
<td>❌ No</td>
<td>⚠️ Partial</td>
</tr>
<tr>
<td><strong>Async ORM</strong></td>
<td>✅ Built-in + Caching</td>
<td>❌ No</td>
<td>⚠️ Basic</td>
</tr>
<tr>
<td><strong>Economy API</strong></td>
<td>✅ Vault + Async</td>
<td>❌ No</td>
<td>⚠️ Vault only</td>
</tr>
<tr>
<td><strong>Redis Support</strong></td>
<td>✅ Pub/Sub + KV</td>
<td>❌ No</td>
<td>❌ Rare</td>
</tr>
<tr>
<td><strong>Hot-Reload Config</strong></td>
<td>✅ WatchService</td>
<td>❌ Manual</td>
<td>❌ Manual</td>
</tr>
<tr>
<td><strong>Item Builder</strong></td>
<td>✅ MiniMessage</td>
<td>⚠️ Legacy colors</td>
<td>✅ Varies</td>
</tr>
<tr>
<td><strong>GUI System</strong></td>
<td>✅ Fluent + Pagination</td>
<td>❌ Manual</td>
<td>✅ Varies</td>
</tr>
<tr>
<td><strong>Task Chains</strong></td>
<td>✅ Async/Sync</td>
<td>⚠️ Basic</td>
<td>⚠️ Limited</td>
</tr>
<tr>
<td><strong>Testing Framework</strong></td>
<td>✅ Mocks included</td>
<td>❌ No</td>
<td>❌ Rare</td>
</tr>
<tr>
<td><strong>Learning Curve</strong></td>
<td>🟢 Low</td>
<td>🟡 Medium</td>
<td>🟡 Varies</td>
</tr>
<tr>
<td><strong>Code Reduction</strong></td>
<td>🟢 50-80%</td>
<td>-</td>
<td>🟡 30-50%</td>
</tr>
</table>

---

## 🎯 Features

<table>
<tr>
<td width="50%">

### 🔥 Core Features
- ⚡ **Unified Scheduler** - Paper & Folia compatible
- 🎮 **Modern Commands** - Fluent registration
- ⚙️ **Smart Config** - Type-safe configuration
- 📢 **Event Bus** - Custom event system
- 🛠️ **Utilities** - MiniMessage support

</td>
<td width="50%">

### 🆕 Extended Features
- 🎨 **Item Builder** - Fluent ItemStack creation
- 📦 **GUI System** - Interactive menus
- 📊 **Scoreboard/BossBar** - Dynamic UI
- 🔗 **Task Chains** - Async/sync workflows
- 💾 **Database ORM** - Simple data persistence

</td>
</tr>
</table>

### 🌟 Additional Modules
- 💰 **Economy System** - Vault-compatible with async API (~14 KB)
- 🔴 **Redis Integration** - Pub/sub messaging + KV storage (~9 KB + Lettuce)
- 🔥 **Hot-Reload Config** - Auto-reload on file changes (built-in)
- 🔌 **Cross-Server Messaging** - Velocity/BungeeCord support (~11 KB)
- 👻 **Packet NPCs** - Lightweight, zero-tick NPCs (~24 KB)
- 🏷️ **PlaceholderAPI Bridge** - Auto-registration (~7 KB)
- 💉 **Dependency Injection** - Clean architecture (~6 KB)
- 🧪 **Unit Testing** - Mock framework (~21 KB)
- 🔄 **Compatibility Layer** - Java 8+ polyfills (~38 KB)

### 🚀 v1.5.0 Features (Enterprise-Grade)
- 📢 **Working Event Bus** - Real in-memory event dispatcher with priority sorting
- 💾 **SQL Injection Protection** - Identifier quoting (`backticks`) for all table/column names
- 💉 **Advanced DI** - Constructor injection support + Circular dependency detection
- 💾 **No Thread Starvation** - Dedicated database executors for all async operations
- 👻 **NPC Bridge Pattern** - Refactored to `NmsAdapter` interface for modularity

### 🆕 v1.3.0 Features
- 📊 **Scoreboard Fix** - No more flashing on updates
- ✨ **Animated Lines** - Cycling scoreboard text
- ✅ **ConfirmationGui** - Simple yes/no dialogs
- 💀 **Skull Textures** - Custom head textures via base64
- 🎨 **Leather Colors** - Colored leather armor
- 🧪 **Potion Effects** - Fluent potion builder

### 🔧 v1.2.6 Features
- ⏱️ **CooldownManager** - Thread-safe player cooldowns
- 🚦 **RateLimiter** - Sliding window rate limiting
- 💬 **MessageBuilder** - Fluent MiniMessage builder with templates
- 📊 **IonScoreboard** - Easy scoreboard creation
- 📈 **IonBossBar** - Boss bar management
- 📉 **Metrics** - Lightweight performance monitoring
- ⚡ **BatchOperation** - 10-50x faster bulk database operations
- 🔄 **ReflectionCache** - Cached entity metadata for ORM

**Total size (all modules): ~260 KB** - Smaller than most images!

---

## 📦 Installation

> 💡 **Easy Shading**: IonAPI is designed to be easily shadable! Just add the Shadow plugin and dependency - no complex configuration needed.

### Gradle (Kotlin DSL) - Recommended ⭐

```kotlin
plugins {
    id("com.gradleup.shadow") version "8.3.0"
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    // IonAPI automatically shades into your plugin!
    implementation("com.github.mattbaconz:IonAPI:1.5.0")
}

tasks.shadowJar {
    // ⚠️ IMPORTANT: Relocate to avoid conflicts!
    relocate("com.ionapi", "your.plugin.libs.ionapi")
    
    // Optional: Minimize JAR size
    minimize()
}
```

**✨ That's it!** IonAPI is designed to be easily shadable. Just add the dependency and Shadow plugin handles the rest!

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
    implementation 'com.github.mattbaconz:IonAPI:1.5.0'
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
        <groupId>com.github.mattbaconz</groupId>
        <artifactId>IonAPI</artifactId>
        <version>1.4.0</version>
    </dependency>
</dependencies>

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

> 💡 **Pro Tip**: Always relocate IonAPI to avoid conflicts when multiple plugins use different versions! For detailed instructions, module sizes, and dependency graphs, see the [Shading Guide](docs/SHADING.md).

### 🎯 Shading Made Easy

IonAPI is **designed for easy adoption** with minimal configuration:

**✅ What you get:**
- 📦 **Single JAR** - Everything bundled together
- 🔒 **Conflict-free** - Proper relocation prevents issues
- 🪶 **Lightweight** - Only ~300KB when shaded
- ⚡ **Fast** - No runtime dependencies to load
- 🎯 **Simple** - Just add Shadow plugin and dependency

**Example: Complete build.gradle.kts**
```kotlin
plugins {
    java
    id("com.gradleup.shadow") version "8.3.0"
}

group = "com.example"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    implementation("com.github.mattbaconz:IonAPI:1.5.0")
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        relocate("com.ionapi", "${project.group}.libs.ionapi")
        minimize() // Optional: Reduce JAR size
    }
    
    build {
        dependsOn(shadowJar)
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}
```

**Build your plugin:**
```bash
./gradlew shadowJar
# Your plugin JAR with IonAPI included: build/libs/YourPlugin-1.0.0.jar
```

---

## 🚀 Quick Start

### 1️⃣ Create Your Plugin

```java
import com.ionapi.api.IonPlugin;

public class MyPlugin implements IonPlugin {
    
    @Override
    public void onEnable() {
        getLogger().info("🚀 MyPlugin enabled!");
        
        // Register commands
        getCommandRegistry().register(new HelloCommand());
        
        // Load config
        IonConfig config = getConfigProvider().getConfig();
        String message = config.getString("welcome-message");
    }
    
    @Override
    public void onDisable() {
        getScheduler().cancelAll();
        getLogger().info("👋 MyPlugin disabled!");
    }
    
    @Override
    public String getName() {
        return "MyPlugin";
    }
}
```

### 2️⃣ Create a Command

```java
public class HelloCommand implements IonCommand {
    @Override
    public boolean execute(CommandContext ctx) {
        String name = ctx.getArg(0, "World");
        ctx.reply("<green>Hello, <bold>" + name + "</bold>!");
        return true;
    }
    
    @Override
    public String getName() { return "hello"; }
    @Override
    public String getDescription() { return "Greets a player"; }
    @Override
    public String getUsage() { return "/hello [name]"; }
    @Override
    public String getPermission() { return "myplugin.hello"; }
}
```

### 3️⃣ Configure Shading (build.gradle.kts)

```kotlin
plugins {
    java
    id("com.gradleup.shadow") version "8.3.0"
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    implementation("com.github.mattbaconz:IonAPI:1.5.0")
}

tasks.shadowJar {
    archiveClassifier.set("")
    // ⚠️ CRITICAL: Always relocate to avoid conflicts
    relocate("com.ionapi", "com.yourname.yourplugin.libs.ionapi")
    minimize()
}
```

### 4️⃣ Build & Run

```bash
./gradlew shadowJar
# Copy build/libs/YourPlugin-1.0.0.jar to server/plugins/
```

**That's it!** 🎉

> 💡 **Why relocate?** Prevents conflicts when multiple plugins use IonAPI. See [SHADING_GUIDE.md](docs/SHADING.md) for details.

---

## 💡 Feature Showcase

### 🎨 Item Builder

```java
ItemStack sword = IonItem.builder(Material.DIAMOND_SWORD)
    .name("<gradient:red:blue>Legendary Sword")
    .lore(
        "<gray>Forged in dragon fire",
        "",
        "<gold>⚔ Legendary Weapon"
    )
    .enchant(Enchantment.SHARPNESS, 5)
    .enchant(Enchantment.FIRE_ASPECT, 2)
    .unbreakable()
    .glow()
    .build();
```

### 📦 GUI System

```java
IonGui.builder()
    .title("<gold><bold>✨ Shop Menu")
    .rows(3)
    .item(10, diamondItem, click -> {
        Player player = click.getPlayer();
        if (buyItem(player, 100)) {
            player.sendMessage("<green>✓ Purchased!");
            click.close();
        } else {
            player.sendMessage("<red>✗ Not enough money!");
        }
    })
    .fillBorderBuilder(IonItem.of(Material.GRAY_STAINED_GLASS_PANE, " "))
    .build()
    .open(player);
```

### 📊 Dynamic Scoreboard

```java
IonScoreboard.builder()
    .title("<gold><bold>⚡ Server Stats")
    .line(15, "<gray>━━━━━━━━━━━━━━")
    .line(14, "<yellow>Players: <white>{players}")
    .line(13, "<green>Health: <white>{health}")
    .line(12, "")
    .line(11, "<aqua>example.com")
    .placeholder("players", p -> String.valueOf(Bukkit.getOnlinePlayers().size()))
    .placeholder("health", p -> String.valueOf((int) p.getHealth()) + "❤")
    .updateInterval(20)  // Updates every second
    .fixedWidth(20)      // Prevents width changes
    .build()
    .show(player);
```

### 🔗 Task Chains (Async/Sync)

```java
TaskChain.create(plugin)
    .async(() -> database.loadPlayerData(uuid))
    .syncAt(player, data -> {
        player.setLevel(data.level);
        player.sendMessage("<green>✓ Data loaded!");
    })
    .delay(2, TimeUnit.SECONDS)
    .syncAt(player, () -> player.sendMessage("<gold>Welcome back!"))
    .exceptionally(ex -> player.sendMessage("<red>✗ Failed to load data!"))
    .execute();
```

### 💾 Database ORM

```java
@Table("players")
@Cacheable(ttl = 60) // Cache for 60 seconds
public class PlayerData {
    @PrimaryKey private UUID uuid;
    @Column private String name;
    @Column private int level;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "guild_id")
    private Guild guild;
}

// Simple queries
PlayerData data = db.find(PlayerData.class, playerUuid);
data.setLevel(data.getLevel() + 1);
db.save(data);

// Async support
db.findAsync(PlayerData.class, uuid)
    .thenAccept(data -> processData(data));
```

### 💰 Economy System

```java
// Check balance
IonEconomy.getBalance(player.getUniqueId()).thenAccept(balance -> {
    player.sendMessage("Balance: " + IonEconomy.format(balance));
});

// Fluent transaction API
IonEconomy.transaction(player.getUniqueId())
    .withdraw(100)
    .reason("Shop purchase")
    .commit()
    .thenAccept(result -> {
        if (result.isSuccess()) {
            player.sendMessage("<green>Purchase complete!");
        }
    });

// Transfer between players
IonEconomy.transfer(sender, receiver, BigDecimal.valueOf(50));
```

### 🔴 Redis Pub/Sub

```java
IonRedis redis = IonRedisBuilder.create()
    .host("localhost")
    .port(6379)
    .password("secret")
    .build();

// Subscribe to channel
redis.subscribe("player-events", message -> {
    String data = message.getData();
    Bukkit.broadcastMessage("Event: " + data);
});

// Publish message
redis.publish("player-events", "Player joined: " + player.getName());

// Key-value storage with TTL
redis.set("player:" + uuid, playerData, 3600); // Expires in 1 hour
```

### 🔥 Hot-Reload Config

```java
HotReloadConfig config = HotReloadConfig.create(this, "config.yml")
    .onReload(cfg -> {
        welcomeMessage = cfg.getString("welcome-message");
        maxPlayers = cfg.getInt("max-players");
        getLogger().info("Config reloaded!");
    })
    .start();

// Edit config.yml while server is running - changes apply instantly!
```

---

## 🎯 Platform Compatibility

| Feature | Paper | Folia |
|---------|:-----:|:-----:|
| ⚡ Scheduler | ✅ Main thread | ✅ Region-aware |
| 🎮 Commands | ✅ | ✅ |
| ⚙️ Configuration | ✅ | ✅ |
| 📢 Events | ✅ | ✅ |
| 🛠️ Utilities | ✅ | ✅ |
| 🎨 Item Builder | ✅ | ✅ |
| 📦 GUI System | ✅ | ✅ |
| 📊 UI Components | ✅ | ✅ |
| 🔗 Task Chains | ✅ | ✅ Folia-optimized |

---

## 📚 Documentation

<table>
<tr>
<td width="33%">

### 📖 Getting Started
- [Quick Start Guide](docs/GETTING_STARTED.md)
- [API Reference](docs/API_REFERENCE.md)
- [Quick Reference](docs/QUICK_REFERENCE.md)

</td>
<td width="33%">

### 💡 Learn More
- [Examples](docs/EXAMPLES.md)
- [Folia Guide](docs/FOLIA_GUIDE.md)
- [Javadoc Guide](docs/JAVADOC_GUIDE.md)

</td>
<td width="33%">

### 🔧 Advanced
- [Migration Guide](docs/MIGRATION_GUIDE.md)
- [Shading Guide](docs/SHADING.md)
- [Upgrade Guide](docs/UPGRADE.md)
- [Troubleshooting](docs/TROUBLESHOOTING.md)

</td>
</tr>
</table>

---

## 🏗️ Project Structure

```
IonAPI/
├── 🎯 ion-api/          Core API interfaces
├── ⚙️ ion-core/         Base implementations
├── 🎨 ion-item/         Item Builder
├── 📦 ion-gui/          GUI System
├── 📊 ion-ui/           Scoreboard & BossBar
├── 🔗 ion-tasks/        Task Chains
├── 💾 ion-database/     Database ORM + Caching
├── 💰 ion-economy/      Economy API + Vault hook
├── 🔴 ion-redis/        Redis pub/sub + KV store
├── 🔌 ion-proxy/        Cross-server messaging
├── 👻 ion-npc/          Packet NPCs
├── 🏷️ ion-placeholder/  PlaceholderAPI bridge
├── 💉 ion-inject/       Dependency injection
├── 🧪 ion-test/         Testing framework
├── 🔄 ion-compat/       Compatibility layer
└── 🖥️ platforms/        Paper & Folia implementations
```

---

## 🤝 Contributing

We love contributions! Whether it's:

- 🐛 **Bug reports**
- 💡 **Feature requests**
- 📝 **Documentation improvements**
- 🔧 **Code contributions**

Check out our [Contributing Guide](CONTRIBUTING.md) to get started!

---

## 💖 Support the Project

If IonAPI helps you build better plugins, consider supporting development:

<div align="center">

[![Ko-fi](https://img.shields.io/badge/Ko--fi-Buy%20Me%20a%20Coffee-FF5E5B?style=for-the-badge&logo=ko-fi&logoColor=white)](https://ko-fi.com/mbczishim/tip)
[![PayPal](https://img.shields.io/badge/PayPal-Donate-00457C?style=for-the-badge&logo=paypal&logoColor=white)](https://www.paypal.com/paypalme/MatthewWatuna)

</div>

---

## 🌟 Community

<div align="center">

[![Discord](https://img.shields.io/badge/Discord-Join%20Our%20Server-5865F2?style=for-the-badge&logo=discord&logoColor=white)](https://discord.com/invite/VQjTVKjs46)
[![GitHub](https://img.shields.io/badge/GitHub-Follow%20@mattbaconz-181717?style=for-the-badge&logo=github)](https://github.com/mattbaconz)

**Join our Discord for:**
- 💬 Plugin development help
- 🐛 Bug reports & support
- 💡 Feature discussions
- 🎉 Community showcase

</div>

---

## 📜 License

IonAPI is open source software licensed under the [MIT License](LICENSE).

---

## 🙏 Acknowledgments

Built with ❤️ by [mattbaconz](https://github.com/mattbaconz)

Special thanks to:
- 🎮 **PaperMC** - For the amazing Paper & Folia platforms
- 🎨 **Adventure API** - For modern text components
- 🌟 **All contributors** - For making IonAPI better

---

<div align="center">

### ⭐ Star this repo if you find it useful!

**Made with ❤️ for the Minecraft plugin development community**

[📚 Documentation](docs/GETTING_STARTED.md) • [💡 Examples](docs/EXAMPLES.md) • [🐛 Report Bug](https://github.com/mattbaconz/IonAPI/issues) • [💬 Discord](https://discord.com/invite/VQjTVKjs46)

</div>
