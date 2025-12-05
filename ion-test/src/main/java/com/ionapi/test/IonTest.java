package com.ionapi.test;

import com.ionapi.api.IonPlugin;
import com.ionapi.test.mock.*;
import org.jetbrains.annotations.NotNull;

/**
 * Entry point for IonAPI testing utilities.
 * Provides mock implementations of all IonAPI components for unit testing
 * without requiring a running Minecraft server.
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * public class MyPluginTest {
 *     private MockIonPlugin plugin;
 *     private MockScheduler scheduler;
 *     
 *     @BeforeEach
 *     void setup() {
 *         plugin = IonTest.createMockPlugin("TestPlugin");
 *         scheduler = plugin.getMockScheduler();
 *     }
 *     
 *     @Test
 *     void testScheduledTask() {
 *         MyService service = new MyService(plugin);
 *         service.scheduleTask();
 *         
 *         // Verify task was scheduled
 *         assertEquals(1, scheduler.getPendingTasks().size());
 *         
 *         // Execute all pending tasks
 *         scheduler.runPendingTasks();
 *         
 *         // Verify task executed
 *         assertTrue(service.wasTaskExecuted());
 *     }
 *     
 *     @Test
 *     void testConfig() {
 *         MockConfig config = plugin.getMockConfig();
 *         config.set("database.host", "localhost");
 *         
 *         MyDatabase db = new MyDatabase(plugin);
 *         assertEquals("localhost", db.getHost());
 *     }
 * }
 * }</pre>
 */
public final class IonTest {

    private IonTest() {}

    /**
     * Creates a mock IonPlugin for testing.
     * 
     * @param name the plugin name
     * @return a mock plugin instance
     */
    @NotNull
    public static MockIonPlugin createMockPlugin(@NotNull String name) {
        return new MockIonPlugin(name);
    }

    /**
     * Creates a mock scheduler for testing.
     * 
     * @return a mock scheduler instance
     */
    @NotNull
    public static MockScheduler createMockScheduler() {
        return new MockScheduler();
    }

    /**
     * Creates a mock config for testing.
     * 
     * @return a mock config instance
     */
    @NotNull
    public static MockConfig createMockConfig() {
        return new MockConfig();
    }

    /**
     * Creates a mock event bus for testing.
     * 
     * @return a mock event bus instance
     */
    @NotNull
    public static MockEventBus createMockEventBus() {
        return new MockEventBus();
    }

    /**
     * Creates a mock logger for testing.
     * 
     * @param name the logger name
     * @return a mock logger instance
     */
    @NotNull
    public static MockLogger createMockLogger(@NotNull String name) {
        return new MockLogger(name);
    }
}
