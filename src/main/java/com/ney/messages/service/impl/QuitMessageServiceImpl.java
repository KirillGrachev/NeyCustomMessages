package com.ney.messages.service.impl;

import com.ney.messages.config.QuitConfig;
import com.ney.messages.service.interfaces.IQuitMessageService;
import com.ney.messages.service.interfaces.ISoundService;
import com.ney.messages.service.interfaces.ITitleService;
import com.ney.messages.util.HexColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitMessageServiceImpl implements IQuitMessageService {

    private final QuitConfig config;
    private final ITitleService titleService;
    private final ISoundService soundService;

    public QuitMessageServiceImpl(QuitConfig config,
                                  ITitleService titleService,
                                  ISoundService soundService) {
        this.config = config;
        this.titleService = titleService;
        this.soundService = soundService;
    }

    @Override
    public void handleQuit(PlayerQuitEvent event) {

        if (!config.enabled()) return;

        Player player = event.getPlayer();
        event.setQuitMessage(null);

        if (config.message().enabled() && config.message().format() != null && !config.message().format().isEmpty()) {
            String message = HexColorUtil.color(config.message().format().replace("{player}", player.getName()));
            Bukkit.broadcastMessage(message);
        }

        if (config.title().enabled() &&
                (config.title().title() != null && !config.title().title().isEmpty() ||
                        config.title().subtitle() != null && !config.title().subtitle().isEmpty())) {

            titleService.sendTitle(
                    player,
                    HexColorUtil.color(config.title().title()),
                    HexColorUtil.color(config.title().subtitle().replace("{player}", player.getName())),
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
        
    }
}