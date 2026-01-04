# Configuration

### Create config.yml

Create a `config.yml` in your plugin's resources folder:

```yaml
# config.yml
prefix: "<gray>[<green>MyPlugin<gray>]"

messages:
  welcome: "<green>Welcome to the server!"
  goodbye: "<red>See you later!"

database:
  enabled: true
  host: "localhost"
  port: 3306
  name: "mydb"

features:
  - economy
  - shops
  - teleports
```

### Load Configuration

```java
private void loadConfig() {
    IonConfig config = getConfigProvider().getConfig();
    
    // Read values
    String prefix = config.getString("prefix", "<gray>[<green>MyPlugin<gray>]");
    String welcome = config.getString("messages.welcome");
    boolean dbEnabled = config.getBoolean("database.enabled");
    
    if (dbEnabled) {
        String host = config.getString("database.host");
        int port = config.getInt("database.port");
        connectToDatabase(host, port);
    }
    
    // Read list
    List<String> features = config.getStringList("features");
    getLogger().info("Enabled features: " + String.join(", ", features));
}
```

### Save Configuration

```java
// Modify values
config.set("last-updated", System.currentTimeMillis());
config.set("player-count", Bukkit.getOnlinePlayers().size());

// Save to disk
config.save();

// Reload from disk
config.reload();
```
