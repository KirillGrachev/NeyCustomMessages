package com.ney.messages.service.event;

import com.ney.messages.NeyCustomMessages;
import com.ney.messages.config.ConfigKeys;
import com.ney.messages.config.ConfigManager;
import com.ney.messages.service.SoundService;
import com.ney.messages.service.TitleService;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathMessageService {

    private final ConfigManager configManager;
    private final TitleService titleService;
    private final SoundService soundService;
    private final NeyCustomMessages neyCustomMessages;

    public DeathMessageService(NeyCustomMessages neyCustomMessages,
                               ConfigManager configManager,
                               TitleService titleService,
                               SoundService soundService) {
        this.neyCustomMessages = neyCustomMessages;
        this.configManager = configManager;
        this.titleService = titleService;
        this.soundService = soundService;
    }

    public void handleDeath(PlayerDeathEvent event) {

        if (!configManager.getBoolean(ConfigKeys.DEATH_ENABLED)) return;

        Player player = event.getEntity();
        String playerName = player.getName();
        String deathCause = event.getDeathMessage() != null ? event.getDeathMessage() : "неизвестно";

        event.setDeathMessage(null);

        if (configManager.getBoolean(ConfigKeys.DEATH_MESSAGE_ENABLED)) {

            String msg = configManager.getString(ConfigKeys.DEATH_MESSAGE_FORMAT)
                    .replace("{player}", playerName)
                    .replace("{death_cause}", deathCause);

            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', msg));

        }

        if (configManager.getBoolean(ConfigKeys.DEATH_TITLE_ENABLED)) {

            String title = configManager.getString(ConfigKeys.DEATH_TITLE_TITLE);
            String subtitle = configManager.getString(ConfigKeys.DEATH_TITLE_SUBTITLE)
                    .replace("{death_cause}", deathCause);

            int fadeIn = configManager.getInt(ConfigKeys.DEATH_TITLE_FADE_IN);
            int stay = configManager.getInt(ConfigKeys.DEATH_TITLE_STAY);
            int fadeOut = configManager.getInt(ConfigKeys.DEATH_TITLE_FADE_OUT);

            titleService.sendTitle(player, title, subtitle, fadeIn, stay, fadeOut);

        }

        if (configManager.getBoolean(ConfigKeys.DEATH_SOUND_ENABLED)) {

            String soundName = configManager.getString(ConfigKeys.DEATH_SOUND_NAME);

            float volume = configManager.getFloat(ConfigKeys.DEATH_SOUND_VOLUME);
            float pitch = configManager.getFloat(ConfigKeys.DEATH_SOUND_PITCH);

            soundService.playSound(player, soundName, volume, pitch);

        }

        Bukkit.getScheduler().runTaskLater(neyCustomMessages, () -> {

            if (player.isDead()) {
                player.spigot().respawn();
            }

        }, 1L);

    }
}