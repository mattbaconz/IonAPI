# 📚 IonAPI Javadoc & Documentation Guide

Complete guide to accessing and generating IonAPI documentation.

---

## 🌐 Online Javadoc

**Latest version**: https://javadoc.jitpack.io/com/github/mattbaconz/IonAPI/1.2.6/javadoc/

Browse the complete API documentation online!

---

## 📖 Documentation Overview

### Available Guides

| Document | Purpose |
|----------|---------|
| [README](../README.md) | Project overview and feature showcase |
| [Getting Started](GETTING_STARTED.md) | Complete tutorial for beginners |
| [Quick Reference](QUICK_REFERENCE.md) | Cheatsheet for experienced users |
| [API Reference](API_REFERENCE.md) | Full API documentation |
| [Examples](EXAMPLES.md) | Practical code examples |
| [Migration Guide](MIGRATION_GUIDE.md) | Migrate from Bukkit API |
| [Folia Guide](FOLIA_GUIDE.md) | Folia-specific features |
| [Shading Guide](SHADING.md) | Build configuration |
| [Upgrade Guide](UPGRADE.md) | Version migration |
| [Troubleshooting](TROUBLESHOOTING.md) | Common issues & solutions |
| [Security](../SECURITY.md) | Security best practices |

---

## 🔨 Generate Javadoc Locally

### Generate for All Modules

```bash
./gradlew aggregateJavadoc
```

Output: `build/docs/javadoc/index.html`

### Generate for Specific Module

```bash
./gradlew :ion-api:javadoc
./gradlew :ion-database:javadoc
./gradlew :ion-economy:javadoc
./gradlew :ion-gui:javadoc
./gradlew :ion-item:javadoc
./gradlew :ion-tasks:javadoc
./gradlew :ion-ui:javadoc
./gradlew :ion-npc:javadoc
./gradlew :ion-redis:javadoc
./gradlew :ion-proxy:javadoc
```

Output: `<module>/build/docs/javadoc/index.html`

---

## 📦 Javadoc JAR

Javadoc JARs are automatically generated for each module:

```bash
./gradlew build
```

Find them in: `<module>/build/libs/<module>-1.2.6-javadoc.jar`

---

## 🔗 IDE Integration

### IntelliJ IDEA

Javadoc is automatically downloaded when you add IonAPI as a dependency via JitPack.

**Manual download:**
1. Right-click on IonAPI dependency
2. Maven → Download Documentation

**Quick access:**
- Hover over any IonAPI class/method → Javadoc appears
- Press `Ctrl+Q` (Windows) or `F1` (Mac) for quick doc

### Eclipse

1. Right-click on project → Properties
2. Java Build Path → Libraries
3. Expand IonAPI → Javadoc location
4. Edit → Enter: `https://javadoc.jitpack.io/com/github/mattbaconz/IonAPI/1.2.6/javadoc/`

### VS Code

Install "Java Extension Pack" - Javadoc is automatically fetched.

**Hover over any IonAPI class to see documentation.**

---

## 📖 Key Documentation Pages

### Core Interfaces

| Class | Description |
|-------|-------------|
| `IonPlugin` | Main plugin interface |
| `IonScheduler` | Unified scheduler (Paper/Folia) |
| `IonCommand` | Command interface |
| `CommandContext` | Command execution context |
| `IonConfig` | Configuration interface |
| `IonEvent` | Custom event base |
| `EventBus` | Event pub/sub system |

### Extended Modules

| Class | Module | Description |
|-------|--------|-------------|
| `IonItem` | ion-item | ItemStack builder |
| `IonGui` | ion-gui | GUI builder |
| `InputGui` | ion-gui | Anvil text input |
| `SignInput` | ion-gui | Sign text input |
| `IonScoreboard` | ion-ui | Scoreboard manager |
| `IonBossBar` | ion-ui | Boss bar manager |
| `TaskChain` | ion-tasks | Async/sync workflows |
| `IonDatabase` | ion-database | Database ORM |
| `IonEconomy` | ion-economy | Economy API |
| `IonRedis` | ion-redis | Redis client |
| `IonNPC` | ion-npc | Packet NPCs |
| `IonProxy` | ion-proxy | Cross-server messaging |

