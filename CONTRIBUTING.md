# Contributing to IonAPI

First off, thank you for considering contributing to IonAPI! It's people like you that make IonAPI such a great tool.

## Code of Conduct

This project and everyone participating in it is governed by our Code of Conduct. By participating, you are expected to uphold this code.

## How Can I Contribute?

### Reporting Bugs

Before creating bug reports, please check existing issues to avoid duplicates. When you create a bug report, include as many details as possible:

- **Use a clear and descriptive title**
- **Describe the exact steps to reproduce the problem**
- **Provide specific examples**
- **Describe the behavior you observed**
- **Explain which behavior you expected to see instead**
- **Include server version (Paper/Folia), Java version, and IonAPI version**

### Suggesting Enhancements

Enhancement suggestions are tracked as GitHub issues. When creating an enhancement suggestion:

- **Use a clear and descriptive title**
- **Provide a detailed description of the suggested enhancement**
- **Provide specific examples to demonstrate the enhancement**
- **Explain why this enhancement would be useful**

### Pull Requests

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## Development Setup

### Prerequisites

- Java 21 or higher
- Gradle 9.0+
- Git

### Building the Project

```bash
# Clone your fork
git clone https://github.com/YOUR_USERNAME/IonAPI.git
cd IonAPI

# Build the project
./gradlew build

# Run tests
./gradlew test
```

## Code Style Guidelines

### Java Code Style

- **Indentation**: 4 spaces (no tabs)
- **Line Length**: Max 120 characters
- **Braces**: Opening brace on same line
- **Naming Conventions**:
  - Classes: `PascalCase`
  - Methods/Variables: `camelCase`
  - Constants: `UPPER_SNAKE_CASE`
  - Interfaces: `PascalCase` (prefix with `I` for API interfaces)

### Example

```java
public class MyClass implements IMyInterface {
    
    private static final int MAX_VALUE = 100;
    private final String playerName;
    
    public MyClass(String playerName) {
        this.playerName = playerName;
    }
    
    public void doSomething() {
        if (playerName != null) {
            // Do something
        }
    }
}
```

### Documentation

- All public APIs must have JavaDoc comments
- Include `@param`, `@return`, and `@throws` where applicable
- Provide usage examples for complex APIs

```java
/**
 * Schedules a task to run after a delay.
 *
 * @param task the task to run
 * @param delay the delay before execution
 * @param unit the time unit of the delay
 * @return the scheduled task handle
 * @throws IllegalArgumentException if delay is negative
 */
@NotNull
IonTask runLater(@NotNull Runnable task, long delay, @NotNull TimeUnit unit);
```

## Testing

### Writing Tests

- Write unit tests for new features
- Ensure all tests pass before submitting PR
- Aim for >80% code coverage

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MyFeatureTest {
    
    @Test
    void testFeature() {
        MyFeature feature = new MyFeature();
        assertEquals(expected, feature.doSomething());
    }
}
```

### Running Tests

```bash
./gradlew test
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

## Commit Message Guidelines

Format: `<type>(<scope>): <subject>`

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks

**Examples:**
```
feat(scheduler): add support for cron-style scheduling
fix(config): handle null values in getString()
docs(readme): update installation instructions
```

## Release Process

1. Update version in `build.gradle.kts`
2. Update CHANGELOG.md
3. Create release tag
4. Build and publish artifacts

## Questions?

Feel free to ask questions in:
- GitHub Issues
- GitHub Discussions
- Discord Server (if available)

## License

By contributing, you agree that your contributions will be licensed under the MIT License.
