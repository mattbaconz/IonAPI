# The IonPlugin Interface

Your main class should implement `IonPlugin` instead of just extending `JavaPlugin`. This provides access to IonAPI's enhanced features like the scheduler, event bus, and config provider.

### Example Main Class

```java
package com.example.myplugin;

import com.ionapi.api.IonPlugin;
import org.jetbrains.annotations.NotNull;

public class MyPlugin implements IonPlugin {
    
    @Override
    public void onEnable() {
        getLogger().info("MyPlugin has been enabled!");
        
        // Initialize your plugin
        loadConfig();
        registerCommands();
        registerEvents();
    }
    
    @Override
    public void onDisable() {
        getLogger().info("MyPlugin has been disabled!");
        
        // Cleanup resources
        getScheduler().cancelAll();
        getConfigProvider().saveAll();
    }
    
    @Override
    public @NotNull String getName() {
        return "MyPlugin";
    }
    
    private void loadConfig() {
        // Config loading code
    }
    
    private void registerCommands() {
        // Command registration code
    }
    
    private void registerEvents() {
        // Event registration code
    }
}
```
