package com.ney.messages.service.strategy;

import com.ney.messages.config.*;
import com.ney.messages.service.strategy.context.NotificationContext;
import com.ney.messages.util.HexColorUtil;
import com.ney.messages.util.LoggerHelper;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.util.Map;

public class ChatMessageStrategy implements NotificationStrategy {

    private final LoggerHelper loggerHelper;

    public ChatMessageStrategy(LoggerHelper loggerHelper) {
        this.loggerHelper = loggerHelper;
    }

    @Override
    public void execute(@NotNull NotificationContext context) {

        Player player = context.getPlayer();
        Object config = context.getConfig();

        MessageConfig messageConfig = extractMessageConfig(config);

        if (messageConfig == null || !messageConfig.enabled()
                || messageConfig.format() == null
                || messageConfig.format().isEmpty()) {
            return;
        }

        String formattedMessage = messageConfig.format();
        for (Map.Entry<String, String> entry : context.getPlaceholders().entrySet()) {
            formattedMessage = formattedMessage.replace(entry.getKey(), entry.getValue());
        }

        BaseComponent[] component = createComponent(formattedMessage,
                extractAnnouncementConfig(config));

        if (component != null) {
            player.spigot().sendMessage(component);
        } else {
            Bukkit.broadcastMessage(HexColorUtil.color(formattedMessage));
        }

    }

    @Override
    public boolean isEnabled(@NotNull NotificationContext context) {

        Object config = context.getConfig();
        MessageConfig messageConfig = extractMessageConfig(config);

        return messageConfig != null && messageConfig.enabled()
                && messageConfig.format() != null
                && !messageConfig.format().isEmpty();

    }

    private @Nullable MessageConfig extractMessageConfig(Object config) {

        if (config instanceof AnnouncementConfig) {
            return ((AnnouncementConfig) config).message();
        } else if (config instanceof DeathConfig) {
            return ((DeathConfig) config).message();
        } else if (config instanceof JoinConfig) {
            return ((JoinConfig) config).message();
        } else if (config instanceof QuitConfig) {
            return ((QuitConfig) config).message();
        }

        return null;

    }

    private AnnouncementConfig extractAnnouncementConfig(Object config) {
        return config instanceof AnnouncementConfig ? (AnnouncementConfig) config : null;
    }

    private BaseComponent[] createComponent(String formattedMessage,
                                            AnnouncementConfig announcementConfig) {

        if (announcementConfig == null) return null;

        TextComponent component = new TextComponent(HexColorUtil.color(formattedMessage));

        if (announcementConfig.hoverText() != null && !announcementConfig.hoverText().isEmpty()) {

            component.setHoverEvent(new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(HexColorUtil.color(announcementConfig.hoverText())).create()
            ));

        }

        if (announcementConfig.clickAction() != null && announcementConfig.clickValue() != null
                && !announcementConfig.clickValue().isEmpty()) {

            try {

                ClickEvent.Action action = announcementConfig.clickAction();
                String value = announcementConfig.clickValue().trim();

                if (action == ClickEvent.Action.OPEN_URL) {
                    new java.net.URL(value);
                } else if (action == ClickEvent.Action.RUN_COMMAND
                        || action == ClickEvent.Action.SUGGEST_COMMAND) {
                    if (!value.startsWith("/")) value = "/" + value;
                }

                component.setClickEvent(new ClickEvent(action, value));

            } catch (MalformedURLException e) {

                loggerHelper.error("Announcement", e, "Invalid URL in announcement: "
                        + announcementConfig.clickValue());

            } catch (Exception e) {

                loggerHelper.error("Announcement", e,
                        "Error processing click event for announcement");

            }
        }

        return new BaseComponent[]{component};

    }
}