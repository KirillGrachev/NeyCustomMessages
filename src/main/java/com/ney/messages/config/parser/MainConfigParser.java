package com.ney.messages.config.parser;

import com.ney.messages.config.*;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.ney.messages.config.keys.ConfigKeys.*;

public class MainConfigParser implements ConfigParser {

    @Override
    public MainConfig parse(FileConfiguration config) {

        DeathConfig deathConfig = parseDeathConfig(config);
        JoinConfig joinConfig = parseJoinConfig(config);
        QuitConfig quitConfig = parseQuitConfig(config);
        AnnouncementsConfig announcementsConfig = parseAnnouncementsConfig(config);

        return new MainConfig(deathConfig, joinConfig, quitConfig, announcementsConfig);

    }

    private @NotNull DeathConfig parseDeathConfig(@NotNull FileConfiguration config) {

        boolean enabled = config.getBoolean(Death.ENABLED, true);

        MessageConfig messageConfig = new MessageConfig(
                config.getBoolean(Death.Message.ENABLED, true),
                config.getString(Death.Message.FORMAT, "&c{player} &7умер от {death_cause}")
        );

        TitleConfig titleConfig = new TitleConfig(
                config.getBoolean(Death.Title.ENABLED, true),
                config.getString(Death.Title.TITLE, "&cСмерть!"),
                config.getString(Death.Title.SUBTITLE, "&7Вы умерли"),
                config.getInt(Death.Title.FADE_IN, 10),
                config.getInt(Death.Title.STAY, 70),
                config.getInt(Death.Title.FADE_OUT, 20)
        );

        SoundConfig soundConfig = new SoundConfig(
                config.getBoolean(Death.Sound.ENABLED, true),
                config.getString(Death.Sound.NAME, "ENTITY_PLAYER_DEATH"),
                (float) config.getDouble(Death.Sound.VOLUME, 1.0),
                (float) config.getDouble(Death.Sound.PITCH, 1.0)
        );

        BossBarConfig bossBarConfig = new BossBarConfig(
                config.getBoolean(Death.BossBar.ENABLED, false),
                config.getString(Death.BossBar.TEXT, "&cВы умерли!"),
                parseBarColor(config.getString(Death.BossBar.COLOR, "RED")),
                parseBarStyle(config.getString(Death.BossBar.STYLE, "SOLID")),
                config.getInt(Death.BossBar.DURATION, 3),
                config.getBoolean(Death.BossBar.PROGRESS_DECAY, false)
        );

        return new DeathConfig(enabled, messageConfig, titleConfig,
                soundConfig, bossBarConfig);

    }

    private @NotNull JoinConfig parseJoinConfig(@NotNull FileConfiguration config) {

        boolean enabled = config.getBoolean(Join.ENABLED, false);

        MessageConfig messageConfig = new MessageConfig(
                config.getBoolean(Join.Message.ENABLED, true),
                config.getString(Join.Message.FORMAT, "&a{player} &7присоединился к серверу!")
        );

        TitleConfig titleConfig = new TitleConfig(
                config.getBoolean(Join.Title.ENABLED, true),
                config.getString(Join.Title.TITLE, "&aДобро пожаловать!"),
                config.getString(Join.Title.SUBTITLE, "&7{player}"),
                config.getInt(Join.Title.FADE_IN, 10),
                config.getInt(Join.Title.STAY, 70),
                config.getInt(Join.Title.FADE_OUT, 20)
        );

        SoundConfig soundConfig = new SoundConfig(
                config.getBoolean(Join.Sound.ENABLED, true),
                config.getString(Join.Sound.NAME, "ENTITY_PLAYER_LEVELUP"),
                (float) config.getDouble(Join.Sound.VOLUME, 1.0),
                (float) config.getDouble(Join.Sound.PITCH, 1.0)
        );

        BossBarConfig bossBarConfig = new BossBarConfig(
                config.getBoolean(Join.BossBar.ENABLED, false),
                config.getString(Join.BossBar.TEXT, "&aДобро пожаловать, {player}!"),
                parseBarColor(config.getString(Join.BossBar.COLOR, "GREEN")),
                parseBarStyle(config.getString(Join.BossBar.STYLE, "SOLID")),
                config.getInt(Join.BossBar.DURATION, 5),
                config.getBoolean(Join.BossBar.PROGRESS_DECAY, false)
        );

        return new JoinConfig(enabled, messageConfig, titleConfig,
                soundConfig, bossBarConfig);

    }

