package com.ney.messages.service.strategy;

import com.ney.messages.config.*;
import com.ney.messages.service.interfaces.ISoundService;
import com.ney.messages.service.strategy.context.NotificationContext;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SoundStrategy implements NotificationStrategy {

    private final ISoundService soundService;

    public SoundStrategy(ISoundService soundService) {
        this.soundService = soundService;
    }

    @Override
    public void execute(@NotNull NotificationContext context) {

        Player player = context.getPlayer();
        Object config = context.getConfig();

        SoundConfig soundConfig = extractSoundConfig(config);

        if (soundConfig == null || !soundConfig.enabled()
                || soundConfig.name() == null
                || soundConfig.name().isEmpty()) {
            return;
        }

        soundService.playSound(
                player,
                soundConfig.name(),
                soundConfig.volume(),
                soundConfig.pitch()
        );

    }

    @Override
    public boolean isEnabled(@NotNull NotificationContext context) {

        Object config = context.getConfig();
        SoundConfig soundConfig = extractSoundConfig(config);

        return soundConfig != null && soundConfig.enabled()
                && soundConfig.name() != null && !soundConfig.name().isEmpty();

    }

    private @Nullable SoundConfig extractSoundConfig(Object config) {

        if (config instanceof AnnouncementConfig) {
            return ((AnnouncementConfig) config).sound();
        } else if (config instanceof DeathConfig) {
            return ((DeathConfig) config).sound();
        } else if (config instanceof JoinConfig) {
            return ((JoinConfig) config).sound();
        } else if (config instanceof QuitConfig) {
            return ((QuitConfig) config).sound();
        }

        return null;

    }
}