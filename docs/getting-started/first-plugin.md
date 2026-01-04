# Your First Plugin

Let's create a simple plugin with a command, configuration, and scheduled task.

### Step 1: Create a Hello Command

```java
package com.example.myplugin.commands;

import com.ionapi.api.command.CommandContext;
import com.ionapi.api.command.IonCommand;
import org.jetbrains.annotations.NotNull;

public class HelloCommand implements IonCommand {
    
    @Override
    public boolean execute(@NotNull CommandContext ctx) {
        // Get optional argument with default value
        String name = ctx.getArg(0, "World");
        
        // Send colored message using MiniMessage format
        ctx.reply("<green>Hello, <bold>" + name + "</bold>!");
        
        return true;
    }
    
    @Override
    public @NotNull String getName() {
        return "hello";
    }
    
    @Override
    public @NotNull String getDescription() {
        return "Sends a greeting message";
    }
    
    @Override
    public @NotNull String getUsage() {
        return "/hello [name]";
    }
    
    @Override
    public @NotNull String getPermission() {
        return "myplugin.hello";
    }
}
```

### Step 2: Register the Command

```java
private void registerCommands() {
    getCommandRegistry().register(new HelloCommand());
}
```

### Step 3: Test Your Plugin

1. Build your plugin: `./gradlew build`
2. Copy JAR to server's `plugins/` folder
3. Start server
4. Run `/hello` or `/hello Steve`
