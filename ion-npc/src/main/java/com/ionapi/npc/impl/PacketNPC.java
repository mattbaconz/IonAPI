package com.ionapi.npc.impl;

import com.ionapi.npc.IonNPC;
import com.ionapi.npc.skin.SkinData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Packet-based NPC implementation using reflection for NMS access.
 * Works across multiple Minecraft versions.
 */
public class PacketNPC implements IonNPC, Listener {

    private static final AtomicInteger ENTITY_ID_COUNTER = new AtomicInteger(-1000);
    private static final ReflectionHelper REFLECT = new ReflectionHelper();

    private final Plugin plugin;
    private final UUID uuid;
    private final int entityId;
    private final Object gameProfile;
    private final Set<UUID> viewers = ConcurrentHashMap.newKeySet();
    private final Consumer<Player> clickHandler;
    private final boolean persistent;
    private final int viewDistance;
    private final boolean lookAtPlayer;

    private Location location;
    private Component name;
    private boolean destroyed = false;

    public PacketNPC(
        @NotNull Plugin plugin,
        @NotNull Location location,
        @Nullable Component name,
        @Nullable String skinTexture,
        @Nullable String skinSignature,
        boolean lookAtPlayer,
        @Nullable Consumer<Player> clickHandler,
        boolean persistent,
        int viewDistance
    ) {
        this.plugin = plugin;
        this.location = location.clone();
        this.name = name;
        this.lookAtPlayer = lookAtPlayer;
        this.clickHandler = clickHandler;
        this.persistent = persistent;
        this.viewDistance = viewDistance;
        
        this.uuid = UUID.randomUUID();
        this.entityId = ENTITY_ID_COUNTER.getAndDecrement();
        
        // Create game profile
        String displayName = name != null 
            ? PlainTextComponentSerializer.plainText().serialize(name) 
            : "NPC";
        if (displayName.length() > 16) displayName = displayName.substring(0, 16);
        
        this.gameProfile = REFLECT.createGameProfile(uuid, displayName);
        
        // Apply skin
        String texture = skinTexture != null ? skinTexture : SkinData.STEVE.texture();
        String signature = skinSignature != null ? skinSignature : SkinData.STEVE.signature();
        REFLECT.setSkin(gameProfile, texture, signature);
        
        // Register listener for persistent NPCs
        if (persistent) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
        }
        
