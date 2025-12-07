# 📦 IonAPI Shading Guide

Complete guide for properly shading IonAPI into your Minecraft plugin to avoid conflicts.

**Version**: 1.2.0  
**Last Updated**: December 7, 2024

---

## ⚠️ Why Shading is Required

When multiple plugins use IonAPI without shading, they share the same classes. This causes:
- **Version conflicts**: Plugin A uses v1.1.0, Plugin B uses v1.2.0 → crashes
- **ClassNotFoundException**: Classes loaded by wrong plugin
- **NoSuchMethodError**: Method signatures changed between versions
- **Data corruption**: Shared static state between plugins

**Solution**: Relocate IonAPI classes to your plugin's unique package.

---

## 🚀 Gradle (Kotlin DSL) - Recommended

### build.gradle.kts (Java 21 Compatible)
```kotlin
plugins {
    java
    id("com.gradleup.shadow") version "8.3.0"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    implementation("com.github.mattbaconz:IonAPI:1.2.0")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.shadowJar {
    archiveClassifier.set("")
    
    // ⚠️ CRITICAL: Relocate to YOUR package
    relocate("com.ionapi", "com.yourname.yourplugin.libs.ionapi")
    
    // Optional: Minimize JAR size (removes unused classes)
    minimize()
}

// Make 'build' task produce the shaded JAR
tasks.build {
    dependsOn(tasks.shadowJar)
}
```

### Alternative: Gradle 8.5+ with Shadow 8.3.0
If you encounter ASM issues, ensure you're using Gradle 8.5 or higher:

```bash
# Check Gradle version
./gradlew --version

# Upgrade if needed (in gradle/wrapper/gradle-wrapper.properties)
distributionUrl=https\://services.gradle.org/distributions/gradle-8.5-bin.zip
```


### Build Command
```bash
./gradlew shadowJar
```

Output: `build/libs/YourPlugin-1.0.0.jar` (shaded)

---

## 🚀 Gradle (Groovy DSL)

### build.gradle
```groovy
plugins {
    id 'java'
    id 'com.gradleup.shadow' version '8.3.0'
}

repositories {
    mavenCentral()
    maven { url 'https://repo.papermc.io/repository/maven-public/' }
    maven { url 'https://jitpack.io' }
}

dependencies {
    compileOnly 'io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT'
    implementation 'com.github.mattbaconz:IonAPI:1.2.0'
}

shadowJar {
    archiveClassifier.set('')
    
    // ⚠️ CRITICAL: Relocate to YOUR package
    relocate 'com.ionapi', 'com.yourname.yourplugin.libs.ionapi'
    
    minimize()
}

build.dependsOn shadowJar

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
```

---

## 🚀 Maven

### pom.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.yourname</groupId>
    <artifactId>YourPlugin</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <repositories>
        <repository>
            <id>papermc</id>
            <url>https://repo.papermc.io/repository/maven-public/</url>
        </repository>
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>
    </repositories>

    <dependencies>
        <dependency>
            <groupId>io.papermc.paper</groupId>
            <artifactId>paper-api</artifactId>
            <version>1.20.4-R0.1-SNAPSHOT</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>com.github.mattbaconz</groupId>
            <artifactId>IonAPI</artifactId>
            <version>1.2.0</version>
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
                        <goals>
                            <goal>shade</goal>
                        </goals>
                        <configuration>
                            <relocations>
                                <!-- ⚠️ CRITICAL: Relocate to YOUR package -->
                                <relocation>
                                    <pattern>com.ionapi</pattern>
                                    <shadedPattern>com.yourname.yourplugin.libs.ionapi</shadedPattern>
                                </relocation>
                            </relocations>
                            <minimizeJar>true</minimizeJar>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

### Build Command
```bash
mvn package
```

Output: `target/YourPlugin-1.0.0.jar` (shaded)


---

## 📋 Relocation Examples

### Correct Relocation Patterns
```kotlin
// ✅ Good - Unique to your plugin
relocate("com.ionapi", "com.yourname.yourplugin.libs.ionapi")
relocate("com.ionapi", "me.developer.myplugin.shaded.ionapi")
relocate("com.ionapi", "net.myserver.plugin.internal.ionapi")

// ❌ Bad - Too generic, may conflict
relocate("com.ionapi", "libs.ionapi")
relocate("com.ionapi", "shaded.ionapi")
```

