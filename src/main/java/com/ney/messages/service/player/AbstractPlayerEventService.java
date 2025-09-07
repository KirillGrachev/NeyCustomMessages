package com.ney.messages.service.player;

import com.ney.messages.config.ConfigManager;
import com.ney.messages.service.SoundService;
import com.ney.messages.service.TitleService;

import com.ney.messages.service.player.impl.EventConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;

public abstract class AbstractPlayerEventService {

    protected final ConfigManager configManager;
    protected final TitleService titleService;
    protected final SoundService soundService;

    public AbstractPlayerEventService(ConfigManager configManager,
                                      TitleService titleService,
                                      SoundService soundService) {
        this.configManager = configManager;
        this.titleService = titleService;
        this.soundService = soundService;
    }

    protected void handle(Player player,
                          String playerName,
                          @NotNull EventConfig cfg,
                          String deathCause) {

        if (!configManager.getBooleanWithValidation(cfg.enabled(), false)) return;

        if (configManager.getBooleanWithValidation(cfg.messageEnabled(), false)) {

            String msg = configManager.getStringWithColor(cfg.messageFormat(), "")
                    .replace("{player}", playerName);

            if (deathCause != null) {
                msg = msg.replace("{death_cause}", deathCause);
            }

            Bukkit.broadcastMessage(msg);

        }

        if (configManager.getBooleanWithValidation(cfg.titleEnabled(), false)) {

            String title = configManager.getStringWithColor(cfg.title(), "");
            String subtitle = configManager.getStringWithColor(cfg.subtitle(), "")
                    .replace("{player}", playerName);

            if (deathCause != null) {
                subtitle = subtitle.replace("{death_cause}", deathCause);
            }

            int fadeIn = configManager.getIntWithValidation(cfg.fadeIn(), 15, 0, 100);
            int stay = configManager.getIntWithValidation(cfg.stay(), 30, 0, 200);
            int fadeOut = configManager.getIntWithValidation(cfg.fadeOut(), 15, 0, 100);

            titleService.sendTitle(player, title, subtitle, fadeIn, stay, fadeOut);

        }

        if (configManager.getBooleanWithValidation(cfg.soundEnabled(), false)) {

            String soundName = configManager.getString(cfg.soundName(), "");

            float volume = configManager.getFloatWithValidation(cfg.soundVolume(),
                    1.0f, 0.0f, 1.0f);

            float pitch = configManager.getFloatWithValidation(cfg.soundPitch(),
                    1.0f, 0.0f, 2.0f);

            soundService.playSound(player, soundName, volume, pitch);

        }
    }
}