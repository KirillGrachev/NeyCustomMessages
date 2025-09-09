package com.ney.messages.service.impl;

import com.ney.messages.service.interfaces.ISoundService;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class SoundServiceImpl implements ISoundService {

    private final JavaPlugin plugin;

    public SoundServiceImpl(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void playSound(Player player, String soundName, float volume, float pitch) {

        if (player == null || !player.isOnline() || soundName == null
                || soundName.isEmpty()) {
            return;
        }

        try {

            Sound sound = Sound.valueOf(soundName.toUpperCase());
            player.playSound(player.getLocation(), sound, volume, pitch);

        } catch (IllegalArgumentException e) {
            plugin.getLogger().log(Level.WARNING, "Unknown sound name: {0}", soundName);
        }
    }
}