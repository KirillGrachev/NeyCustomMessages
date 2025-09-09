package com.ney.messages.service.impl;

import com.ney.messages.config.DeathConfig;
import com.ney.messages.service.interfaces.IDeathMessageService;
import com.ney.messages.service.interfaces.ISoundService;
import com.ney.messages.service.interfaces.ITitleService;
import com.ney.messages.util.HexColorUtil;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathMessageServiceImpl implements IDeathMessageService {

    private final DeathConfig config;
    private final ITitleService titleService;
    private final ISoundService soundService;

    public DeathMessageServiceImpl(DeathConfig config,
                                   ITitleService titleService, ISoundService soundService) {
        this.config = config;
        this.titleService = titleService;
        this.soundService = soundService;
    }

    @Override
    public void handleDeath(PlayerDeathEvent event) {

        if (!config.enabled()) return;

        Player player = event.getEntity();
        String deathMessage = event.getDeathMessage();
        event.setDeathMessage(null);

        if (config.message().enabled() && config.message().format() != null
                && !config.message().format().isEmpty()) {

            String message = HexColorUtil.color(config.message().format()
                    .replace("{player}", player.getName())
                    .replace("{death_cause}", deathMessage != null ? deathMessage : "unknown"));

            Bukkit.broadcastMessage(message);

        }

        if (config.title().enabled() &&
                (config.title().title() != null && !config.title().title().isEmpty() ||
                        config.title().subtitle() != null && !config.title().subtitle().isEmpty())) {

            titleService.sendTitle(
                    player,
                    HexColorUtil.color(config.title().title()),
                    HexColorUtil.color(config.title().subtitle()
                            .replace("{player}", player.getName())
                            .replace("{death_cause}", deathMessage != null ? deathMessage : "unknown")),
                    config.title().fadeIn(),
                    config.title().stay(),
                    config.title().fadeOut()
            );

        }

        if (config.sound().enabled() && config.sound().name() != null && !config.sound().name().isEmpty()) {

            soundService.playSound(
                    player,
                    config.sound().name(),
                    config.sound().volume(),
                    config.sound().pitch()
            );

        }

        Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("NeyCustomMessages"),
                () -> {

                    if (player.isDead()) {
                        player.spigot().respawn();
                    }

                }, 1L);
    }
}