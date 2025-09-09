package com.ney.messages.service.impl;

import com.ney.messages.config.AnnouncementConfig;
import com.ney.messages.config.AnnouncementsConfig;
import com.ney.messages.service.interfaces.IAnnouncementService;
import com.ney.messages.service.interfaces.IBossBarService;
import com.ney.messages.service.interfaces.ISoundService;
import com.ney.messages.service.interfaces.ITitleService;
import com.ney.messages.util.HexColorUtil;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AnnouncementServiceImpl implements IAnnouncementService {

    private final AnnouncementsConfig config;
    private final ITitleService titleService;
    private final ISoundService soundService;
    private final IBossBarService bossBarService;

    private final Logger logger;
    private final Random random = new Random();

    private final AtomicInteger currentIndex = new AtomicInteger(0);

    public AnnouncementServiceImpl(AnnouncementsConfig config, ITitleService titleService,
                                   ISoundService soundService, IBossBarService bossBarService,
                                   @NotNull JavaPlugin plugin) {
        this.config = config;
        this.titleService = titleService;
        this.soundService = soundService;
        this.bossBarService = bossBarService;
        this.logger = plugin.getLogger();
    }

    @Override
    public void runAnnouncement() {

        if (!config.enabled()) return;

        List<AnnouncementConfig> announcements = config.messages();
        if (announcements.isEmpty()) {
            logger.warning("No announcements available to display!");
            return;
        }

        AnnouncementConfig announcement = config.random()
                ? announcements.get(random.nextInt(announcements.size()))
                : announcements.get(Math.abs(currentIndex.getAndIncrement()) % announcements.size());

        BaseComponent[] component = null;
        if (announcement.message().enabled() && !announcement.message().format().isEmpty()) {
            component = createComponent(announcement);
        }

        for (Player player : Bukkit.getOnlinePlayers()) {

            if (component != null) {
                player.spigot().sendMessage(component);
            }

            if (announcement.title().enabled() &&
                    (!announcement.title().title().isEmpty() || !announcement.title().subtitle().isEmpty())) {

                titleService.sendTitle(
                        player,
                        HexColorUtil.color(announcement.title().title()),
                        HexColorUtil.color(announcement.title().subtitle()),
                        announcement.title().fadeIn(),
                        announcement.title().stay(),
                        announcement.title().fadeOut()
                );

            }

            if (announcement.sound().enabled() && !announcement.sound().name().isEmpty()) {

                soundService.playSound(
                        player,
                        announcement.sound().name(),
                        announcement.sound().volume(),
                        announcement.sound().pitch()
                );

            }

            if (announcement.bossBar().enabled() && !announcement.bossBar().text().isEmpty()) {

                bossBarService.showBossBar(
                        player,
                        HexColorUtil.color(announcement.bossBar().text()),
                        announcement.bossBar().color(),
                        announcement.bossBar().style(),
                        announcement.bossBar().duration(),
                        announcement.bossBar().progressDecay()
                );

            }
        }
    }

    private BaseComponent @NotNull [] createComponent(@NotNull AnnouncementConfig announcement) {

        TextComponent component = new TextComponent(HexColorUtil.color(announcement.message().format()));

        if (announcement.hoverText() != null && !announcement.hoverText().isEmpty()) {

            component.setHoverEvent(new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(HexColorUtil.color(announcement.hoverText())).create()
            ));

        }

        if (announcement.clickAction() != null && announcement.clickValue() != null && !announcement.clickValue().isEmpty()) {

            try {

                ClickEvent.Action action = announcement.clickAction();
                String value = announcement.clickValue().trim();

                if (action == ClickEvent.Action.OPEN_URL) {
                    new java.net.URL(value);
                } else if (action == ClickEvent.Action.RUN_COMMAND
                        || action == ClickEvent.Action.SUGGEST_COMMAND) {

                    if (!value.startsWith("/")) {
                        value = "/" + value;
                    }

                }

                component.setClickEvent(new ClickEvent(action, value));

            } catch (MalformedURLException e) {
                logger.log(Level.WARNING, "Invalid URL in announcement: " + announcement.clickValue(), e);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error processing click event for announcement", e);
            }
        }

        return new BaseComponent[]{component};

    }
}