### Multiple Dependencies
```kotlin
tasks.shadowJar {
    // IonAPI
    relocate("com.ionapi", "${project.group}.libs.ionapi")
    
    // Other common libraries (if you use them)
    relocate("com.zaxxer.hikari", "${project.group}.libs.hikari")
    relocate("org.slf4j", "${project.group}.libs.slf4j")
}
```

---

## ✅ Verification

### Check Your Shaded JAR
After building, verify the relocation worked:

```bash
# List contents of JAR
jar tf build/libs/YourPlugin-1.0.0.jar | grep ionapi
```

**Expected output:**
```
com/yourname/yourplugin/libs/ionapi/api/IonPlugin.class
com/yourname/yourplugin/libs/ionapi/database/IonDatabase.class
...
```

**Bad output (not relocated):**
```
com/ionapi/api/IonPlugin.class
com/ionapi/database/IonDatabase.class
```

### Check JAR Size
Your shaded JAR should be larger than your source code alone:

| Component | Approximate Size |
|-----------|------------------|
| Your code | ~50-200 KB |
| IonAPI | ~273 KB |
| **Total** | ~323-473 KB |

If your JAR is too small, shading may have failed.

---

## 🔧 Troubleshooting

### Problem: ASM compatibility error with Java 21
```
java.lang.IllegalArgumentException: Unsupported class file major version
```

**Solution**: Use Shadow 8.3.0 with Gradle 8.5+:
```kotlin
// build.gradle.kts
plugins {
    id("com.gradleup.shadow") version "8.3.0"
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}
```

Check your Gradle version:
```bash
./gradlew --version
```

If below 8.5, update `gradle/wrapper/gradle-wrapper.properties`:
```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-8.5-bin.zip
```

### Problem: Classes not found at runtime
```
java.lang.NoClassDefFoundError: com/ionapi/api/IonPlugin
```

**Solution**: Ensure `implementation` (not `compileOnly`) and shadowJar is configured.

### Problem: Relocation not working
**Solution**: Check shadowJar task is actually running:
```kotlin
tasks.build {
    dependsOn(tasks.shadowJar)
}
```

### Problem: JAR too large
**Solution**: Enable minimize:
```kotlin
tasks.shadowJar {
    minimize()
}
```

### Problem: Missing classes after minimize
**Solution**: Exclude specific packages from minimization:
```kotlin
tasks.shadowJar {
    minimize {
        exclude(dependency("com.github.mattbaconz:IonAPI:.*"))
    }
}
```

### Problem: Duplicate class errors
**Solution**: You have multiple versions. Check all dependencies:
```bash
./gradlew dependencies --configuration runtimeClasspath
```

---

## 📦 What Gets Shaded

When you shade IonAPI, these packages are included:

```
com.ionapi.api.*        → com.yourplugin.libs.ionapi.api.*
com.ionapi.core.*       → com.yourplugin.libs.ionapi.core.*
com.ionapi.database.*   → com.yourplugin.libs.ionapi.database.*
com.ionapi.economy.*    → com.yourplugin.libs.ionapi.economy.*
com.ionapi.gui.*        → com.yourplugin.libs.ionapi.gui.*
com.ionapi.item.*       → com.yourplugin.libs.ionapi.item.*
com.ionapi.redis.*      → com.yourplugin.libs.ionapi.redis.*
com.ionapi.tasks.*      → com.yourplugin.libs.ionapi.tasks.*
com.ionapi.ui.*         → com.yourplugin.libs.ionapi.ui.*
... (all modules)
```

---

## 🎯 Best Practices

1. **Always relocate** - Never ship IonAPI without relocation
2. **Use unique package** - Include your name/organization
3. **Use minimize()** - Reduces JAR size by removing unused classes
4. **Test thoroughly** - Verify all features work after shading
5. **Check JAR contents** - Confirm relocation with `jar tf`
6. **Update regularly** - Keep IonAPI version current

