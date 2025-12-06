# 🚀 IonAPI v1.1.0 - Economy, Redis & Enterprise Features

A major update focused on **security**, **economy systems**, and **enterprise-grade features** for production Minecraft plugins.

---

## 🔒 Security Fixes (Critical)

- **Fixed SQL injection vulnerability** in QueryBuilder - all identifiers, operators, and directions are now sanitized
- **Fixed resource leak** in Transaction - connections properly closed on failure
- Added comprehensive input validation across database operations

---

## 💰 Economy System (NEW)

Complete economy API with Vault integration and async-first design:

- **`IonEconomy`** - Static API with fluent transactions
- **Multi-currency support** with custom formatting
- **BigDecimal precision** for accurate monetary calculations
- **Vault compatibility** - works with existing economy plugins
- **Admin commands**: `/ion eco set/give/debug`

```java
// Simple usage
IonEconomy.withdraw(player.getUniqueId(), 100).thenAccept(result -> {
    if (result.isSuccess()) {
        player.sendMessage("Purchase complete!");
    }
});

// Fluent API
IonEconomy.transaction(player.getUniqueId())
    .withdraw(100)
    .reason("Shop purchase")
    .commit();
```

---

## 🔴 Redis Integration (NEW)

Cross-server communication made easy:

- **Pub/Sub messaging** for real-time events
- **Key-value storage** with TTL support
- **Connection pooling** with Lettuce client
- **Health monitoring** and statistics

```java
IonRedis redis = IonRedisBuilder.create()
    .host("localhost")
    .port(6379)
    .build();

redis.subscribe("player-events", message -> {
    Bukkit.broadcastMessage("Event: " + message.data());
});

redis.publish("player-events", "Player joined!");
```

---

## 📊 ORM Enhancements

### Entity Relationships
- **`@OneToMany`** - One-to-many relationships
- **`@ManyToOne`** - Many-to-one relationships  
- **`@JoinColumn`** - Foreign key specification
- **`FetchType`** - EAGER/LAZY loading
- **`CascadeType`** - Cascade operations

```java
@Table("guilds")
public class Guild {
    @PrimaryKey
    private UUID id;
    
    @OneToMany(mappedBy = "guildId", fetch = FetchType.LAZY)
    private List<GuildMember> members;
}
```

### Entity Caching
- **`@Cacheable`** - Automatic caching with TTL
- **Thread-safe** cache implementation
- **Automatic expiration** and cleanup
- **Cache statistics** and monitoring

```java
@Table("player_settings")
@Cacheable(ttl = 60, maxSize = 500)
public class PlayerSettings {
    // Cached for 60 seconds
}
```

---

## 🔥 Hot-Reload Configuration (NEW)

Real-time config updates without server restart:

```java
HotReloadConfig config = HotReloadConfig.create(this, "config.yml")
    .onReload(cfg -> {
        loadSettings(cfg);
        getLogger().info("Config reloaded!");
    })
    .start();

// Edit config.yml - changes apply instantly!
```

---

## 📦 Installation

### Gradle (Kotlin DSL)
```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.mattbaconz:IonAPI:1.1.0")
}

tasks.shadowJar {
    relocate("com.ionapi", "${project.group}.libs.ionapi")
}
```

### Maven
```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>

<dependency>
    <groupId>com.github.mattbaconz</groupId>
    <artifactId>IonAPI</artifactId>
    <version>1.1.0</version>
</dependency>
```

---

## 📚 New Examples

- **`EconomyExample.java`** - Economy system usage
- **`AdvancedDatabaseExample.java`** - ORM relationships & caching
- **`RedisExample.java`** - Cross-server messaging
- **`HotReloadExample.java`** - Config hot-reloading

---

## 🔧 CI/CD

- GitHub Actions workflow for automated builds
- JitPack integration for Maven distribution
- Automated testing on push/PR

---

## 📊 Statistics

- **50+ new files** created
- **3,500+ lines** of production code
- **6 comprehensive examples** added
- **Zero breaking changes** from v1.0.0

---

## 🔗 Links

- **Documentation**: [Getting Started](docs/GETTING_STARTED.md)
- **Discord**: https://discord.com/invite/VQjTVKjs46
- **Support**: [Ko-fi](https://ko-fi.com/mbczishim/tip) | [PayPal](https://www.paypal.com/paypalme/MatthewWatuna)

---

## ⬆️ Upgrading from v1.0.0

**No breaking changes!** All v1.0.0 code continues to work.

Simply update your dependency version:
```kotlin
implementation("com.github.mattbaconz:IonAPI:1.1.0")
```

New features are opt-in - use only what you need!

---

## 🙏 Credits

Built with ❤️ by [@mattbaconz](https://github.com/mattbaconz)

Special thanks to:
- PaperMC for Paper & Folia
- Lettuce for Redis client
- Vault for economy integration
- All contributors and testers

---

**Full Changelog**: https://github.com/mattbaconz/IonAPI/compare/v1.0.0...v1.1.0