    private @NotNull QuitConfig parseQuitConfig(@NotNull FileConfiguration config) {

        boolean enabled = config.getBoolean(Quit.ENABLED, false);

        MessageConfig messageConfig = new MessageConfig(
                config.getBoolean(Quit.Message.ENABLED, true),
                config.getString(Quit.Message.FORMAT, "&c{player} &7покинул сервер!")
        );

        TitleConfig titleConfig = new TitleConfig(
                config.getBoolean(Quit.Title.ENABLED, false),
                config.getString(Quit.Title.TITLE, ""),
                config.getString(Quit.Title.SUBTITLE, ""),
                config.getInt(Quit.Title.FADE_IN, 10),
                config.getInt(Quit.Title.STAY, 70),
                config.getInt(Quit.Title.FADE_OUT, 20)
        );

        SoundConfig soundConfig = new SoundConfig(
                config.getBoolean(Quit.Sound.ENABLED, false),
                config.getString(Quit.Sound.NAME, "ENTITY_PLAYER_LEVELUP"),
                (float) config.getDouble(Quit.Sound.VOLUME, 1.0),
                (float) config.getDouble(Quit.Sound.PITCH, 1.0)
        );

        BossBarConfig bossBarConfig = new BossBarConfig(
                config.getBoolean(Quit.BossBar.ENABLED, false),
                config.getString(Quit.BossBar.TEXT, "&c{player} покинул сервер"),
                parseBarColor(config.getString(Quit.BossBar.COLOR, "BLUE")),
                parseBarStyle(config.getString(Quit.BossBar.STYLE, "SOLID")),
                config.getInt(Quit.BossBar.DURATION, 3),
                config.getBoolean(Quit.BossBar.PROGRESS_DECAY, false)
        );


        return new QuitConfig(enabled, messageConfig, titleConfig,
                soundConfig, bossBarConfig);

    }

    private @NotNull AnnouncementsConfig parseAnnouncementsConfig(@NotNull FileConfiguration config) {

        boolean enabled = config.getBoolean(Announcements.ENABLED, false);

        int interval = config.getInt(Announcements.INTERVAL, 60);
        boolean random = config.getBoolean(Announcements.RANDOM, true);

        List<AnnouncementConfig> messages = new ArrayList<>();
        ConfigurationSection messagesSection = config.getConfigurationSection(Announcements.MESSAGES);

        if (messagesSection != null) {

            for (String key : messagesSection.getKeys(false)) {

                ConfigurationSection announceSection = messagesSection.getConfigurationSection(key);
                if (announceSection != null) {

                    AnnouncementConfig announcementConfig = parseAnnouncementConfig(announceSection);
                    if (announcementConfig != null) messages.add(announcementConfig);

                }
            }
        }

        return new AnnouncementsConfig(enabled, interval, random, messages);

    }

    private @NotNull AnnouncementConfig parseAnnouncementConfig(@NotNull ConfigurationSection section) {

        MessageConfig messageConfig = new MessageConfig(
                section.getBoolean(Announcements.Message.ENABLED, true),
                section.getString(Announcements.Message.FORMAT, "&6[Анонс] &fДобро пожаловать на сервер!")
        );

        TitleConfig titleConfig = new TitleConfig(
                section.getBoolean(Announcements.Title.ENABLED, true),
                section.getString(Announcements.Title.TITLE, "&6Анонс"),
                section.getString(Announcements.Title.SUBTITLE, "&fПроверьте чат!"),
                section.getInt(Announcements.Title.FADE_IN, 10),
                section.getInt(Announcements.Title.STAY, 70),
                section.getInt(Announcements.Title.FADE_OUT, 20)
        );

        SoundConfig soundConfig = new SoundConfig(
                section.getBoolean(Announcements.Sound.ENABLED, true),
                section.getString(Announcements.Sound.NAME, "BLOCK_NOTE_BLOCK_PLING"),
                (float) section.getDouble(Announcements.Sound.VOLUME, 1.0),
                (float) section.getDouble(Announcements.Sound.PITCH, 1.0)
        );

        BossBarConfig bossBarConfig = new BossBarConfig(
                section.getBoolean(Announcements.BossBar.ENABLED, true),
                section.getString(Announcements.BossBar.TEXT, "&6Анонс: Добро пожаловать!"),
                parseBarColor(section.getString(Announcements.BossBar.COLOR, "YELLOW")),
                parseBarStyle(section.getString(Announcements.BossBar.STYLE, "SOLID")),
                section.getInt(Announcements.BossBar.DURATION, 5),
                section.getBoolean(Announcements.BossBar.PROGRESS_DECAY, false)
        );

        String hoverText = section.getString(Announcements.Message.HOVER, "&7Нажмите, чтобы открыть сайт");
        String clickValue = section.getString(Announcements.Message.CLICK, "https://t.me/NeyOff");
        ClickEvent.Action clickAction = parseClickAction(section.getString(Announcements.Message.CLICK_ACTION, "OPEN_URL"));

        return new AnnouncementConfig(messageConfig, titleConfig, soundConfig, bossBarConfig, hoverText, clickAction, clickValue);

    }

    private BarColor parseBarColor(@NotNull String colorStr) {

        try {
            return BarColor.valueOf(colorStr.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return BarColor.YELLOW;
        }
    }

    private BarStyle parseBarStyle(@NotNull String styleStr) {

        try {
            return BarStyle.valueOf(styleStr.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return BarStyle.SOLID;
        }
    }

    private ClickEvent.Action parseClickAction(String actionStr) {

        if (actionStr == null || actionStr.isEmpty()) return null;

        try {
            return ClickEvent.Action.valueOf(actionStr.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}