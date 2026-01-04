# Database Setup

IonAPI provides a fluent ORM-like database interface.

### Connecting

```java
IonDatabase db = IonDatabase.builder()
    .type(DatabaseType.MYSQL)
    .host("localhost")
    .port(3306)
    .database("server_db")
    .username("admin")
    .password("secret")
    .build();

db.connect();
```

### SQLite (Flat File)

```java
IonDatabase db = IonDatabase.sqlite("plugins/MyPlugin/data.db");
db.connect();
```

### Defining Entities

Use annotations to map classes to tables:

```java
@Table("players")
public class PlayerData {
    @PrimaryKey
    private UUID uuid;
    
    @Column("username")
    private String name;
    
    private double balance;
    
    // Must have no-arg constructor
    public PlayerData() {}
}
```

### Basic Operations

```java
// Save
db.save(new PlayerData(uuid, "Steve", 100.0));

// Find
PlayerData data = db.find(PlayerData.class, uuid);

// Async Find
db.findAsync(PlayerData.class, uuid).thenAccept(opt -> {
    if (opt.isPresent()) {
        // ...
    }
});
```
