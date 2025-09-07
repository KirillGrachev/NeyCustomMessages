package com.ney.messages.service;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SoundService {

    private final JavaPlugin plugin;

    public SoundService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void playSound(Player player, String soundName,
                          float volume, float pitch) {

        if (player == null || !player.isOnline()) return;

        try {

            Sound sound = Sound.valueOf(soundName.toUpperCase());
            player.playSound(player.getLocation(), sound, volume, pitch);

        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Неизвестный звук: " + soundName + ". Проверьте конфигурацию.");
        }
    }
}