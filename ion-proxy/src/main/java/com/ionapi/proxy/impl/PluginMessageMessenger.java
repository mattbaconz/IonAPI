package com.ionapi.proxy.impl;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.ionapi.proxy.IonMessenger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * Plugin Messaging implementation for Velocity/BungeeCord networks.
 */
public class PluginMessageMessenger implements IonMessenger, PluginMessageListener {

    private static final String BUNGEECORD_CHANNEL = "BungeeCord";
    
    private final Plugin plugin;
    private final Map<String, BiConsumer<Player, String>> stringHandlers = new ConcurrentHashMap<>();
    private final Map<String, BiConsumer<Player, byte[]>> rawHandlers = new ConcurrentHashMap<>();
    private boolean registered = false;

    public PluginMessageMessenger(@NotNull Plugin plugin) {
        this.plugin = plugin;
        registerChannels();
    }

    private void registerChannels() {
        if (registered) return;
        Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, BUNGEECORD_CHANNEL);
        Bukkit.getMessenger().registerIncomingPluginChannel(plugin, BUNGEECORD_CHANNEL, this);
        registered = true;
    }

    @Override
    public @NotNull IonMessenger subscribe(@NotNull String channel, @NotNull BiConsumer<@Nullable Player, String> handler) {
        stringHandlers.put(channel, handler);
        return this;
    }

    @Override
    public @NotNull IonMessenger unsubscribe(@NotNull String channel) {
        stringHandlers.remove(channel);
        rawHandlers.remove(channel);
        return this;
    }

    @Override
    public void send(@NotNull Player player, @NotNull String channel, @NotNull String message) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Forward");
        out.writeUTF("ONLINE"); // Send to player's current server
        out.writeUTF(channel);
        
        byte[] msgBytes = message.getBytes();
        out.writeShort(msgBytes.length);
        out.write(msgBytes);
        
        player.sendPluginMessage(plugin, BUNGEECORD_CHANNEL, out.toByteArray());
    }

    @Override
    public void sendToServer(@NotNull String server, @NotNull String channel, @NotNull String message) {
        Player player = getAnyPlayer();
        if (player == null) return;
        
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Forward");
        out.writeUTF(server);
        out.writeUTF(channel);
        
        byte[] msgBytes = message.getBytes();
        out.writeShort(msgBytes.length);
        out.write(msgBytes);
        
        player.sendPluginMessage(plugin, BUNGEECORD_CHANNEL, out.toByteArray());
    }

    @Override
    public void broadcast(@NotNull String channel, @NotNull String message) {
        Player player = getAnyPlayer();
        if (player == null) return;
        
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Forward");
        out.writeUTF("ALL");
        out.writeUTF(channel);
        
        byte[] msgBytes = message.getBytes();
        out.writeShort(msgBytes.length);
        out.write(msgBytes);
        
        player.sendPluginMessage(plugin, BUNGEECORD_CHANNEL, out.toByteArray());
    }

    @Override
    public void sendRaw(@NotNull Player player, @NotNull String channel, byte[] data) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Forward");
        out.writeUTF("ALL");
        out.writeUTF(channel);
        out.writeShort(data.length);
        out.write(data);
        
        player.sendPluginMessage(plugin, BUNGEECORD_CHANNEL, out.toByteArray());
    }

    @Override
    public @NotNull IonMessenger subscribeRaw(@NotNull String channel, @NotNull BiConsumer<@Nullable Player, byte[]> handler) {
        rawHandlers.put(channel, handler);
        return this;
    }

    @Override
    public boolean isConnected() {
        return registered && !Bukkit.getOnlinePlayers().isEmpty();
    }

    @Override
    public void close() {
        if (registered) {
            Bukkit.getMessenger().unregisterOutgoingPluginChannel(plugin, BUNGEECORD_CHANNEL);
            Bukkit.getMessenger().unregisterIncomingPluginChannel(plugin, BUNGEECORD_CHANNEL, this);
            registered = false;
        }
        stringHandlers.clear();
        rawHandlers.clear();
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message) {
        if (!channel.equals(BUNGEECORD_CHANNEL)) return;
        
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();
        
        // Handle string messages
        BiConsumer<Player, String> stringHandler = stringHandlers.get(subchannel);
        if (stringHandler != null) {
            short len = in.readShort();
            byte[] msgBytes = new byte[len];
            in.readFully(msgBytes);
            String msg = new String(msgBytes);
            
            Bukkit.getScheduler().runTask(plugin, () -> stringHandler.accept(player, msg));
            return;
        }
        
        // Handle raw messages
        BiConsumer<Player, byte[]> rawHandler = rawHandlers.get(subchannel);
        if (rawHandler != null) {
            short len = in.readShort();
            byte[] data = new byte[len];
            in.readFully(data);
            
            Bukkit.getScheduler().runTask(plugin, () -> rawHandler.accept(player, data));
        }
    }

    @Nullable
    private Player getAnyPlayer() {
        return Bukkit.getOnlinePlayers().stream().findFirst().orElse(null);
    }
}