        // Register with NPC manager for click handling
        NPCManager.register(this);
    }

    @Override
    public @NotNull UUID getUniqueId() {
        return uuid;
    }

    @Override
    public int getEntityId() {
        return entityId;
    }

    @Override
    public @NotNull Location getLocation() {
        return location.clone();
    }

    @Override
    public @Nullable Component getName() {
        return name;
    }

    @Override
    public void show(@NotNull Player player) {
        if (destroyed) return;
        if (viewers.contains(player.getUniqueId())) return;
        
        try {
            // Send player info packet
            Object playerInfoPacket = REFLECT.createPlayerInfoPacket(gameProfile, entityId, uuid);
            REFLECT.sendPacket(player, playerInfoPacket);
            
            // Send spawn packet
            Object spawnPacket = REFLECT.createSpawnPacket(entityId, uuid, location);
            REFLECT.sendPacket(player, spawnPacket);
            
            // Send head rotation
            Object headRotationPacket = REFLECT.createHeadRotationPacket(entityId, location.getYaw());
            REFLECT.sendPacket(player, headRotationPacket);
            
            // Remove from tab list after delay
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!destroyed && player.isOnline()) {
                    try {
                        Object removePacket = REFLECT.createPlayerInfoRemovePacket(uuid);
                        REFLECT.sendPacket(player, removePacket);
                    } catch (Exception ignored) {}
                }
            }, 20L);
            
            viewers.add(player.getUniqueId());
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to show NPC to " + player.getName() + ": " + e.getMessage());
        }
    }

    @Override
    public void show(@NotNull Collection<? extends Player> players) {
        players.forEach(this::show);
    }

    @Override
    public void showAll() {
        Bukkit.getOnlinePlayers().forEach(this::show);
    }

    @Override
    public void hide(@NotNull Player player) {
        if (!viewers.remove(player.getUniqueId())) return;
        if (!player.isOnline()) return;
        
        try {
            Object destroyPacket = REFLECT.createDestroyPacket(entityId);
            REFLECT.sendPacket(player, destroyPacket);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to hide NPC from " + player.getName() + ": " + e.getMessage());
        }
    }

    @Override
    public void hide(@NotNull Collection<? extends Player> players) {
        players.forEach(this::hide);
    }

    @Override
    public void hideAll() {
        new HashSet<>(viewers).stream()
            .map(Bukkit::getPlayer)
            .filter(Objects::nonNull)
            .forEach(this::hide);
    }

    @Override
    public void teleport(@NotNull Location location) {
        this.location = location.clone();
        
        for (UUID viewerId : viewers) {
            Player player = Bukkit.getPlayer(viewerId);
            if (player == null) continue;
            
            try {
                Object teleportPacket = REFLECT.createTeleportPacket(entityId, location);
                REFLECT.sendPacket(player, teleportPacket);
            } catch (Exception ignored) {}
        }
    }

    @Override
    public void lookAt(@NotNull Location target) {
        double dx = target.getX() - location.getX();
        double dy = target.getY() - location.getY();
        double dz = target.getZ() - location.getZ();
        
        double dist = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float) Math.toDegrees(-Math.atan2(dy, dist));
        
        location.setYaw(yaw);
        location.setPitch(pitch);
        
        sendRotation(yaw, pitch);
    }

    @Override
    public void lookAt(@NotNull Player target) {
        lookAt(target.getEyeLocation());
    }

    private void sendRotation(float yaw, float pitch) {
        for (UUID viewerId : viewers) {
            Player player = Bukkit.getPlayer(viewerId);
            if (player == null) continue;
            
            try {
                Object rotationPacket = REFLECT.createRotationPacket(entityId, yaw, pitch);
                REFLECT.sendPacket(player, rotationPacket);
                
                Object headPacket = REFLECT.createHeadRotationPacket(entityId, yaw);
                REFLECT.sendPacket(player, headPacket);
            } catch (Exception ignored) {}
        }
    }

    @Override
    public void swingMainHand() {
        sendAnimation(0);
    }

    @Override
    public void swingOffHand() {
        sendAnimation(3);
    }

    private void sendAnimation(int animationId) {
        for (UUID viewerId : viewers) {
            Player player = Bukkit.getPlayer(viewerId);
            if (player == null) continue;
            
            try {
                Object animPacket = REFLECT.createAnimationPacket(entityId, animationId);
                REFLECT.sendPacket(player, animPacket);
            } catch (Exception ignored) {}
        }
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            showAll();
        } else {
            hideAll();
        }
    }

    @Override
    public boolean isVisibleTo(@NotNull Player player) {
        return viewers.contains(player.getUniqueId());
    }

    @Override
    public @NotNull Collection<Player> getViewers() {
        return viewers.stream()
            .map(Bukkit::getPlayer)
            .filter(Objects::nonNull)
            .toList();
    }

    @Override
    public void destroy() {
        if (destroyed) return;
        destroyed = true;
        
        hideAll();
        viewers.clear();
        
        if (persistent) {
            HandlerList.unregisterAll(this);
        }
        
        NPCManager.unregister(this);
    }

    @Override
    public boolean isDestroyed() {
        return destroyed;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!persistent || destroyed) return;
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (event.getPlayer().isOnline() && isInRange(event.getPlayer())) {
                show(event.getPlayer());
            }
        }, 10L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        viewers.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!persistent || destroyed) return;
        
        Player player = event.getPlayer();
        boolean inRange = isInRange(player);
        boolean isViewer = viewers.contains(player.getUniqueId());
        
        if (inRange && !isViewer) {
            show(player);
        } else if (!inRange && isViewer) {
            hide(player);
        } else if (inRange && isViewer && lookAtPlayer) {
            lookAt(player);
        }
    }

    private boolean isInRange(Player player) {
        if (!player.getWorld().equals(location.getWorld())) return false;
        return player.getLocation().distanceSquared(location) <= viewDistance * viewDistance;
    }

    public void handleClick(@NotNull Player player) {
        if (clickHandler != null) {
            clickHandler.accept(player);
        }
    }
}
