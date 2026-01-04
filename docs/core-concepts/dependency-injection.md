# Dependency Injection

IonAPI includes a lightweight dependency injection container to manage your plugin's services.

### Basic Usage

Initialize the injector in your `onEnable`:

```java
public class MyPlugin extends JavaPlugin {
    private IonInjector injector;
    
    @Override
    public void onEnable() {
        injector = IonInjector.create(this)
            .register(PlayerService.class)
            .register(EconomyService.class)
            .register(IonConfig.class, () -> getConfig())
            .build();
            
        // Create a command with dependencies injected
        MyCommand cmd = injector.create(MyCommand.class);
    }
}
```

### Injection Types

**Constructor Injection (Recommended):**
```java
public class MyCommand implements CommandExecutor {
    private final PlayerService playerService;
    
    @Inject
    public MyCommand(PlayerService playerService) {
        this.playerService = playerService;
    }
}
```

**Field Injection:**
```java
public class MyListener implements Listener {
    @Inject
    private EconomyService economyService;
}
```

### Named Bindings

You can register named instances for multiple objects of the same type:
```java
injector = IonInjector.create(this)
    .registerNamed("sql", sqlDatabase)
    .registerNamed("mongo", mongoDatabase)
    .build();

public class Service {
    @Inject("sql")
    private Database sqlDb;
}
```
