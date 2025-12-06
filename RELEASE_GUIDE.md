# 📦 IonAPI v1.1.0 Release Guide

## ✅ What to Release

### **Option 1: Just Tag the Release (Recommended - Easiest!)**

JitPack will automatically build everything when you create a GitHub release.

**Steps:**
1. Go to: https://github.com/mattbaconz/IonAPI/releases/new
2. Tag: `v1.1.0`
3. Title: `IonAPI v1.1.0 - Economy, Redis, ORM Relationships`
4. Description: Copy from `V1.1.0_RELEASE_NOTES.md`
5. Click "Publish release"
6. **Done!** JitPack will build it automatically

**Developers will use:**
```kotlin
dependencies {
    implementation("com.github.mattbaconz:IonAPI:1.1.0")
}
```

JitPack automatically bundles ALL modules into one dependency!

---

### **Option 2: Upload Individual JARs (Optional)**

If you want to provide direct downloads, upload these JARs from `build/libs/`:

**Core JARs:**
- `ion-api/build/libs/ion-api-1.1.0.jar` (12 KB)
- `ion-core/build/libs/ion-core-1.1.0.jar`

**New v1.1.0 Features:**
- `ion-database/build/libs/ion-database-1.1.0.jar` (41 KB) - ORM + Caching
- `ion-economy/build/libs/ion-economy-1.1.0.jar` (14 KB) - Economy System
- `ion-redis/build/libs/ion-redis-1.1.0.jar` (9 KB) - Redis Integration

**Other Modules:**
- `ion-gui/build/libs/ion-gui-1.1.0.jar`
- `ion-item/build/libs/ion-item-1.1.0.jar`
- `ion-tasks/build/libs/ion-tasks-1.1.0.jar`
- `ion-proxy/build/libs/ion-proxy-1.1.0.jar`
- `ion-npc/build/libs/ion-npc-1.1.0.jar`
- `ion-placeholder/build/libs/ion-placeholder-1.1.0.jar`
- `ion-inject/build/libs/ion-inject-1.1.0.jar`
- `ion-test/build/libs/ion-test-1.1.0.jar`
- `ion-compat/build/libs/ion-compat-1.1.0.jar`
- `ion-ui/build/libs/ion-ui-1.1.0.jar`

---

## 🎯 Recommended: Just Tag It!

**Why?**
- ✅ JitPack handles everything automatically
- ✅ No need to upload JARs manually
- ✅ Developers get all modules in one dependency
- ✅ Automatic versioning and Maven coordinates
- ✅ Works immediately after tagging

**How developers use it:**
```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    // Gets ALL modules automatically!
    implementation("com.github.mattbaconz:IonAPI:1.1.0")
}

tasks.shadowJar {
    relocate("com.ionapi", "${project.group}.libs.ionapi")
}
```

---

## 📝 Release Checklist

- [x] Version updated to 1.1.0 in `build.gradle.kts`
- [x] CHANGELOG.md updated
- [x] All code pushed to GitHub
- [x] Build passes (`./gradlew build`)
- [ ] Create GitHub release with tag `v1.1.0`
- [ ] Copy release notes from `V1.1.0_RELEASE_NOTES.md`
- [ ] (Optional) Upload individual JARs
- [ ] Announce on Discord

---

## 🚀 After Release

1. **Test JitPack**: Visit https://jitpack.io/#mattbaconz/IonAPI/1.1.0
2. **Update README**: Ensure installation instructions show v1.1.0
3. **Announce**: Post in Discord about new features
4. **Documentation**: Update any version-specific docs

---

## 💡 For Developers

Once released, developers can use IonAPI like this:

```kotlin
plugins {
    id("com.gradleup.shadow") version "8.3.0"
}

repositories {
    maven("https://jitpack.io")
}

dependencies {
    // All-in-one dependency
    implementation("com.github.mattbaconz:IonAPI:1.1.0")
}

tasks.shadowJar {
    relocate("com.ionapi", "com.yourplugin.libs.ionapi")
}
```

**That's it!** Super easy to use.

---

## 📊 What's Included in v1.1.0

- 🔒 Security fixes (SQL injection, resource leaks)
- 💰 Economy system with Vault integration
- 🔴 Redis pub/sub and KV storage
- 🔥 Hot-reload configuration
- 📊 ORM relationships (@OneToMany, @ManyToOne)
- 💾 Entity caching (@Cacheable)
- 🛠️ Admin economy commands
- 📚 Complete examples and documentation

**Total**: 50+ new files, 3,500+ lines of code

---

**Ready to release? Just create the GitHub tag and you're done!** 🎉
