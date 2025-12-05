package examples;

import com.ionapi.npc.IonNPC;
import com.ionapi.npc.impl.NPCManager;
import com.ionapi.proxy.IonMessenger;
import com.ionapi.proxy.IonProxy;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Example demonstrating IonProxy (cross-server messaging) and IonNPC (packet-based NPCs).
 */
public class ProxyAndNPCExample extends JavaPlugin {

    private IonMessenger messenger;
    private IonNPC shopNpc;
    private IonNPC infoNpc;

    @Override
    public void onEnable() {
        // ============================================
        // CROSS-SERVER MESSAGING (IonProxy)
        // ============================================
        
        // Create a plugin messaging based messenger (works with Velocity/BungeeCord)
        messenger = IonProxy.messenger(this);
        
        // Subscribe to channels
        messenger.subscribe("my:alerts", (player, message) -> {
            getLogger().info("Alert received: " + message);
            // Broadcast to all players on this server
            getServer().broadcast(net.kyori.adventure.text.Component.text("§c[Alert] " + message));
        });
        
        messenger.subscribe("my:playerdata", (player, message) -> {
            // Handle player data sync
            String[] parts = message.split(":");
            if (parts.length >= 2) {
                String playerName = parts[0];
                String data = parts[1];
                getLogger().info("Received data for " + playerName + ": " + data);
            }
        });
        
        // For Redis-based messaging (requires Jedis library):
        // IonMessenger redisMessenger = IonProxy.redis(this, "localhost", 6379);
        // IonMessenger redisWithAuth = IonProxy.redis(this, "localhost", 6379, "password");
        
        // ============================================
        // PACKET-BASED NPCs (IonNPC)
        // ============================================
        
        // NPCs will be created when a player runs a command
        // See createNPCs() method below
    }

    @Override
    public void onDisable() {
        // Clean up messenger
        if (messenger != null) {
            messenger.close();
        }
        
        // Clean up NPCs
        if (shopNpc != null) shopNpc.destroy();
        if (infoNpc != null) infoNpc.destroy();
        
        // Or destroy all NPCs at once
        NPCManager.shutdown();
    }

    /**
     * Example: Create NPCs at spawn location.
     */
    public void createNPCs(Location spawnLocation) {
        // Shop NPC with skin and click handler
        shopNpc = IonNPC.builder(this)
            .location(spawnLocation.clone().add(5, 0, 0))
            .name("<gold><bold>Shop Keeper")
            .skin("Notch") // Fetches skin from Mojang API
            .lookAtPlayer(true) // NPC will look at nearby players
            .onClick(player -> {
                player.sendMessage("§6[Shop] §fWelcome! Opening shop...");
                // openShopGui(player);
            })
            .persistent(true) // Auto-show to players who join/enter range
            .viewDistance(48) // Visible within 48 blocks
            .build();
        
        // Info NPC with custom skin texture
        infoNpc = IonNPC.builder(this)
            .location(spawnLocation.clone().add(-5, 0, 0))
            .name("<aqua>Information")
            .skin(
                "ewogICJ0aW1lc3RhbXAiIDog...", // Base64 texture value
                "signature..." // Texture signature
            )
            .onClick(player -> {
                player.sendMessage("§b[Info] §fServer rules:");
                player.sendMessage("§71. Be respectful");
                player.sendMessage("§72. No cheating");
            })
            .build();
        
        // Show to all online players
        shopNpc.showAll();
        infoNpc.showAll();
    }

    /**
     * Example: Send cross-server messages.
     */
    public void sendAlertToAllServers(String message) {
        messenger.broadcast("my:alerts", message);
    }

    public void sendToSpecificServer(String server, String message) {
        messenger.sendToServer(server, "my:alerts", message);
    }

    public void syncPlayerData(Player player, String data) {
        messenger.broadcast("my:playerdata", player.getName() + ":" + data);
    }

    /**
     * Example: NPC manipulation.
     */
    public void manipulateNPC() {
        if (shopNpc == null) return;
        
        // Teleport NPC
        shopNpc.teleport(shopNpc.getLocation().add(0, 1, 0));
        
        // Make NPC look at a location
        shopNpc.lookAt(shopNpc.getLocation().add(10, 0, 10));
        
        // Play animation
        shopNpc.swingMainHand();
        
        // Hide from specific player
        // shopNpc.hide(player);
        
        // Show to specific player
        // shopNpc.show(player);
        
        // Check if player can see NPC
        // boolean visible = shopNpc.isVisibleTo(player);
        
        // Get all viewers
        // Collection<Player> viewers = shopNpc.getViewers();
    }
}
