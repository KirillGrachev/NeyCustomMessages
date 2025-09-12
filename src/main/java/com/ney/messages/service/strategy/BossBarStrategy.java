package com.ney.messages.service.strategy;

import com.ney.messages.config.*;
import com.ney.messages.service.interfaces.IBossBarService;
import com.ney.messages.service.strategy.context.NotificationContext;
import com.ney.messages.util.HexColorUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class BossBarStrategy implements NotificationStrategy {

    private final IBossBarService bossBarService;

    public BossBarStrategy(IBossBarService bossBarService) {
        this.bossBarService = bossBarService;
    }

    @Override
    public void execute(@NotNull NotificationContext context) {

        Player player = context.getPlayer();
        Object config = context.getConfig();
        BossBarConfig bossBarConfig = extractBossBarConfig(config);

        if (bossBarConfig == null || !bossBarConfig.enabled()
                || bossBarConfig.text() == null
                || bossBarConfig.text().isEmpty()) {
            return;
        }

        String text = bossBarConfig.text();
        for (Map.Entry<String, String> entry : context.getPlaceholders().entrySet()) {
            text = text.replace(entry.getKey(), entry.getValue());
        }

        bossBarService.showBossBar(
                player,
                HexColorUtil.color(text),
                bossBarConfig.color(),
                bossBarConfig.style(),
                bossBarConfig.duration(),
                bossBarConfig.progressDecay()
        );

    }

    @Override
    public boolean isEnabled(@NotNull NotificationContext context) {

        Object config = context.getConfig();
        BossBarConfig bossBarConfig = extractBossBarConfig(config);

        return bossBarConfig != null && bossBarConfig.enabled()
                && bossBarConfig.text() != null
                && !bossBarConfig.text().isEmpty();

    }

    private @Nullable BossBarConfig extractBossBarConfig(Object config) {

        if (config instanceof AnnouncementConfig) {
            return ((AnnouncementConfig) config).bossBar();
        } else if (config instanceof DeathConfig) {
            return ((DeathConfig) config).bossBar();
        } else if (config instanceof JoinConfig) {
            return ((JoinConfig) config).bossBar();
        } else if (config instanceof QuitConfig) {
            return ((QuitConfig) config).bossBar();
        }

        return null;

    }
}