package com.ney.messages.service.impl;

import com.ney.messages.NeyCustomMessages;
import com.ney.messages.service.interfaces.ISoundService;
import com.ney.messages.util.LoggerHelper;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class SoundServiceImpl implements ISoundService {

    private final LoggerHelper loggerHelper;

    public SoundServiceImpl(@NotNull NeyCustomMessages plugin) {
        this.loggerHelper = plugin.getLoggerHelper();
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
            loggerHelper.log(Level.WARNING, "Unknown sound name: {0}", soundName);
        }
    }
}