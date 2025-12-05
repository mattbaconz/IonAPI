package com.ionapi.npc.impl;

import com.ionapi.npc.IonNPC;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Simple armor stand based NPC for basic use cases.
 * Does not require NMS but has limited features (no player skin).
 * Use PacketNPC for full player-like NPCs.
 */
public class SimpleNPC implements IonNPC, Listener {

    private final Plugin plugin;
    private final UUID uuid;
    private final Set<UUID> viewers = ConcurrentHashMap.newKeySet();
    private final Consumer<Player> clickHandler;
    private final boolean persistent;
    private final int viewDistance;

    private Location location;
    private Component name;
    private ArmorStand entity;
    private boolean destroyed = false;

    public SimpleNPC(
        @NotNull Plugin plugin,
        @NotNull Location location,
        @Nullable Component name,
        @Nullable Consumer<Player> clickHandler,
        boolean persistent,
        int viewDistance
    ) {
        this.plugin = plugin;
        this.location = location.clone();
        this.name = name;
        this.clickHandler = clickHandler;
        this.persistent = persistent;
        this.viewDistance = viewDistance;
        this.uuid = UUID.randomUUID();
        
        // Spawn the armor stand
        spawnEntity();
        
        if (persistent) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
        }
    }

    private void spawnEntity() {
        if (location.getWorld() == null) return;
        
        entity = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        entity.setVisible(false);
        entity.setGravity(false);
        entity.setInvulnerable(true);
        entity.setMarker(true);
        entity.setCollidable(false);
        entity.setPersistent(false);
        
        if (name != null) {
            entity.customName(name);
            entity.setCustomNameVisible(true);
        }
    }

    @Override
    public @NotNull UUID getUniqueId() {
        return uuid;
    }

    @Override
    public int getEntityId() {
        return entity != null ? entity.getEntityId() : -1;
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
        viewers.add(player.getUniqueId());
        // Armor stand is always visible to all, this just tracks who "should" see it
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
        viewers.remove(player.getUniqueId());
    }

    @Override
    public void hide(@NotNull Collection<? extends Player> players) {
        players.forEach(this::hide);
    }

    @Override
    public void hideAll() {
        viewers.clear();
    }

    @Override
    public void teleport(@NotNull Location location) {
        this.location = location.clone();
        if (entity != null && entity.isValid()) {
            entity.teleport(location);
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
        
        if (entity != null && entity.isValid()) {
            entity.setRotation(yaw, pitch);
        }
    }

    @Override
    public void lookAt(@NotNull Player target) {
        lookAt(target.getEyeLocation());
    }

    @Override
    public void swingMainHand() {
        // Not supported for armor stands
    }

    @Override
    public void swingOffHand() {
        // Not supported for armor stands
    }

    @Override
    public void setVisible(boolean visible) {
        if (entity != null) {
            entity.setVisible(visible);
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
        
        if (entity != null && entity.isValid()) {
            entity.remove();
        }
        
        viewers.clear();
        
        if (persistent) {
            HandlerList.unregisterAll(this);
        }
    }

    @Override
    public boolean isDestroyed() {
        return destroyed;
    }

    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent event) {
        if (entity == null || !event.getRightClicked().equals(entity)) return;
        
        event.setCancelled(true);
        if (clickHandler != null) {
            clickHandler.accept(event.getPlayer());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (persistent && !destroyed) {
            viewers.add(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        viewers.remove(event.getPlayer().getUniqueId());
    }
}
