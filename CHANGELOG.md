# 📋 Changelog

All notable changes to IonAPI will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.2.0] - 2024-12-07

### ⚡ Performance
- **Reflection Caching**: Entity metadata is now cached, providing 10-50x faster ORM operations
- **Batch Operations**: New `BatchOperation` API for efficient bulk insert/update/delete
- **Ultra-Lightweight**: HikariCP and Redis Lettuce are `compileOnly`, minimize() removes unused classes
- **Modular**: Users only include what they need, Paper provides common dependencies

### ✨ Added
- **CooldownManager**: Thread-safe player cooldown management
  - Named cooldown managers for different actions
  - Automatic cleanup of expired cooldowns
  - Time unit flexibility (seconds, minutes, etc.)
- **RateLimiter**: Sliding window rate limiting
  - Prevent spam and abuse
  - Per-player rate limits
  - Configurable window and request limits
- **MessageBuilder**: Fluent MiniMessage builder
  - Placeholder support
  - Title/subtitle/actionbar sending
  - Reusable message templates
  - Broadcast support
- **IonScoreboard**: Easy scoreboard creation
  - MiniMessage formatting
  - Dynamic placeholders
  - Per-player scoreboards
  - Auto-update support
- **IonBossBar**: Boss bar management
  - MiniMessage formatting
  - Progress updates
  - Color and style changes
  - Named bars for retrieval
- **Metrics**: Lightweight performance monitoring
  - Counters for events
  - Timing statistics
  - Gauge values
  - Min/max/average tracking
- **BatchOperation**: Bulk database operations
  - Batch insert/update/delete
  - Configurable batch size
  - Async execution
  - Performance statistics
- **ReflectionCache**: Entity metadata caching
  - Automatic field discovery
  - Column name resolution
  - Primary key detection

### 📚 Documentation
- New `V120FeaturesExample.java` demonstrating all new features
- Updated Javadocs for all new classes
- Javadoc generation with `./gradlew aggregateJavadoc`

---

## [1.1.0] - 2024-12-06

### 🔒 Security
- Fixed SQL injection vulnerability in `QueryBuilderImpl` - all column names, operators, and ORDER BY directions are now sanitized
- Fixed resource leak in `TransactionImpl` - connection is now properly closed if `setAutoCommit(false)` fails

### ✨ Added
- **ion-economy**: Complete economy system with Vault integration
  - `EconomyProvider` - Core async economy interface
  - `Currency` - Multi-currency support with formatting
  - `TransactionResult` - Detailed transaction outcomes
  - `EconomyAccount` - Database entity with BigDecimal precision
  - `IonEconomy` - Static API entry point with fluent transactions
  - `IonEconomyVaultHook` - Full Vault compatibility bridge
- GitHub Actions CI/CD pipeline for automated builds and tests
- Comprehensive input validation for database queries
- New `EconomyExample.java` demonstrating economy usage
- **ORM Relationships**: Entity relationship annotations
  - `@OneToMany` - One-to-many relationships with lazy/eager loading
  - `@ManyToOne` - Many-to-one relationships
  - `@JoinColumn` - Foreign key column specification
  - `FetchType` - EAGER/LAZY loading strategies
  - `CascadeType` - Cascade operations (ALL, PERSIST, MERGE, REMOVE, REFRESH)
- **Entity Caching**: Reduce database queries with in-memory caching
  - `@Cacheable` - Mark entities for caching with TTL and max size
  - `EntityCache` - Thread-safe cache with automatic expiration
  - `CacheManager` - Centralized cache management
- New `AdvancedDatabaseExample.java` showing relationships, caching, and transactions
- **ion-redis**: Redis integration module
  - `IonRedis` - Async Redis client interface
  - `IonRedisBuilder` - Fluent builder for Redis connections
  - Pub/sub messaging support
  - Key-value storage with TTL
  - Connection statistics and health monitoring
- **Hot-Reload Config**: Auto-reload configuration on file changes
  - `HotReloadConfig` - WatchService-based config monitoring
  - Multiple reload handlers support
  - Real-time config updates without server restart
- **Economy Admin Commands**: Debug commands for economy system
  - `/ion eco set <player> <amount>` - Set player balance
  - `/ion eco give <player> <amount>` - Give money to player
  - `/ion eco debug <player>` - View raw database state
- New `HotReloadExample.java` demonstrating config hot-reloading
- JitPack configuration for Maven Central publication
- "IonAPI vs Others" comparison table in README

---

## [1.0.0] - 2024-12-06

### ✨ Added
- **ion-api**: Core plugin API with lifecycle management
- **ion-core**: Platform abstraction layer (Bukkit, Folia, Velocity, BungeeCord)
- **ion-database**: Async ORM with annotations (`@Table`, `@Column`, `@Id`)
- **ion-tasks**: Unified scheduler supporting Bukkit and Folia
- **ion-gui**: Inventory GUI framework with pagination
- **ion-placeholder**: PlaceholderAPI integration
- **ion-npc**: NPC creation and management
- **ion-proxy**: Cross-server messaging (Velocity/BungeeCord)
- **ion-inject**: Lightweight dependency injection
- **ion-test**: Testing utilities and mocks
- **ion-compat**: Version compatibility helpers
- **ion-item**: ItemStack builder with MiniMessage support
- **ion-ui**: Text component utilities

### 📚 Documentation
- Comprehensive API reference
- Getting started guide
- Migration guide from other libraries
- Folia compatibility guide
- Quick reference cheat sheet

---

## 📊 Version Comparison

| Version | Release Date | Key Features | Size |
|---------|--------------|--------------|------|
| 1.2.0 | 2024-12-07 | Performance optimizations, 8 new utilities | 273 KB |
| 1.1.0 | 2024-12-06 | Economy, Redis, ORM relationships, Security fixes | 252 KB |
| 1.0.0 | 2024-12-06 | Initial release with 13 modules | 230 KB |

---

## 🔗 Links

- **Repository**: https://github.com/mattbaconz/IonAPI
- **JitPack**: https://jitpack.io/#mattbaconz/IonAPI
- **Discord**: https://discord.com/invite/VQjTVKjs46
- **Documentation**: https://github.com/mattbaconz/IonAPI/tree/main/docs

---

[Unreleased]: https://github.com/mattbaconz/IonAPI/compare/1.2.0...HEAD
[1.2.0]: https://github.com/mattbaconz/IonAPI/compare/1.1.0...1.2.0
[1.1.0]: https://github.com/mattbaconz/IonAPI/compare/1.0.0...1.1.0
[1.0.0]: https://github.com/mattbaconz/IonAPI/releases/tag/1.0.0
