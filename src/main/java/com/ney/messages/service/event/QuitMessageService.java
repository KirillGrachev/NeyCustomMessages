package com.ney.messages.service.event;

import com.ney.messages.config.ConfigKeys;
import com.ney.messages.config.ConfigManager;
import com.ney.messages.service.SoundService;
import com.ney.messages.service.TitleService;
import com.ney.messages.service.player.AbstractPlayerEventService;
import com.ney.messages.service.player.impl.EventConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class QuitMessageService extends AbstractPlayerEventService {

    private static final EventConfig CONFIG = new EventConfig(ConfigKeys.QUIT_ENABLED,
            ConfigKeys.QUIT_MESSAGE_ENABLED, ConfigKeys.QUIT_MESSAGE_FORMAT,
            ConfigKeys.QUIT_TITLE_ENABLED, ConfigKeys.QUIT_TITLE_TITLE,
            ConfigKeys.QUIT_TITLE_SUBTITLE, ConfigKeys.QUIT_TITLE_FADE_IN,
            ConfigKeys.QUIT_TITLE_STAY, ConfigKeys.QUIT_TITLE_FADE_OUT,
            ConfigKeys.QUIT_SOUND_ENABLED, ConfigKeys.QUIT_SOUND_NAME,
            ConfigKeys.QUIT_SOUND_VOLUME, ConfigKeys.QUIT_SOUND_PITCH
    );

    public QuitMessageService(ConfigManager configManager,
                              TitleService titleService,
                              SoundService soundService) {
        super(configManager, titleService, soundService);
    }

    public void handleQuit(@NotNull PlayerQuitEvent event) {

        Player player = event.getPlayer();
        event.setQuitMessage(null);

        handle(player, player.getName(), CONFIG, null);

    }
}