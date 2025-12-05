package com.ionapi.gui;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a sound configuration for GUI events.
 */
public class GuiSound {

    public static final GuiSound CLICK = new GuiSound(Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    public static final GuiSound SUCCESS = new GuiSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
    public static final GuiSound ERROR = new GuiSound(Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
    public static final GuiSound OPEN = new GuiSound(Sound.BLOCK_CHEST_OPEN, 0.5f, 1.2f);
    public static final GuiSound CLOSE = new GuiSound(Sound.BLOCK_CHEST_CLOSE, 0.5f, 1.2f);
    public static final GuiSound PAGE_TURN = new GuiSound(Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
    public static final GuiSound DENIED = new GuiSound(Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
    public static final GuiSound PURCHASE = new GuiSound(Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.5f);
    public static final GuiSound NONE = null;

    private final Sound sound;
    private final float volume;
    private final float pitch;

    public GuiSound(@NotNull Sound sound, float volume, float pitch) {
        this.sound = sound;
        this.volume = Math.max(0, Math.min(2, volume));
        this.pitch = Math.max(0.5f, Math.min(2, pitch));
    }

    public static GuiSound of(@NotNull Sound sound) {
        return new GuiSound(sound, 1.0f, 1.0f);
    }

    public static GuiSound of(@NotNull Sound sound, float volume) {
        return new GuiSound(sound, volume, 1.0f);
    }

    public static GuiSound of(@NotNull Sound sound, float volume, float pitch) {
        return new GuiSound(sound, volume, pitch);
    }

    public void play(@NotNull Player player) {
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    public @NotNull Sound getSound() { return sound; }
    public float getVolume() { return volume; }
    public float getPitch() { return pitch; }
}
