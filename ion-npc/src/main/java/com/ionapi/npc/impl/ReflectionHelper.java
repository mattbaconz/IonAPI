package com.ionapi.npc.impl;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

/**
 * Reflection helper for NMS packet operations.
 * Supports multiple Minecraft versions.
 */
public class ReflectionHelper {

    private final String version;
    private final boolean isModern; // 1.17+
    
    // Cached classes
    private Class<?> craftPlayerClass;
    private Class<?> gameProfileClass;
    private Class<?> propertyClass;
    private Class<?> packetClass;
    private Method sendPacketMethod;
    private Method getHandleMethod;
    private Field connectionField;
    
    public ReflectionHelper() {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        this.version = packageName.split("\\.").length > 3 ? packageName.split("\\.")[3] : "";
        this.isModern = version.isEmpty() || !version.startsWith("v1_16") && !version.startsWith("v1_15");
        
        try {
            initClasses();
        } catch (Exception e) {
            Bukkit.getLogger().warning("[IonNPC] Failed to initialize reflection: " + e.getMessage());
        }
    }

    private void initClasses() throws Exception {
        // CraftPlayer
        String craftBukkitPackage = "org.bukkit.craftbukkit" + (version.isEmpty() ? "" : "." + version);
        craftPlayerClass = Class.forName(craftBukkitPackage + ".entity.CraftPlayer");
        getHandleMethod = craftPlayerClass.getMethod("getHandle");
        
        // GameProfile (Mojang authlib)
        gameProfileClass = Class.forName("com.mojang.authlib.GameProfile");
        propertyClass = Class.forName("com.mojang.authlib.properties.Property");
    }

    public Object createGameProfile(UUID uuid, String name) {
        try {
            Constructor<?> constructor = gameProfileClass.getConstructor(UUID.class, String.class);
            return constructor.newInstance(uuid, name);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create GameProfile", e);
        }
    }

    public void setSkin(Object gameProfile, String texture, String signature) {
        try {
            Method getProperties = gameProfileClass.getMethod("getProperties");
            Object propertyMap = getProperties.invoke(gameProfile);
            
            Constructor<?> propConstructor = propertyClass.getConstructor(String.class, String.class, String.class);
            Object property = propConstructor.newInstance("textures", texture, signature);
            
            Method put = propertyMap.getClass().getMethod("put", Object.class, Object.class);
            put.invoke(propertyMap, "textures", property);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set skin", e);
        }
    }

    public void sendPacket(Player player, Object packet) {
        try {
            Object handle = getHandleMethod.invoke(craftPlayerClass.cast(player));
            Object connection = getConnection(handle);
            
            Method sendMethod = findSendMethod(connection.getClass());
            sendMethod.invoke(connection, packet);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send packet", e);
        }
    }

    private Object getConnection(Object serverPlayer) throws Exception {
        for (Field field : serverPlayer.getClass().getFields()) {
            if (field.getType().getSimpleName().contains("Connection") || 
                field.getType().getSimpleName().contains("PlayerConnection")) {
                return field.get(serverPlayer);
            }
        }
        // Try common field names
        try {
            return serverPlayer.getClass().getField("connection").get(serverPlayer);
        } catch (NoSuchFieldException e) {
            return serverPlayer.getClass().getField("playerConnection").get(serverPlayer);
        }
    }

    private Method findSendMethod(Class<?> connectionClass) throws Exception {
        for (Method method : connectionClass.getMethods()) {
            if ((method.getName().equals("send") || method.getName().equals("sendPacket")) 
                && method.getParameterCount() == 1) {
                return method;
            }
        }
        throw new NoSuchMethodException("Could not find send method");
    }

    public Object createPlayerInfoPacket(Object gameProfile, int entityId, UUID uuid) throws Exception {
        // This is version-specific and complex - simplified stub
        // In production, you'd need version-specific implementations
        Class<?> packetClass = getNMSClass("ClientboundPlayerInfoUpdatePacket");
        // Implementation varies by version
        return createDummyPacket();
    }

    public Object createSpawnPacket(int entityId, UUID uuid, Location loc) throws Exception {
        Class<?> packetClass = getNMSClass("ClientboundAddEntityPacket");
        return createDummyPacket();
    }

    public Object createHeadRotationPacket(int entityId, float yaw) throws Exception {
        Class<?> packetClass = getNMSClass("ClientboundRotateHeadPacket");
        return createDummyPacket();
    }

    public Object createPlayerInfoRemovePacket(UUID uuid) throws Exception {
        Class<?> packetClass = getNMSClass("ClientboundPlayerInfoRemovePacket");
        return createDummyPacket();
    }

    public Object createDestroyPacket(int entityId) throws Exception {
        Class<?> packetClass = getNMSClass("ClientboundRemoveEntitiesPacket");
        return createDummyPacket();
    }

    public Object createTeleportPacket(int entityId, Location loc) throws Exception {
        Class<?> packetClass = getNMSClass("ClientboundTeleportEntityPacket");
        return createDummyPacket();
    }

    public Object createRotationPacket(int entityId, float yaw, float pitch) throws Exception {
        Class<?> packetClass = getNMSClass("ClientboundMoveEntityPacket$Rot");
        return createDummyPacket();
    }

    public Object createAnimationPacket(int entityId, int animation) throws Exception {
        Class<?> packetClass = getNMSClass("ClientboundAnimatePacket");
        return createDummyPacket();
    }

    private Class<?> getNMSClass(String name) throws ClassNotFoundException {
        if (isModern) {
            return Class.forName("net.minecraft.network.protocol.game." + name);
        } else {
            return Class.forName("net.minecraft.server." + version + "." + name);
        }
    }

    private Object createDummyPacket() {
        // Placeholder - actual implementation requires version-specific code
        return null;
    }
}
