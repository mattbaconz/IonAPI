package examples;

import com.ionapi.api.IonPlugin;
import com.ionapi.api.config.IonConfig;
import com.ionapi.api.scheduler.IonScheduler;
import com.ionapi.test.IonTest;
import com.ionapi.test.mock.*;

/**
 * Example demonstrating how to use IonTest for unit testing.
 * 
 * Add to your test dependencies:
 * testImplementation(project(":ion-test"))
 */
public class TestingExample {

    // ========== Example Service to Test ==========
    
    public static class PlayerService {
        private final IonPlugin plugin;
        private final IonConfig config;
        private boolean initialized = false;
        
        public PlayerService(IonPlugin plugin) {
            this.plugin = plugin;
            this.config = plugin.getConfigProvider().getConfig();
        }
        
        public void initialize() {
            // Schedule a repeating task
            plugin.getScheduler().runTimer(() -> {
                // Cleanup task
                plugin.getLogger().info("Running cleanup...");
            }, 0, 60, java.util.concurrent.TimeUnit.SECONDS);
            
            initialized = true;
        }
        
        public int getMaxPlayers() {
            return config.getInt("max-players", 100);
        }
        
        public boolean isInitialized() {
            return initialized;
        }
    }

    // ========== Unit Tests ==========
    
    /**
     * Example test using JUnit 5 (add @Test annotation in real tests)
     */
    public void testPlayerServiceInitialization() {
        // Create mock plugin
        MockIonPlugin plugin = IonTest.createMockPlugin("TestPlugin");
        
        // Configure mock
        plugin.getMockConfig().set("max-players", 50);
        
        // Create service
        PlayerService service = new PlayerService(plugin);
        service.initialize();
        
        // Verify initialization
        assert service.isInitialized() : "Service should be initialized";
        assert service.getMaxPlayers() == 50 : "Max players should be 50";
        
        // Verify scheduler was used
        MockScheduler scheduler = plugin.getMockScheduler();
        assert scheduler.getPendingTasks().size() == 1 : "Should have 1 scheduled task";
        
        // Verify task is repeating
        MockTask task = scheduler.getPendingTasks().get(0);
        assert task.isRepeating() : "Task should be repeating";
        
        // Execute the task
        scheduler.runPendingTasks();
        
        // Verify logging
        assert plugin.getMockLogger().hasMessage("Running cleanup...") : "Should log cleanup message";
    }
    
    /**
     * Example test for scheduler timing
     */
    public void testSchedulerTiming() {
        MockScheduler scheduler = IonTest.createMockScheduler();
        
        final boolean[] executed = {false};
        
        // Schedule task for 20 ticks later (1 second)
        scheduler.runLater(() -> executed[0] = true, 1, java.util.concurrent.TimeUnit.SECONDS);
        
        // Task should not execute immediately
        scheduler.runPendingTasks();
        assert !executed[0] : "Task should not execute yet";
        
        // Advance time
        scheduler.advanceTicks(19);
        assert !executed[0] : "Task should not execute at tick 19";
        
        scheduler.advanceTicks(1);
        assert executed[0] : "Task should execute at tick 20";
    }
    
    /**
     * Example test for config
     */
    public void testConfig() {
        MockConfig config = IonTest.createMockConfig();
        
        // Set nested values
        config.set("database.host", "localhost");
        config.set("database.port", 3306);
        config.set("features.enabled", true);
        
        // Verify values
        assert "localhost".equals(config.getString("database.host"));
        assert config.getInt("database.port") == 3306;
        assert config.getBoolean("features.enabled");
        
        // Test defaults
        assert config.getInt("missing.value", 42) == 42;
        
        // Verify save tracking
        config.save();
        assert config.getSaveCount() == 1;
    }
    
    /**
     * Example test for events
     */
    public void testEventBus() {
        MockEventBus eventBus = IonTest.createMockEventBus();
        
        final String[] receivedMessage = {null};
        
        // Subscribe to event
        eventBus.subscribe(TestEvent.class, event -> {
            receivedMessage[0] = event.getMessage();
        });
        
        // Fire event
        eventBus.fire(new TestEvent("Hello!"));
        
        // Verify
        assert "Hello!".equals(receivedMessage[0]);
        assert eventBus.wasFired(TestEvent.class);
        assert eventBus.getFiredEvents(TestEvent.class).size() == 1;
    }
    
    // Simple test event
    public static class TestEvent implements com.ionapi.api.event.IonEvent {
        private final String message;
        
        public TestEvent(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
    }
}
