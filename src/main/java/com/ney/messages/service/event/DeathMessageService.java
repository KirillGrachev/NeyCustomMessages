package com.ney.messages.service.event;

import com.ney.messages.NeyCustomMessages;
import com.ney.messages.config.ConfigKeys;
import com.ney.messages.config.ConfigManager;
import com.ney.messages.service.SoundService;
import com.ney.messages.service.TitleService;
import com.ney.messages.service.player.AbstractPlayerEventService;
import com.ney.messages.service.player.impl.EventConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;

public class DeathMessageService extends AbstractPlayerEventService {

    private static final EventConfig CONFIG = new EventConfig(ConfigKeys.DEATH_ENABLED,
            ConfigKeys.DEATH_MESSAGE_ENABLED, ConfigKeys.DEATH_MESSAGE_FORMAT,
            ConfigKeys.DEATH_TITLE_ENABLED, ConfigKeys.DEATH_TITLE_TITLE,
            ConfigKeys.DEATH_TITLE_SUBTITLE, ConfigKeys.DEATH_TITLE_FADE_IN,
            ConfigKeys.DEATH_TITLE_STAY, ConfigKeys.DEATH_TITLE_FADE_OUT,
            ConfigKeys.DEATH_SOUND_ENABLED, ConfigKeys.DEATH_SOUND_NAME,
            ConfigKeys.DEATH_SOUND_VOLUME, ConfigKeys.DEATH_SOUND_PITCH
    );

    private final NeyCustomMessages plugin;

    public DeathMessageService(NeyCustomMessages plugin,
                               ConfigManager configManager,
                               TitleService titleService,
                               SoundService soundService) {
        super(configManager, titleService, soundService);
        this.plugin = plugin;
    }

    public void handleDeath(@NotNull PlayerDeathEvent event) {

        Player player = event.getEntity();
        String deathCause = event.getDeathMessage() != null
                ? event.getDeathMessage() : "неизвестно";

        event.setDeathMessage(null);

        handle(player, player.getName(), CONFIG, deathCause);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {

            if (player.isDead()) {
                player.spigot().respawn();
            }

        }, 1L);

    }
}