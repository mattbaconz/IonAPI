<div align="center">

# 🤝 Contributing to IonAPI

**Thank you for considering contributing to IonAPI!**

*It's people like you that make IonAPI such a great tool for the Minecraft plugin development community.*

[![Discord](https://img.shields.io/badge/Discord-Join%20Server-5865F2?style=flat-square&logo=discord&logoColor=white)](https://discord.com/invite/VQjTVKjs46)
[![GitHub](https://img.shields.io/badge/GitHub-mattbaconz-181717?style=flat-square&logo=github)](https://github.com/mattbaconz)

</div>

---

## 📋 Table of Contents

- [Code of Conduct](#-code-of-conduct)
- [How Can I Contribute?](#-how-can-i-contribute)
- [Development Setup](#-development-setup)
- [Code Style Guidelines](#-code-style-guidelines)
- [Testing](#-testing)
- [Commit Guidelines](#-commit-guidelines)

---

## 📜 Code of Conduct

This project and everyone participating in it is governed by our Code of Conduct. By participating, you are expected to uphold this code.

**Be respectful, be kind, and help build an awesome community!** 🌟

## 🎯 How Can I Contribute?

### 🐛 Reporting Bugs

Found a bug? Help us squash it!

**Before creating a bug report:**
- 🔍 Check [existing issues](https://github.com/mattbaconz/IonAPI/issues) to avoid duplicates
- 📝 Gather all relevant information

**When creating a bug report, include:**
- ✅ **Clear, descriptive title**
- 📋 **Exact steps to reproduce**
- 💻 **Code examples** (if applicable)
- 🎯 **Expected vs actual behavior**
- 🔧 **Environment details:**
  - IonAPI version
  - Server type (Paper/Folia)
  - Server version (e.g., 1.20.4)
  - Java version

**Example:**
```markdown
**Bug**: GUI items disappear after clicking

**Steps to Reproduce:**
1. Create GUI with IonGui.builder()
2. Add items with .item()
3. Click any item
4. Items vanish

**Expected**: Items should remain
**Actual**: Items disappear

**Environment:**
- IonAPI: 1.0.0
- Paper: 1.20.4
- Java: 21
```

### 💡 Suggesting Enhancements

Have an idea? We'd love to hear it!

**When suggesting enhancements:**
- 🎯 **Clear title** describing the feature
- 📝 **Detailed description** of what you want
- 💻 **Code examples** showing proposed API
- 🌟 **Use cases** explaining why it's useful
- 🔄 **Alternatives** you've considered

**Example:**
```markdown
**Feature**: Add particle effect builder

**Description**: 
Fluent API for spawning particles, similar to IonItem builder.

**Proposed API:**
```java
IonParticle.builder(Particle.FLAME)
    .location(player.getLocation())
    .count(50)
    .offset(0.5, 0.5, 0.5)
    .speed(0.1)
    .spawn();
```

**Use Case**: 
Makes particle effects easier and more readable.
```

### 🔧 Pull Requests

Ready to contribute code? Awesome!

**Steps:**
1. 🍴 **Fork** the repository
2. 🌿 **Create** your feature branch
   ```bash
   git checkout -b feature/AmazingFeature
   ```
3. ✍️ **Commit** your changes
   ```bash
   git commit -m "feat: add amazing feature"
   ```
4. 📤 **Push** to your branch
   ```bash
   git push origin feature/AmazingFeature
   ```
5. 🎉 **Open** a Pull Request

**PR Checklist:**
- [ ] Code follows style guidelines
- [ ] Self-reviewed the code
- [ ] Added comments for complex logic
- [ ] Updated documentation
- [ ] Added/updated tests
- [ ] All tests pass
- [ ] No new warnings

## 🛠️ Development Setup

### Prerequisites

Make sure you have:
- ☕ **Java 21+** - [Download](https://adoptium.net/)
- 🐘 **Gradle 8.0+** - Included via wrapper
- 🔧 **Git** - [Download](https://git-scm.com/)
- 💻 **IDE** - IntelliJ IDEA recommended

### 🚀 Quick Setup

```bash
# 1️⃣ Fork & Clone
git clone https://github.com/YOUR_USERNAME/IonAPI.git
cd IonAPI

# 2️⃣ Build the project
./gradlew build

# 3️⃣ Run tests
./gradlew test

# 4️⃣ Open in IDE
# IntelliJ: File → Open → Select IonAPI folder
```

### 🎯 Project Structure

```
IonAPI/
├── 🎯 ion-api/          # Core API interfaces
├── ⚙️ ion-core/         # Base implementations
├── 🎨 ion-item/         # Item Builder module
├── 📦 ion-gui/          # GUI System module
├── 📊 ion-ui/           # UI Components module
├── 🔗 ion-tasks/        # Task Chains module
├── 💾 ion-database/     # Database ORM module
└── 🖥️ platforms/        # Platform implementations
    ├── ion-paper/       # Paper support
    └── ion-folia/       # Folia support
```

## 🎨 Code Style Guidelines

### ✨ Java Code Style

**Formatting:**
- 📏 **Indentation**: 4 spaces (no tabs)
- 📐 **Line Length**: Max 120 characters
- 🔲 **Braces**: Opening brace on same line
- 🎯 **Imports**: Organize and remove unused

**Naming Conventions:**
- 📦 **Classes**: `PascalCase`
- 🔧 **Methods/Variables**: `camelCase`
- 🔒 **Constants**: `UPPER_SNAKE_CASE`
- 🎭 **Interfaces**: `PascalCase` (prefix with `I` for API interfaces)

### 📝 Example

```java
public class PlayerManager implements IPlayerManager {
    
    private static final int MAX_PLAYERS = 100;
    private final Map<UUID, PlayerData> playerCache;
    
    public PlayerManager() {
        this.playerCache = new HashMap<>();
    }
    
    public void addPlayer(UUID uuid, PlayerData data) {
        if (uuid != null && data != null) {
            playerCache.put(uuid, data);
        }
    }
}
```

### 📚 Documentation

**All public APIs need JavaDoc!**

**Include:**
- 📝 Clear description
- 📋 `@param` for parameters
- 🎯 `@return` for return values
- ⚠️ `@throws` for exceptions
- 💡 Usage examples for complex APIs

**Example:**
```java
/**
 * Schedules a task to run after a delay.
 * <p>
 * This method is thread-safe and works on both Paper and Folia.
 * On Folia, the task runs on the global region scheduler.
 *
 * @param task the task to run
 * @param delay the delay before execution
 * @param unit the time unit of the delay
 * @return the scheduled task handle
 * @throws IllegalArgumentException if delay is negative
 * 
 * @example
 * <pre>{@code
 * scheduler.runLater(() -> {
 *     player.sendMessage("5 seconds passed!");
 * }, 5, TimeUnit.SECONDS);
 * }</pre>
 */
@NotNull
IonTask runLater(@NotNull Runnable task, long delay, @NotNull TimeUnit unit);
```

## 🧪 Testing

### ✅ Writing Tests

**Guidelines:**
- ✍️ Write unit tests for new features
- 🎯 Test edge cases and error conditions
- 📊 Aim for >80% code coverage
- 🚀 Keep tests fast and focused

**Example:**
```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ItemBuilderTest {
    
    @Test
    void testItemCreation() {
        ItemStack item = IonItem.builder(Material.DIAMOND_SWORD)
            .name("<red>Test Sword")
            .build();
        
        assertNotNull(item);
        assertEquals(Material.DIAMOND_SWORD, item.getType());
    }
    
    @Test
    void testItemWithEnchantments() {
        ItemStack item = IonItem.builder(Material.DIAMOND_SWORD)
            .enchant(Enchantment.SHARPNESS, 5)
            .build();
        
        assertTrue(item.containsEnchantment(Enchantment.SHARPNESS));
        assertEquals(5, item.getEnchantmentLevel(Enchantment.SHARPNESS));
    }
}
```

### 🏃 Running Tests

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests ItemBuilderTest

# Run with coverage report
./gradlew test jacocoTestReport
```

## Project Structure

```
IonAPI/
├── ion-api/              # Core API interfaces
│   └── src/main/java/com/ionapi/api/
│       ├── scheduler/    # Scheduler API
│       ├── command/      # Command API
│       ├── config/       # Configuration API
│       ├── event/        # Event API
│       └── util/         # Utilities
├── ion-core/             # Base implementations
├── platforms/            # Platform-specific implementations
│   ├── ion-paper/        # Paper implementation
│   └── ion-folia/        # Folia implementation
└── docs/                 # Documentation
```

## Adding New Features

### 1. Design Phase

- Create an issue describing the feature
- Discuss the API design in the issue
- Get feedback from maintainers

### 2. Implementation Phase

- Create interfaces in `ion-api`
- Implement in `ion-core` if platform-agnostic
- Add platform-specific code in `ion-paper`/`ion-folia`
- Write comprehensive JavaDoc
- Add unit tests

### 3. Documentation Phase

- Update README.md if needed
- Add examples to docs/EXAMPLES.md
- Update docs/API_REFERENCE.md
- Add migration guide if breaking changes

## 📝 Commit Guidelines

### 🎯 Commit Message Format

```
<type>(<scope>): <subject>

[optional body]

[optional footer]
```

### 🏷️ Types

| Type | Description | Emoji |
|------|-------------|-------|
| `feat` | New feature | ✨ |
| `fix` | Bug fix | 🐛 |
| `docs` | Documentation | 📝 |
| `style` | Code style/formatting | 💄 |
| `refactor` | Code refactoring | ♻️ |
| `perf` | Performance improvement | ⚡ |
| `test` | Tests | ✅ |
| `chore` | Maintenance | 🔧 |

### ✨ Examples

**Good commits:**
```bash
✨ feat(scheduler): add cron-style scheduling support
🐛 fix(config): handle null values in getString()
📝 docs(readme): update installation instructions
♻️ refactor(gui): simplify click handler logic
⚡ perf(item): optimize builder performance
✅ test(tasks): add task chain unit tests
```

**Bad commits:**
```bash
❌ fixed stuff
❌ update
❌ changes
❌ wip
```

### 💡 Tips

- ✅ Use present tense ("add" not "added")
- ✅ Use imperative mood ("move" not "moves")
- ✅ Keep subject line under 50 characters
- ✅ Capitalize subject line
- ✅ Don't end subject with period
- ✅ Separate subject from body with blank line

## Release Process

1. Update version in `build.gradle.kts`
2. Update CHANGELOG.md
3. Create release tag
4. Build and publish artifacts

## 💬 Questions & Support

Need help? We're here for you!

<div align="center">

[![Discord](https://img.shields.io/badge/Discord-Join%20Server-5865F2?style=for-the-badge&logo=discord&logoColor=white)](https://discord.com/invite/VQjTVKjs46)
[![GitHub Issues](https://img.shields.io/badge/GitHub-Issues-181717?style=for-the-badge&logo=github)](https://github.com/mattbaconz/IonAPI/issues)
[![GitHub Discussions](https://img.shields.io/badge/GitHub-Discussions-181717?style=for-the-badge&logo=github)](https://github.com/mattbaconz/IonAPI/discussions)

</div>

**Where to ask:**
- 💬 **Discord** - Quick questions, general chat
- 🐛 **GitHub Issues** - Bug reports, feature requests
- 💡 **GitHub Discussions** - Ideas, questions, showcase

---

## 📜 License

By contributing, you agree that your contributions will be licensed under the [MIT License](LICENSE).

---

## 🙏 Thank You!

Every contribution, no matter how small, makes IonAPI better for everyone!

**Contributors are awesome!** 🌟

<div align="center">

Made with ❤️ by the IonAPI community

[🏠 Back to README](README.md) • [📚 Documentation](docs/GETTING_STARTED.md) • [💬 Discord](https://discord.com/invite/VQjTVKjs46)

</div>
