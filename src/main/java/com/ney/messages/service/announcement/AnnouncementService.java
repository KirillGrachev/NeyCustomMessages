package com.ney.messages.service.announcement;

import com.ney.messages.config.ConfigKeys;
import com.ney.messages.config.ConfigManager;
import com.ney.messages.service.BossBarService;
import com.ney.messages.service.SoundService;
import com.ney.messages.service.TitleService;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

public class AnnouncementService {

    private final ConfigManager configManager;
    private final TitleService titleService;
    private final SoundService soundService;
    private final BossBarService bossBarService;

    private final Random random = new Random();
    private int currentIndex = 0;

    public AnnouncementService(ConfigManager configManager, TitleService titleService,
                               SoundService soundService, BossBarService bossBarService) {
        this.configManager = configManager;
        this.titleService = titleService;
        this.soundService = soundService;
        this.bossBarService = bossBarService;
    }

    public void runAnnouncement() {

        if (!configManager.getBoolean(ConfigKeys.ANNOUNCES_ENABLED)) return;

        List<ConfigManager.Announcement> announcements = configManager.getAnnouncements();
        if (announcements.isEmpty()) {

            Bukkit.getLogger().warning("Нет доступных анонсов для отображения!");
            return;

        }

        ConfigManager.Announcement announcement;
        if (configManager.getBoolean(ConfigKeys.ANNOUNCES_RANDOM)) {
            announcement = announcements.get(random.nextInt(announcements.size()));
        } else {

            announcement = announcements.get(currentIndex);
            currentIndex = (currentIndex + 1) % announcements.size();

        }

        if (!announcement.messageFormat().isEmpty()) {
            BaseComponent[] component = createComponent(announcement);
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.spigot().sendMessage(component);
            }
        }

        if (!announcement.title().isEmpty() || !announcement.subtitle().isEmpty()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                titleService.sendTitle(player, announcement.title(), announcement.subtitle(),
                        announcement.fadeIn(), announcement.stay(), announcement.fadeOut());
            }
        }

        if (!announcement.soundName().isEmpty()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                soundService.playSound(player, announcement.soundName(),
                        announcement.soundVolume(), announcement.soundPitch());
            }
        }

        if (!announcement.bossBarText().isEmpty()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                bossBarService.showBossBar(player, announcement.bossBarText(),
                        announcement.bossBarColor(), announcement.bossBarStyle(),
                        announcement.duration(), announcement.progressDecay());
            }
        }
    }

    private BaseComponent @NotNull [] createComponent(ConfigManager.@NotNull Announcement announcement) {

        TextComponent component = new TextComponent(
                ChatColor.translateAlternateColorCodes('&', announcement.messageFormat())
        );

        if (!announcement.hoverText().isEmpty()) {
            component.setHoverEvent(new net.md_5.bungee.api.chat.HoverEvent(
                    net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', announcement.hoverText())).create()
            ));
        }

        ClickEvent.Action clickAction = announcement.clickAction();
        String clickValue = announcement.clickValue().trim();

        if (clickAction != null && !clickValue.isEmpty()) {

            try {

                switch (clickAction) {

                    case OPEN_URL -> new java.net.URL(clickValue);

                    case RUN_COMMAND, SUGGEST_COMMAND -> {

                        if (!clickValue.startsWith("/")) {
                            clickValue = "/" + clickValue;
                        }

                    }

                    default -> {

                        Bukkit.getLogger().warning("Действие клика не поддерживается: " + clickAction);
                        return new BaseComponent[]{component};

                    }
                }

                component.setClickEvent(new ClickEvent(clickAction, clickValue));

            } catch (java.net.MalformedURLException e) {
                Bukkit.getLogger().warning("Некорректный URL в анонсе: " + clickValue);
            } catch (Exception e) {
                Bukkit.getLogger().severe("Ошибка при обработке действия клика: " + e.getMessage());
            }
        }

        return new BaseComponent[]{component};

    }
}