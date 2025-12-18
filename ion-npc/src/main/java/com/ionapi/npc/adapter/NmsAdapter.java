package com.ionapi.npc.adapter;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Adapter interface for NMS (Net Minecraft Server) operations.
 * Allows decoupling of NPC logic from specific Minecraft version implementations.
 * 
 * <p>This interface follows the Bridge pattern to allow different implementations
 * for different Minecraft versions without modifying the core NPC logic.</p>
 */
public interface NmsAdapter {

    /**
     * Creates a GameProfile object for the NPC.
     * 
     * @param uuid the UUID for the profile
     * @param name the display name (max 16 characters)
     * @return the created GameProfile object
     */
    Object createGameProfile(UUID uuid, String name);

    /**
     * Sets the skin texture on a GameProfile.
     * 
     * @param gameProfile the GameProfile to modify
     * @param texture the base64 encoded texture data
     * @param signature the texture signature
     */
    void setSkin(Object gameProfile, String texture, String signature);

    /**
     * Sends a packet to a player.
     * 
     * @param player the player to send to
     * @param packet the packet to send
     */
    void sendPacket(Player player, Object packet);

    /**
     * Creates a player info packet for adding an NPC to the player list.
     * 
     * @param gameProfile the GameProfile of the NPC
     * @param entityId the entity ID
     * @param uuid the UUID of the NPC
     * @return the player info packet
     * @throws Exception if packet creation fails
     */
    Object createPlayerInfoPacket(Object gameProfile, int entityId, UUID uuid) throws Exception;

    /**
     * Creates a spawn packet for the NPC.
     * 
     * @param entityId the entity ID
     * @param uuid the UUID of the NPC
     * @param location the spawn location
     * @return the spawn packet
     * @throws Exception if packet creation fails
     */
    Object createSpawnPacket(int entityId, UUID uuid, Location location) throws Exception;

    /**
     * Creates a head rotation packet.
     * 
     * @param entityId the entity ID
     * @param yaw the yaw angle
     * @return the head rotation packet
     * @throws Exception if packet creation fails
     */
    Object createHeadRotationPacket(int entityId, float yaw) throws Exception;

    /**
     * Creates a packet to remove the NPC from the player list.
     * 
     * @param uuid the UUID of the NPC
     * @return the player info remove packet
     * @throws Exception if packet creation fails
     */
    Object createPlayerInfoRemovePacket(UUID uuid) throws Exception;

    /**
     * Creates a packet to destroy/despawn an entity.
     * 
     * @param entityId the entity ID
     * @return the destroy packet
     * @throws Exception if packet creation fails
     */
    Object createDestroyPacket(int entityId) throws Exception;

    /**
     * Creates a teleport packet.
     * 
     * @param entityId the entity ID
     * @param location the new location
     * @return the teleport packet
     * @throws Exception if packet creation fails
     */
    Object createTeleportPacket(int entityId, Location location) throws Exception;

    /**
     * Creates a rotation packet.
     * 
     * @param entityId the entity ID
     * @param yaw the yaw angle
     * @param pitch the pitch angle
     * @return the rotation packet
     * @throws Exception if packet creation fails
     */
    Object createRotationPacket(int entityId, float yaw, float pitch) throws Exception;

    /**
     * Creates an animation packet.
     * 
     * @param entityId the entity ID
     * @param animation the animation ID (0 = swing main hand, 3 = swing off hand)
     * @return the animation packet
     * @throws Exception if packet creation fails
     */
    Object createAnimationPacket(int entityId, int animation) throws Exception;
}
