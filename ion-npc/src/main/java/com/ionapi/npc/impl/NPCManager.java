package com.ionapi.npc.impl;

import com.ionapi.npc.IonNPC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages NPC registration and click handling.
 */
public final class NPCManager implements Listener {

    private static final Map<Integer, PacketNPC> NPC_BY_ENTITY_ID = new ConcurrentHashMap<>();
    private static final Map<Integer, SimpleNPC> SIMPLE_NPC_BY_ENTITY_ID = new ConcurrentHashMap<>();
    private static Plugin plugin;
    private static boolean initialized = false;
    private static NPCManager instance;

    private NPCManager() {}

    /**
     * Initializes the NPC manager.
     *
     * @param owningPlugin the plugin
     */
    public static void init(@NotNull Plugin owningPlugin) {
        if (initialized) return;
        plugin = owningPlugin;
        initialized = true;
        instance = new NPCManager();
        
        Bukkit.getPluginManager().registerEvents(instance, plugin);
    }

    /**
     * Registers a packet-based NPC for click handling.
     *
     * @param npc the NPC to register
     */
    public static void register(@NotNull PacketNPC npc) {
        if (!initialized) {
            // Auto-init - try to get plugin from NPC
            try {
                java.lang.reflect.Field pluginField = PacketNPC.class.getDeclaredField("plugin");
                pluginField.setAccessible(true);
                init((Plugin) pluginField.get(npc));
            } catch (Exception ignored) {}
        }
        NPC_BY_ENTITY_ID.put(npc.getEntityId(), npc);
    }

    /**
     * Registers a simple NPC.
     *
     * @param npc the NPC to register
     */
    public static void register(@NotNull SimpleNPC npc) {
        if (!initialized) {
            try {
                java.lang.reflect.Field pluginField = SimpleNPC.class.getDeclaredField("plugin");
                pluginField.setAccessible(true);
                init((Plugin) pluginField.get(npc));
            } catch (Exception ignored) {}
        }
        SIMPLE_NPC_BY_ENTITY_ID.put(npc.getEntityId(), npc);
    }

    /**
     * Unregisters a packet-based NPC.
     *
     * @param npc the NPC to unregister
     */
    public static void unregister(@NotNull PacketNPC npc) {
        NPC_BY_ENTITY_ID.remove(npc.getEntityId());
    }

    /**
     * Unregisters a simple NPC.
     *
     * @param npc the NPC to unregister
     */
    public static void unregister(@NotNull SimpleNPC npc) {
        SIMPLE_NPC_BY_ENTITY_ID.remove(npc.getEntityId());
    }

    /**
     * Gets an NPC by entity ID.
     *
     * @param entityId the entity ID
     * @return the NPC, or null if not found
     */
    @Nullable
    public static IonNPC getByEntityId(int entityId) {
        IonNPC npc = NPC_BY_ENTITY_ID.get(entityId);
        if (npc != null) return npc;
        return SIMPLE_NPC_BY_ENTITY_ID.get(entityId);
    }

    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent event) {
        Entity entity = event.getRightClicked();
        SimpleNPC npc = SIMPLE_NPC_BY_ENTITY_ID.get(entity.getEntityId());
        // SimpleNPC handles its own events
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // NPCs handle their own join events
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // Clean up viewer lists
        UUID playerId = event.getPlayer().getUniqueId();
        // NPCs handle their own quit events
    }

    /**
     * Shuts down the NPC manager and cleans up.
     */
    public static void shutdown() {
        if (!initialized) return;
        
        NPC_BY_ENTITY_ID.values().forEach(IonNPC::destroy);
        SIMPLE_NPC_BY_ENTITY_ID.values().forEach(IonNPC::destroy);
        NPC_BY_ENTITY_ID.clear();
        SIMPLE_NPC_BY_ENTITY_ID.clear();
        
        if (instance != null) {
            HandlerList.unregisterAll(instance);
        }
        initialized = false;
    }

    /**
     * Gets all registered NPCs.
     *
     * @return collection of all NPCs
     */
    public static java.util.Collection<IonNPC> getAllNPCs() {
        java.util.List<IonNPC> all = new java.util.ArrayList<>();
        all.addAll(NPC_BY_ENTITY_ID.values());
        all.addAll(SIMPLE_NPC_BY_ENTITY_ID.values());
        return all;
    }
}
