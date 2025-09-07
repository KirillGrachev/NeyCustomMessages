package com.ney.messages.service.announcement;

import com.ney.messages.config.ConfigKeys;
import com.ney.messages.config.ConfigManager;
import com.ney.messages.service.BossBarService;
import com.ney.messages.service.SoundService;
import com.ney.messages.service.TitleService;
import com.ney.messages.util.HexColorUtil;
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

        if (!configManager.getBooleanWithValidation(ConfigKeys.ANNOUNCES_ENABLED, false))
            return;

        List<ConfigManager.Announcement> announcements = configManager.getAnnouncements();
        if (announcements.isEmpty()) {

            Bukkit.getLogger().warning("Нет доступных анонсов для отображения!");
            return;

        }

        ConfigManager.Announcement announcement = configManager.getBooleanWithValidation(ConfigKeys.ANNOUNCES_RANDOM,
                false) ? announcements.get(random.nextInt(announcements.size()))
                : announcements.get(currentIndex++ % announcements.size());

        BaseComponent[] component = null;
        if (announcement.messageEnabled() && !announcement.messageFormat().isEmpty()) {
            component = createComponent(announcement);
        }

        for (Player player : Bukkit.getOnlinePlayers()) {

            if (component != null) {
                player.spigot().sendMessage(component);
            }

            if ((announcement.titleEnabled() && !announcement.title().isEmpty()) || !announcement.subtitle().isEmpty()) {

                titleService.sendTitle(player, HexColorUtil.color(announcement.title()),
                        HexColorUtil.color(announcement.subtitle()), announcement.fadeIn(),
                        announcement.stay(), announcement.fadeOut()
                );

            }

            if (announcement.soundEnabled() && !announcement.soundName().isEmpty()) {

                soundService.playSound(player, announcement.soundName(),
                        announcement.soundVolume(), announcement.soundPitch()
                );

            }

            if (announcement.bossBarEnabled() && !announcement.bossBarText().isEmpty()) {

                bossBarService.showBossBar(player, HexColorUtil.color(announcement.bossBarText()),
                        announcement.bossBarColor(), announcement.bossBarStyle(),
                        announcement.duration(), announcement.progressDecay()
                );

            }

        }
    }

    private BaseComponent @NotNull [] createComponent(ConfigManager.@NotNull Announcement announcement) {

        TextComponent component = new TextComponent(HexColorUtil.color(announcement.messageFormat()));

        if (!announcement.hoverText().isEmpty()) {
            component.setHoverEvent(new net.md_5.bungee.api.chat.HoverEvent(
                    net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(HexColorUtil.color(announcement.hoverText())).create()
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