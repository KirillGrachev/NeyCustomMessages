package com.ney.messages.service.event;

import com.ney.messages.config.ConfigKeys;
import com.ney.messages.config.ConfigManager;
import com.ney.messages.service.SoundService;
import com.ney.messages.service.TitleService;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitMessageService {

    private final ConfigManager configManager;
    private final TitleService titleService;
    private final SoundService soundService;

    public QuitMessageService(ConfigManager configManager,
                              TitleService titleService,
                              SoundService soundService) {
        this.configManager = configManager;
        this.titleService = titleService;
        this.soundService = soundService;
    }

    public void handleQuit(PlayerQuitEvent event) {

        if (!configManager.getBoolean(ConfigKeys.QUIT_ENABLED)) return;

        Player player = event.getPlayer();
        String playerName = player.getName();
        event.setQuitMessage(null);

        if (configManager.getBoolean(ConfigKeys.QUIT_MESSAGE_ENABLED)) {

            String msg = configManager.getString(ConfigKeys.QUIT_MESSAGE_FORMAT)
                    .replace("{player}", playerName);

            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', msg));

        }

        if (configManager.getBoolean(ConfigKeys.QUIT_TITLE_ENABLED)) {

            String title = configManager.getString(ConfigKeys.QUIT_TITLE_TITLE);
            String subtitle = configManager.getString(ConfigKeys.QUIT_TITLE_SUBTITLE)
                    .replace("{player}", playerName);

            int fadeIn = configManager.getInt(ConfigKeys.QUIT_TITLE_FADE_IN);
            int stay = configManager.getInt(ConfigKeys.QUIT_TITLE_STAY);
            int fadeOut = configManager.getInt(ConfigKeys.QUIT_TITLE_FADE_OUT);

            titleService.sendTitle(player, title, subtitle, fadeIn, stay, fadeOut);

        }

        if (configManager.getBoolean(ConfigKeys.QUIT_SOUND_ENABLED)) {

            String soundName = configManager.getString(ConfigKeys.QUIT_SOUND_NAME);

            float volume = configManager.getFloat(ConfigKeys.QUIT_SOUND_VOLUME);
            float pitch = configManager.getFloat(ConfigKeys.QUIT_SOUND_PITCH);

            soundService.playSound(player, soundName, volume, pitch);

        }
    }
}