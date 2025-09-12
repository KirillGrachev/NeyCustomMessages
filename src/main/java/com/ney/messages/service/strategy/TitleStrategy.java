package com.ney.messages.service.strategy;

import com.ney.messages.config.*;
import com.ney.messages.service.interfaces.ITitleService;
import com.ney.messages.service.strategy.context.NotificationContext;
import com.ney.messages.util.HexColorUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class TitleStrategy implements NotificationStrategy {

    private final ITitleService titleService;

    public TitleStrategy(ITitleService titleService) {
        this.titleService = titleService;
    }

    @Override
    public void execute(@NotNull NotificationContext context) {

        Player player = context.getPlayer();
        Object config = context.getConfig();

        TitleConfig titleConfig = extractTitleConfig(config);

        if (titleConfig == null || !titleConfig.enabled() ||
                (titleConfig.title() == null || titleConfig.title().isEmpty()) &&
                        (titleConfig.subtitle() == null
                                || titleConfig.subtitle().isEmpty())) {
            return;
        }

        String title = titleConfig.title() != null ? titleConfig.title() : "";
        String subtitle = titleConfig.subtitle() != null ? titleConfig.subtitle() : "";

        for (Map.Entry<String, String> entry : context.getPlaceholders().entrySet()) {

            title = title.replace(entry.getKey(), entry.getValue());
            subtitle = subtitle.replace(entry.getKey(), entry.getValue());

        }

        titleService.sendTitle(
                player,
                HexColorUtil.color(title),
                HexColorUtil.color(subtitle),
                titleConfig.fadeIn(),
                titleConfig.stay(),
                titleConfig.fadeOut()
        );

    }

    @Override
    public boolean isEnabled(@NotNull NotificationContext context) {

        Object config = context.getConfig();
        TitleConfig titleConfig = extractTitleConfig(config);

        return titleConfig != null && titleConfig.enabled() &&
                ((titleConfig.title() != null && !titleConfig.title().isEmpty()) ||
                        (titleConfig.subtitle() != null
                                && !titleConfig.subtitle().isEmpty()));

    }

    private @Nullable TitleConfig extractTitleConfig(Object config) {

        if (config instanceof AnnouncementConfig) {
            return ((AnnouncementConfig) config).title();
        } else if (config instanceof DeathConfig) {
            return ((DeathConfig) config).title();
        } else if (config instanceof JoinConfig) {
            return ((JoinConfig) config).title();
        } else if (config instanceof QuitConfig) {
            return ((QuitConfig) config).title();
        }

        return null;

    }
}