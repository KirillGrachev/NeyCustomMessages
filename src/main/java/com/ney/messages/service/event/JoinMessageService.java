package com.ney.messages.service.event;

import com.ney.messages.config.ConfigKeys;
import com.ney.messages.config.ConfigManager;
import com.ney.messages.service.SoundService;
import com.ney.messages.service.TitleService;
import com.ney.messages.service.player.AbstractPlayerEventService;
import com.ney.messages.service.player.impl.EventConfig;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

public class JoinMessageService extends AbstractPlayerEventService {

    private static final EventConfig CONFIG = new EventConfig(ConfigKeys.JOIN_ENABLED,
            ConfigKeys.JOIN_MESSAGE_ENABLED, ConfigKeys.JOIN_MESSAGE_FORMAT,
            ConfigKeys.JOIN_TITLE_ENABLED, ConfigKeys.JOIN_TITLE_TITLE,
            ConfigKeys.JOIN_TITLE_SUBTITLE, ConfigKeys.JOIN_TITLE_FADE_IN,
            ConfigKeys.JOIN_TITLE_STAY, ConfigKeys.JOIN_TITLE_FADE_OUT,
            ConfigKeys.JOIN_SOUND_ENABLED, ConfigKeys.JOIN_SOUND_NAME,
            ConfigKeys.JOIN_SOUND_VOLUME, ConfigKeys.JOIN_SOUND_PITCH
    );

    public JoinMessageService(ConfigManager configManager,
                              TitleService titleService,
                              SoundService soundService) {
        super(configManager, titleService, soundService);
    }

    public void handleJoin(@NotNull PlayerJoinEvent event) {

        Player player = event.getPlayer();
        event.setJoinMessage(null);

        handle(player, player.getName(), CONFIG, null);

    }
}