### Database Annotations

| Annotation | Description |
|------------|-------------|
| `@Table` | Mark class as database entity |
| `@Column` | Define database column |
| `@PrimaryKey` | Primary key field |
| `@Cacheable` | Enable entity caching |
| `@OneToMany` | One-to-many relationship |
| `@ManyToOne` | Many-to-one relationship |
| `@JoinColumn` | Foreign key column |

### Utility Classes

| Class | Description |
|-------|-------------|
| `CooldownManager` | Player cooldown management |
| `RateLimiter` | Rate limiting |
| `MessageBuilder` | MiniMessage builder |
| `TextUtil` | Text formatting utilities |
| `Metrics` | Performance metrics |

---

## 💡 Quick Links

- **Full API**: https://javadoc.jitpack.io/com/github/mattbaconz/IonAPI/1.2.6/javadoc/
- **GitHub**: https://github.com/mattbaconz/IonAPI
- **Examples**: See `examples/` folder
- **Discord**: https://discord.com/invite/VQjTVKjs46

---

## 🔍 Search Tips

When browsing Javadoc:
1. Use **Ctrl+F** to search within a page
2. Use the **search box** at top-right for classes/methods
3. Click **"All Classes"** to see complete class list
4. Use **"Index"** for alphabetical method listing

### Common Search Terms

| Looking for... | Search for... |
|----------------|---------------|
| Create items | `IonItem.builder` |
| Create GUIs | `IonGui.builder` |
| Database queries | `IonDatabase`, `QueryBuilder` |
| Async tasks | `TaskChain`, `IonScheduler` |
| Player cooldowns | `CooldownManager` |
| Rate limiting | `RateLimiter` |
| Scoreboards | `IonScoreboard` |
| Boss bars | `IonBossBar` |
| NPCs | `IonNPC.builder` |
| Economy | `IonEconomy` |
| Redis | `IonRedis`, `IonRedisBuilder` |
| Cross-server | `IonProxy`, `IonMessenger` |

---

## 🏗️ Module Structure

```
IonAPI/
├── ion-api/          Core interfaces
├── ion-core/         Base implementations
├── ion-item/         ItemStack builder
├── ion-gui/          GUI system
├── ion-ui/           Scoreboard & BossBar
├── ion-tasks/        Task chains
├── ion-database/     Database ORM
├── ion-economy/      Economy API
├── ion-redis/        Redis integration
├── ion-proxy/        Cross-server messaging
├── ion-npc/          Packet NPCs
├── ion-placeholder/  PlaceholderAPI bridge
├── ion-inject/       Dependency injection
├── ion-test/         Testing framework
├── ion-compat/       Compatibility layer
└── platforms/        Paper & Folia implementations
```

---

## 📝 Documentation Standards

IonAPI follows these Javadoc conventions:

### Classes
```java
/**
 * Brief description of what the class does.
 *
 * <p>Detailed explanation of how to use the class,
 * including common patterns and best practices.</p>
 *
 * <h3>Example:</h3>
 * <pre>{@code
 * ClassName obj = ClassName.builder()
 *     .option(value)
 *     .build();
 * }</pre>
 *
 * @since 1.0.0
 * @see RelatedClass
 */
public class ClassName { }
```

### Methods
```java
/**
 * Brief description of what the method does.
 *
 * @param paramName description of the parameter
 * @return description of the return value
 * @throws ExceptionType when this exception is thrown
 * @since version when this was added
 */
public ReturnType methodName(ParamType paramName) { }
```

---

## 🤝 Contributing to Docs

Want to improve the documentation?

1. Fork the repository
2. Edit docs in `docs/` folder
3. Follow the existing formatting
4. Submit a pull request
5. See [CONTRIBUTING.md](../CONTRIBUTING.md) for details

---

**Generated with ❤️ by IonAPI v1.2.6**
