# 📋 Changelog

All notable changes to IonAPI will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.0] - Unreleased

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

[Unreleased]: https://github.com/mattbaconz/IonAPI/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/mattbaconz/IonAPI/releases/tag/v1.0.0