---

## 📚 Resources

- **Shadow Plugin Docs**: https://gradleup.com/shadow/
- **Maven Shade Plugin**: https://maven.apache.org/plugins/maven-shade-plugin/
- **IonAPI GitHub**: https://github.com/mattbaconz/IonAPI
- **JitPack**: https://jitpack.io/#mattbaconz/IonAPI

---

## ✅ Complete Working Example (Java 21)

This configuration is tested and working with Java 21:

### build.gradle.kts
```kotlin
plugins {
    java
    id("com.gradleup.shadow") version "8.3.0"
}

group = "com.yourname"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    implementation("com.github.mattbaconz:IonAPI:1.2.0")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        relocate("com.ionapi", "${project.group}.libs.ionapi")
        minimize()
    }
    
    build {
        dependsOn(shadowJar)
    }
    
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(21)
    }
}
```

### gradle/wrapper/gradle-wrapper.properties
```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.5-bin.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

### Build Commands
```bash
# Clean build
./gradlew clean shadowJar

# Verify relocation
jar tf build/libs/YourPlugin-1.0.0.jar | grep ionapi
# Should show: com/yourname/libs/ionapi/...
```

---

## 🎯 Advanced Relocation Examples

### Multiple Plugins on Same Server
If you have multiple plugins using IonAPI, each MUST relocate to avoid conflicts:

**Plugin A:**
```kotlin
relocate("com.ionapi", "com.plugina.libs.ionapi")
```

**Plugin B:**
```kotlin
relocate("com.ionapi", "com.pluginb.libs.ionapi")
```

### Relocating Dependencies
If you're using optional features that require dependencies:

```kotlin
tasks.shadowJar {
    // Relocate IonAPI
    relocate("com.ionapi", "${project.group}.libs.ionapi")
    
    // If using Redis (optional)
    relocate("io.lettuce", "${project.group}.libs.lettuce")
    
    // If using custom database driver
    relocate("org.xerial", "${project.group}.libs.sqlite")
    
    minimize()
}
```

### Excluding Specific Classes
Sometimes you want to exclude certain classes from relocation:

```kotlin
tasks.shadowJar {
    relocate("com.ionapi", "${project.group}.libs.ionapi") {
        // Don't relocate API interfaces (if you want them accessible)
        exclude("com.ionapi.api.IonPlugin")
    }
}
```

---

## 🧪 Testing Relocation

### 1. Build Your Plugin
```bash
./gradlew clean shadowJar
```

### 2. Check JAR Contents
```bash
# Windows PowerShell
jar tf build/libs/YourPlugin-1.0.0.jar | Select-String "ionapi"

# Linux/Mac
jar tf build/libs/YourPlugin-1.0.0.jar | grep ionapi
```

### 3. Expected Output
```
com/yourname/libs/ionapi/api/IonPlugin.class
com/yourname/libs/ionapi/database/IonDatabase.class
com/yourname/libs/ionapi/gui/IonGui.class
...
```

### 4. Wrong Output (Not Relocated)
```
com/ionapi/api/IonPlugin.class  ❌ BAD - Will conflict!
```

---

## ⚠️ Common Relocation Mistakes

### Mistake 1: Forgetting to Relocate
```kotlin
// ❌ BAD - No relocation
tasks.shadowJar {
    archiveClassifier.set("")
}
```

**Result**: Conflicts with other plugins using IonAPI

### Mistake 2: Generic Relocation
```kotlin
// ❌ BAD - Too generic
relocate("com.ionapi", "libs.ionapi")
```

**Result**: Still conflicts if another plugin uses `libs.ionapi`

### Mistake 3: Not Using Shadow Plugin
```kotlin
// ❌ BAD - Regular JAR task
tasks.jar {
    from(configurations.runtimeClasspath.get().map { zipTree(it) })
}
```

**Result**: No relocation, classes conflict

### Correct Way
```kotlin
// ✅ GOOD
tasks.shadowJar {
    relocate("com.ionapi", "com.yourname.yourplugin.libs.ionapi")
    minimize()
}
```

---

**Need help?** Join our Discord: https://discord.com/invite/VQjTVKjs46
