# 📚 IonAPI Javadoc Guide

## 🌐 Online Javadoc

**Latest version**: https://javadoc.jitpack.io/com/github/mattbaconz/IonAPI/1.2.6/javadoc/

Browse the complete API documentation online!

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

### Eclipse

1. Right-click on project → Properties
2. Java Build Path → Libraries
3. Expand IonAPI → Javadoc location
4. Edit → Enter: `https://javadoc.jitpack.io/com/github/mattbaconz/IonAPI/1.2.6/javadoc/`

### VS Code

Install "Java Extension Pack" - Javadoc is automatically fetched.

---

## 📖 Key Documentation Pages

### Core API
- **IonPlugin** - Main plugin interface
- **IonDatabase** - Database ORM
- **IonEconomy** - Economy system
- **IonRedis** - Redis integration
- **HotReloadConfig** - Config hot-reloading

### Annotations
- **@Table** - Mark entity classes
- **@Column** - Define columns
- **@PrimaryKey** - Primary key fields
- **@Cacheable** - Enable entity caching
- **@OneToMany** / **@ManyToOne** - Relationships

### Builders
- **IonItem.builder()** - ItemStack builder
- **IonGui.builder()** - GUI builder
- **IonRedisBuilder** - Redis client builder
- **TaskChain.create()** - Task chain builder

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

---

**Generated with ❤️ by IonAPI v1.2.6**
