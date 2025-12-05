package examples;

import com.ionapi.compat.IonCompat;
import com.ionapi.compat.collection.IonList;
import com.ionapi.compat.collection.IonMap;
import com.ionapi.compat.collection.IonSet;
import com.ionapi.compat.packet.IonPacket;
import com.ionapi.compat.version.ServerVersion;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Example demonstrating the IonCompat compatibility layer.
 * Write modern Java code that works on older JVMs and server versions.
 */
public class CompatibilityExample extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        
        // Log server info
        getLogger().info("Running on: " + ServerVersion.getVersionString());
        getLogger().info("Java version: " + IonCompat.getJavaVersion());
        getLogger().info("Supports Adventure: " + ServerVersion.supportsAdventure());
        
        // ========== Collection Factory Methods ==========
        // Instead of List.of() (Java 9+), use IonList.of()
        
        List<String> permissions = IonList.of("admin.kick", "admin.ban", "admin.mute");
        Set<String> worlds = IonSet.of("world", "world_nether", "world_the_end");
        Map<String, Integer> prices = IonMap.of(
            "diamond", 100,
            "emerald", 50,
            "gold", 25
        );
        
        // Mutable versions available too
        List<String> mutableList = IonList.mutableOf("a", "b", "c");
        mutableList.add("d"); // Works!
        
        // ========== String Utilities ==========
        // Backports of Java 11+ String methods
        
        String input = "  hello world  ";
        
        boolean blank = IonCompat.isBlank("   ");           // true
        String stripped = IonCompat.strip(input);           // "hello world"
        String repeated = IonCompat.repeat("ab", 3);        // "ababab"
        List<String> lines = IonCompat.lines("a\nb\nc");    // ["a", "b", "c"]
        String indented = IonCompat.indent("hello", 4);     // "    hello\n"
        
        // ========== Version Checks ==========
        
        if (ServerVersion.isAtLeast(1, 20)) {
            getLogger().info("Running 1.20+, can use new features!");
        }
        
        if (ServerVersion.isFolia()) {
            getLogger().info("Running on Folia - using region-aware scheduling");
        }
        
        if (ServerVersion.isPaper()) {
            getLogger().info("Running on Paper - Adventure API available");
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // ========== Version-Agnostic Packets ==========
        // Works on all server versions!
        
        // Action bar (works 1.8+)
        IonPacket.actionBar(player, "<gold>Welcome to the server!");
        
        // Title (works 1.8+)
        IonPacket.title(player, 
            "<gradient:gold:yellow>Welcome!", 
            "<gray>Enjoy your stay",
            10, 70, 20  // fadeIn, stay, fadeOut in ticks
        );
        
        // Tab list header/footer (works 1.8+)
        IonPacket.tabList(player,
            "<gold><bold>My Server\n<gray>Players: " + getServer().getOnlinePlayers().size(),
            "<gray>Visit us at example.com"
        );
        
        // Schedule title clear
        getServer().getScheduler().runTaskLater(this, () -> {
            IonPacket.clearTitle(player);
        }, 100L);
    }

    /**
     * Example: Version-specific feature usage
     */
    private void doVersionSpecificStuff(Player player) {
        if (ServerVersion.isAtLeast(1, 20, 5)) {
            // Use 1.20.5+ component items
            getLogger().info("Using modern item components");
        } else if (ServerVersion.isAtLeast(1, 16)) {
            // Use Adventure API
            getLogger().info("Using Adventure API");
        } else {
            // Use legacy methods
            getLogger().info("Using legacy methods");
        }
    }
}

/*
 * COMPATIBILITY SUMMARY:
 * 
 * IonList.of(), IonMap.of(), IonSet.of()
 *   - Backport of Java 9+ collection factories
 *   - Works on Java 8+
 * 
 * IonCompat string methods:
 *   - isBlank(), strip(), repeat(), lines(), indent()
 *   - Backport of Java 11+ String methods
 *   - Works on Java 8+
 * 
 * IonPacket:
 *   - actionBar() - Works 1.8+
 *   - title() - Works 1.8+
 *   - tabList() - Works 1.8+
 *   - Uses Adventure API on Paper 1.16.5+
 *   - Falls back to legacy methods on older servers
 * 
 * ServerVersion:
 *   - Version detection and comparison
 *   - Platform detection (Paper, Folia, Spigot)
 *   - Feature support checks
 */
