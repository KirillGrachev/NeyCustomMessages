package com.ney.messages.service.event;

import com.ney.messages.config.ConfigKeys;
import com.ney.messages.config.ConfigManager;
import com.ney.messages.service.SoundService;
import com.ney.messages.service.TitleService;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinMessageService {

    private final ConfigManager configManager;
    private final TitleService titleService;
    private final SoundService soundService;

    public JoinMessageService(ConfigManager configManager,
                              TitleService titleService,
                              SoundService soundService) {
        this.configManager = configManager;
        this.titleService = titleService;
        this.soundService = soundService;
    }

    public void handleJoin(PlayerJoinEvent event) {

        if (!configManager.getBoolean(ConfigKeys.JOIN_ENABLED)) return;

        Player player = event.getPlayer();
        String playerName = player.getName();
        event.setJoinMessage(null);

        if (configManager.getBoolean(ConfigKeys.JOIN_MESSAGE_ENABLED)) {

            String msg = configManager.getString(ConfigKeys.JOIN_MESSAGE_FORMAT)
                    .replace("{player}", playerName);

            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', msg));

        }

        if (configManager.getBoolean(ConfigKeys.JOIN_TITLE_ENABLED)) {

            String title = configManager.getString(ConfigKeys.JOIN_TITLE_TITLE);
            String subtitle = configManager.getString(ConfigKeys.JOIN_TITLE_SUBTITLE)
                    .replace("{player}", playerName);

            int fadeIn = configManager.getInt(ConfigKeys.JOIN_TITLE_FADE_IN);
            int stay = configManager.getInt(ConfigKeys.JOIN_TITLE_STAY);
            int fadeOut = configManager.getInt(ConfigKeys.JOIN_TITLE_FADE_OUT);

            titleService.sendTitle(player, title, subtitle, fadeIn, stay, fadeOut);

        }

        if (configManager.getBoolean(ConfigKeys.JOIN_SOUND_ENABLED)) {

            String soundName = configManager.getString(ConfigKeys.JOIN_SOUND_NAME);

            float volume = configManager.getFloat(ConfigKeys.JOIN_SOUND_VOLUME);
            float pitch = configManager.getFloat(ConfigKeys.JOIN_SOUND_PITCH);

            soundService.playSound(player, soundName, volume, pitch);

        }
    }
}