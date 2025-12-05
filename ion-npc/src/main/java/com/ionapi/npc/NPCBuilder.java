package com.ionapi.npc;

import com.ionapi.npc.impl.PacketNPC;
import com.ionapi.npc.skin.SkinFetcher;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Builder implementation for creating NPCs.
 */
public class NPCBuilder implements IonNPC.Builder {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final Plugin plugin;
    private Location location;
    private Component name;
    private String skinTexture;
    private String skinSignature;
    private boolean lookAtPlayer = false;
    private Consumer<Player> clickHandler;
    private boolean persistent = true;
    private int viewDistance = 48;

    public NPCBuilder(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull IonNPC.Builder location(@NotNull Location location) {
        this.location = location.clone();
        return this;
    }

    @Override
    public @NotNull IonNPC.Builder name(@NotNull String name) {
        this.name = MINI_MESSAGE.deserialize(name);
        return this;
    }

    @Override
    public @NotNull IonNPC.Builder name(@NotNull Component name) {
        this.name = name;
        return this;
    }

    @Override
    public @NotNull IonNPC.Builder skin(@NotNull String playerName) {
        // Fetch skin asynchronously
        SkinFetcher.fetchSkin(playerName).thenAccept(skin -> {
            if (skin != null) {
                this.skinTexture = skin.texture();
                this.skinSignature = skin.signature();
            }
        });
        return this;
    }

    @Override
    public @NotNull IonNPC.Builder skin(@NotNull String texture, @NotNull String signature) {
        this.skinTexture = texture;
        this.skinSignature = signature;
        return this;
    }

    @Override
    public @NotNull IonNPC.Builder lookAtPlayer(boolean lookAt) {
        this.lookAtPlayer = lookAt;
        return this;
    }

    @Override
    public @NotNull IonNPC.Builder onClick(@NotNull Consumer<Player> handler) {
        this.clickHandler = handler;
        return this;
    }

    @Override
    public @NotNull IonNPC.Builder persistent(boolean persistent) {
        this.persistent = persistent;
        return this;
    }

    @Override
    public @NotNull IonNPC.Builder viewDistance(int distance) {
        this.viewDistance = distance;
        return this;
    }

    @Override
    public @NotNull IonNPC build() {
        if (location == null) {
            throw new IllegalStateException("NPC location must be set");
        }
        
        return new PacketNPC(
            plugin,
            location,
            name,
            skinTexture,
            skinSignature,
            lookAtPlayer,
            clickHandler,
            persistent,
            viewDistance
        );
    }
}
