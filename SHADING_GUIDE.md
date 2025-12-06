# 📦 IonAPI Shading Guide

## ✅ Yes, IonAPI is Designed for Easy Shading!

IonAPI is **specifically built** to be easily shadable into your plugins. Here's everything you need to know.

---

## 🎯 Two Ways to Use IonAPI

### Option 1: Individual Modules (Recommended for Small Plugins)

Pick only the modules you need:

```kotlin
dependencies {
    // Core (always needed)
    implementation("com.github.mattbaconz.IonAPI:ion-api:1.1.0")
    implementation("com.github.mattbaconz.IonAPI:ion-core:1.1.0")
    
    // Add only what you need
    implementation("com.github.mattbaconz.IonAPI:ion-database:1.1.0")
    implementation("com.github.mattbaconz.IonAPI:ion-economy:1.1.0")
    implementation("com.github.mattbaconz.IonAPI:ion-gui:1.1.0")
}
```

**Pros**: Smaller JAR size, only include what you use  
**Cons**: Need to specify each module

---

### Option 2: All-in-One JAR (Easiest)

Use the complete IonAPI bundle:

```kotlin
dependencies {
    // Everything in one dependency!
    implementation("com.github.mattbaconz:IonAPI:1.1.0")
}
```

**Pros**: Single dependency, includes everything  
**Cons**: Larger JAR size (~500KB)

---

## 🔧 Complete Shading Setup

### build.gradle.kts (Full Example)

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
    // Paper API
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    
    // IonAPI - All-in-one
    implementation("com.github.mattbaconz:IonAPI:1.1.0")
    
    // OR individual modules:
    // implementation("com.github.mattbaconz.IonAPI:ion-api:1.1.0")
    // implementation("com.github.mattbaconz.IonAPI:ion-database:1.1.0")
    // implementation("com.github.mattbaconz.IonAPI:ion-economy:1.1.0")
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        
        // ⚠️ CRITICAL: Always relocate to avoid conflicts!
        relocate("com.ionapi", "${project.group}.libs.ionapi")
        
        // Relocate third-party libraries
        relocate("io.lettuce", "${project.group}.libs.lettuce")
        
        // Optional: Minimize JAR size
        minimize()
        
        // Exclude unnecessary files
        exclude("META-INF/*.SF")
        exclude("META-INF/*.DSA")
        exclude("META-INF/*.RSA")
    }
    
    build {
        dependsOn(shadowJar)
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}
```

---

## 🚀 Build Your Plugin

```bash
./gradlew shadowJar
```

Your plugin JAR will be in: `build/libs/YourPlugin-1.0.0.jar`

---

## 📊 JAR Size Comparison

| Configuration | Approximate Size |
|--------------|------------------|
| Core only (ion-api + ion-core) | ~50 KB |
| + Database | ~100 KB |
| + Economy | ~120 KB |
| + GUI + Items | ~150 KB |
| **All modules** | **~500 KB** |
| With Redis (includes Lettuce) | ~2 MB |

---

## ⚠️ Important: Always Relocate!

**Why?** If two plugins use different versions of IonAPI without relocation, they'll conflict.

```kotlin
// ✅ GOOD - Relocates to your package
relocate("com.ionapi", "com.yourplugin.libs.ionapi")

// ❌ BAD - No relocation = conflicts!
// (Don't do this)
```

---

## 🎯 Module Dependencies

If using individual modules, here are the dependencies:

```
ion-api (core)
├── ion-core (platform abstraction)
├── ion-database (requires: ion-api, ion-core)
├── ion-economy (requires: ion-api, ion-database)
├── ion-redis (requires: ion-api)
├── ion-gui (requires: ion-api, ion-core, ion-item)
├── ion-item (requires: ion-api, ion-core)
├── ion-tasks (requires: ion-api, ion-core)
├── ion-proxy (requires: ion-api, ion-core)
├── ion-npc (requires: ion-api, ion-core)
├── ion-placeholder (requires: ion-api, ion-core)
├── ion-inject (requires: ion-api)
├── ion-test (requires: ion-api, ion-core)
└── ion-compat (requires: ion-api)
```

---

## 💡 Recommended Configurations

### Minimal Plugin (Commands + Config)
```kotlin
implementation("com.github.mattbaconz.IonAPI:ion-api:1.1.0")
implementation("com.github.mattbaconz.IonAPI:ion-core:1.1.0")
```
**Size**: ~50 KB

### Database Plugin
```kotlin
implementation("com.github.mattbaconz.IonAPI:ion-api:1.1.0")
implementation("com.github.mattbaconz.IonAPI:ion-core:1.1.0")
implementation("com.github.mattbaconz.IonAPI:ion-database:1.1.0")
```
**Size**: ~100 KB

### GUI Plugin
```kotlin
implementation("com.github.mattbaconz.IonAPI:ion-api:1.1.0")
implementation("com.github.mattbaconz.IonAPI:ion-core:1.1.0")
implementation("com.github.mattbaconz.IonAPI:ion-gui:1.1.0")
implementation("com.github.mattbaconz.IonAPI:ion-item:1.1.0")
```
**Size**: ~80 KB

### Economy Plugin
```kotlin
implementation("com.github.mattbaconz.IonAPI:ion-api:1.1.0")
implementation("com.github.mattbaconz.IonAPI:ion-core:1.1.0")
implementation("com.github.mattbaconz.IonAPI:ion-database:1.1.0")
implementation("com.github.mattbaconz.IonAPI:ion-economy:1.1.0")
```
**Size**: ~120 KB

### Full-Featured Plugin (Everything)
```kotlin
implementation("com.github.mattbaconz:IonAPI:1.1.0")
```
**Size**: ~500 KB (or ~2 MB with Redis)

---

## 🔍 Verify Shading

After building, check your JAR:

```bash
# Windows
jar tf build/libs/YourPlugin-1.0.0.jar | findstr ionapi

# Linux/Mac
jar tf build/libs/YourPlugin-1.0.0.jar | grep ionapi
```

You should see paths like:
```
com/yourplugin/libs/ionapi/api/IonPlugin.class
com/yourplugin/libs/ionapi/database/IonDatabase.class
```

If you see `com/ionapi/` instead, relocation didn't work!

---

## ✅ Summary

**Yes, IonAPI is VERY easy to shade!**

1. Add Shadow plugin
2. Add IonAPI dependency (all-in-one or individual modules)
3. Add relocation rule
4. Run `./gradlew shadowJar`
5. Done!

**Total setup time**: < 2 minutes

---

## 📞 Need Help?

- **Discord**: https://discord.com/invite/VQjTVKjs46
- **GitHub Issues**: https://github.com/mattbaconz/IonAPI/issues
- **Examples**: See `examples/` folder in the repository
