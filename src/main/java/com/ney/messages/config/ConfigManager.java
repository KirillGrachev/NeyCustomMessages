package com.ney.messages.config;

import com.ney.messages.NeyCustomMessages;

import com.ney.messages.util.HexColorUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.ney.messages.config.ConfigKeys.*;

public class ConfigManager {

    private final NeyCustomMessages plugin;
    private FileConfiguration config;

    private final Map<String, Object> cachedValues = new ConcurrentHashMap<>();

    public ConfigManager(@NotNull NeyCustomMessages plugin) {

        this.plugin = plugin;
        saveDefaultConfig();

        loadConfig();
        cacheConfigValues();

    }

    private void saveDefaultConfig() {
        plugin.saveDefaultConfig();
    }

    private void loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    private void cacheConfigValues() {

        cachedValues.put(DEATH_ENABLED, getBooleanWithValidation(DEATH_ENABLED, false));
        cachedValues.put(DEATH_MESSAGE_ENABLED, getBooleanWithValidation(DEATH_MESSAGE_ENABLED, true));
        cachedValues.put(DEATH_MESSAGE_FORMAT, getStringWithColor(DEATH_MESSAGE_FORMAT, ""));
        cachedValues.put(DEATH_TITLE_ENABLED, getBooleanWithValidation(DEATH_TITLE_ENABLED, true));
        cachedValues.put(DEATH_TITLE_TITLE, getStringWithColor(DEATH_TITLE_TITLE, ""));
        cachedValues.put(DEATH_TITLE_SUBTITLE, getStringWithColor(DEATH_TITLE_SUBTITLE, ""));
        cachedValues.put(DEATH_TITLE_FADE_IN, getIntWithValidation(DEATH_TITLE_FADE_IN, 10, 0, 100));
        cachedValues.put(DEATH_TITLE_STAY, getIntWithValidation(DEATH_TITLE_STAY, 70, 0, 200));
        cachedValues.put(DEATH_TITLE_FADE_OUT, getIntWithValidation(DEATH_TITLE_FADE_OUT, 20, 0, 100));
        cachedValues.put(DEATH_SOUND_ENABLED, getBooleanWithValidation(DEATH_SOUND_ENABLED, true));
        cachedValues.put(DEATH_SOUND_NAME, getStringWithValidation(DEATH_SOUND_NAME, "ENTITY_PLAYER_DEATH"));
        cachedValues.put(DEATH_SOUND_VOLUME, getDoubleWithValidation(DEATH_SOUND_VOLUME, 1.0, 0.0, 2.0));
        cachedValues.put(DEATH_SOUND_PITCH, getDoubleWithValidation(DEATH_SOUND_PITCH, 1.0, 0.0, 2.0));

        cachedValues.put(JOIN_ENABLED, getBooleanWithValidation(JOIN_ENABLED, false));
        cachedValues.put(JOIN_MESSAGE_ENABLED, getBooleanWithValidation(JOIN_MESSAGE_ENABLED, true));
        cachedValues.put(JOIN_MESSAGE_FORMAT, getStringWithColor(JOIN_MESSAGE_FORMAT, ""));
        cachedValues.put(JOIN_TITLE_ENABLED, getBooleanWithValidation(JOIN_TITLE_ENABLED, true));
        cachedValues.put(JOIN_TITLE_TITLE, getStringWithColor(JOIN_TITLE_TITLE, ""));
        cachedValues.put(JOIN_TITLE_SUBTITLE, getStringWithColor(JOIN_TITLE_SUBTITLE, ""));
        cachedValues.put(JOIN_TITLE_FADE_IN, getIntWithValidation(JOIN_TITLE_FADE_IN, 10, 0, 100));
        cachedValues.put(JOIN_TITLE_STAY, getIntWithValidation(JOIN_TITLE_STAY, 70, 0, 200));
        cachedValues.put(JOIN_TITLE_FADE_OUT, getIntWithValidation(JOIN_TITLE_FADE_OUT, 20, 0, 100));
        cachedValues.put(JOIN_SOUND_ENABLED, getBooleanWithValidation(JOIN_SOUND_ENABLED, true));
        cachedValues.put(JOIN_SOUND_NAME, getStringWithValidation(JOIN_SOUND_NAME, "ENTITY_PLAYER_LEVELUP"));
        cachedValues.put(JOIN_SOUND_VOLUME, getDoubleWithValidation(JOIN_SOUND_VOLUME, 1.0, 0.0, 2.0));
        cachedValues.put(JOIN_SOUND_PITCH, getDoubleWithValidation(JOIN_SOUND_PITCH, 1.0, 0.0, 2.0));

        cachedValues.put(QUIT_ENABLED, getBooleanWithValidation(QUIT_ENABLED, false));
        cachedValues.put(QUIT_MESSAGE_ENABLED, getBooleanWithValidation(QUIT_MESSAGE_ENABLED, true));
        cachedValues.put(QUIT_MESSAGE_FORMAT, getStringWithColor(QUIT_MESSAGE_FORMAT, ""));
        cachedValues.put(QUIT_TITLE_ENABLED, getBooleanWithValidation(QUIT_TITLE_ENABLED, false));
        cachedValues.put(QUIT_TITLE_TITLE, getStringWithColor(QUIT_TITLE_TITLE, ""));
        cachedValues.put(QUIT_TITLE_SUBTITLE, getStringWithColor(QUIT_TITLE_SUBTITLE, ""));
        cachedValues.put(QUIT_TITLE_FADE_IN, getIntWithValidation(QUIT_TITLE_FADE_IN, 10, 0, 100));
        cachedValues.put(QUIT_TITLE_STAY, getIntWithValidation(QUIT_TITLE_STAY, 70, 0, 200));
        cachedValues.put(QUIT_TITLE_FADE_OUT, getIntWithValidation(QUIT_TITLE_FADE_OUT, 20, 0, 100));
        cachedValues.put(QUIT_SOUND_ENABLED, getBooleanWithValidation(QUIT_SOUND_ENABLED, false));
        cachedValues.put(QUIT_SOUND_NAME, getStringWithValidation(QUIT_SOUND_NAME, "ENTITY_PLAYER_LEVELUP"));
        cachedValues.put(QUIT_SOUND_VOLUME, getDoubleWithValidation(QUIT_SOUND_VOLUME, 1.0, 0.0, 2.0));
        cachedValues.put(QUIT_SOUND_PITCH, getDoubleWithValidation(QUIT_SOUND_PITCH, 1.0, 0.0, 2.0));

        cachedValues.put(ANNOUNCES_ENABLED, getBooleanWithValidation(ANNOUNCES_ENABLED, false));
        cachedValues.put(ANNOUNCES_INTERVAL, getIntWithValidation(ANNOUNCES_INTERVAL, 60, 1, 3600));
        cachedValues.put(ANNOUNCES_RANDOM, getBooleanWithValidation(ANNOUNCES_RANDOM, true));

        cacheAnnouncements();

    }

    private void cacheAnnouncements() {

        List<Announcement> announcements = new ArrayList<>();
        ConfigurationSection section = config.getConfigurationSection(ANNOUNCES_MESSAGES);

        if (section != null) {

            for (String key : section.getKeys(false)) {

                ConfigurationSection announceSection = section.getConfigurationSection(key);
                if (announceSection != null) {

                    Announcement announcement = createAnnouncement(announceSection);
                    if (announcement != null) announcements.add(announcement);

                }
            }
        }

        cachedValues.put(ANNOUNCES_MESSAGES, announcements);

    }

    private @Nullable Announcement createAnnouncement(ConfigurationSection section) {

        try {

            String messageFormat = getStringWithColor(section, "message.format", "");
            String hoverText = getStringWithColor(section, "message.hover", "");
            String clickValue = section.getString("message.click", "");
            String clickActionStr = section.getString("message.clickAction", "").toUpperCase();

            ClickEvent.Action clickAction = null;

            if (!clickValue.isEmpty() && !clickActionStr.isEmpty()) {

                try {
                    clickAction = ClickEvent.Action.valueOf(clickActionStr);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Неверное действие в clickAction для анонса: " + clickActionStr);
                }

            }

            String title = getStringWithColor(section, "title.title", "");
            String subtitle = getStringWithColor(section, "title.subtitle", "");
            int fadeIn = getIntWithValidation(section, "title.fadeIn", 10, 0, 100);
            int stay = getIntWithValidation(section, "title.stay", 70, 0, 200);
            int fadeOut = getIntWithValidation(section, "title.fadeOut", 20, 0, 100);

            String soundName = getStringWithValidation(section, "sound.name", "BLOCK_NOTE_BLOCK_PLING");
            float soundVolume = (float) getDoubleWithValidation(section, "sound.volume", 1.0, 0.0, 2.0);
            float soundPitch = (float) getDoubleWithValidation(section, "sound.pitch", 1.0, 0.0, 2.0);

            String bossBarText = getStringWithColor(section, "bossbar.text", "");
            String colorStr = section.getString("bossbar.color", "YELLOW");
            BarColor bossBarColor = validateBarColor(colorStr);
            String styleStr = section.getString("bossbar.style", "SOLID");
            BarStyle bossBarStyle = validateBarStyle(styleStr);
            int duration = getIntWithValidation(section, "bossbar.duration", 5, 1, 60);
            boolean progressDecay = section.getBoolean("bossbar.progress-decay", false);

            return new Announcement(
                    messageFormat, hoverText, clickAction, clickValue,
                    title, subtitle, fadeIn, stay, fadeOut,
                    soundName, soundVolume, soundPitch,
                    bossBarText, bossBarColor, bossBarStyle, duration, progressDecay
            );

        } catch (Exception e) {

            plugin.getLogger().severe("Ошибка при кэшировании анонса: " + e.getMessage());
            return null;

        }
    }

    private BarColor validateBarColor(@NotNull String colorStr) {

        try {
            return BarColor.valueOf(colorStr.toUpperCase());
        } catch (IllegalArgumentException e) {

            plugin.getLogger().warning("Некорректный цвет босс-бара: " + colorStr + ". Используется YELLOW.");
            return BarColor.YELLOW;

        }
    }

    private BarStyle validateBarStyle(@NotNull String styleStr) {

        try {
            return BarStyle.valueOf(styleStr.toUpperCase());
        } catch (IllegalArgumentException e) {

            plugin.getLogger().warning("Некорректный стиль босс-бара: " + styleStr + ". Используется SOLID.");
            return BarStyle.SOLID;

        }
    }

    private @NotNull String getStringWithColor(String path, String def) {
        return HexColorUtil.color(getString(path, def));
    }

    private @NotNull String getStringWithColor(@NotNull ConfigurationSection section,
                                               String path, String def) {
        return HexColorUtil.color(section.getString(path, def));
    }

    private String getString(String path, String def) {
        return config.getString(path, def);
    }

    private String getStringWithValidation(String path, String def) {

        String value = config.getString(path, def);
        if (value == null || value.trim().isEmpty()) {

            plugin.getLogger().warning("Значение для " + path + " не задано или пусто. Используется значение по умолчанию: " + def);
            return def;

        }

        return value;

    }

    private String getStringWithValidation(@NotNull ConfigurationSection section, String path, String def) {

        String value = section.getString(path, def);
        if (value == null || value.trim().isEmpty()) {

            plugin.getLogger().warning("Значение для " + path + " в секции не задано или пусто. Используется значение по умолчанию: " + def);
            return def;

        }

        return value;

    }

    private boolean getBooleanWithValidation(String path, boolean def) {

        if (!config.isBoolean(path)) {

            plugin.getLogger().warning("Значение для " + path + " не является булевым. Используется значение по умолчанию: " + def);
            return def;

        }

        return config.getBoolean(path, def);

    }

    private int getIntWithValidation(String path, int def, int min, int max) {

        if (!config.isInt(path)) {

            plugin.getLogger().warning("Значение для " + path + " не является целым числом. Используется значение по умолчанию: " + def);
            return def;

        }

        int value = config.getInt(path, def);
        if (value < min || value > max) {

            plugin.getLogger().warning("Значение для " + path + " (" + value + ") выходит за пределы допустимого диапазона [" + min + ", " + max + "]. Используется значение по умолчанию: " + def);
            return def;

        }

        return value;

    }

    private int getIntWithValidation(@NotNull ConfigurationSection section, String path, int def, int min, int max) {

        if (!section.isInt(path)) {

            plugin.getLogger().warning("Значение для " + path + " в секции не является целым числом. Используется значение по умолчанию: " + def);
            return def;

        }

        int value = section.getInt(path, def);
        if (value < min || value > max) {

            plugin.getLogger().warning("Значение для " + path + " в секции (" + value + ") выходит за пределы допустимого диапазона [" + min + ", " + max + "]. Используется значение по умолчанию: " + def);
            return def;

        }

        return value;

    }

    private double getDoubleWithValidation(String path, double def, double min, double max) {

        if (!config.isDouble(path) && !config.isInt(path)) {

            plugin.getLogger().warning("Значение для " + path + " не является числом. Используется значение по умолчанию: " + def);
            return def;

        }

        double value = config.getDouble(path, def);
        if (value < min || value > max) {

            plugin.getLogger().warning("Значение для " + path + " (" + value + ") выходит за пределы допустимого диапазона [" + min + ", " + max + "]. Используется значение по умолчанию: " + def);
            return def;

        }

        return value;

    }

    private double getDoubleWithValidation(@NotNull ConfigurationSection section, String path, double def, double min, double max) {

        if (!section.isDouble(path) && !section.isInt(path)) {

            plugin.getLogger().warning("Значение для " + path + " в секции не является числом. Используется значение по умолчанию: " + def);
            return def;

        }

        double value = section.getDouble(path, def);

        if (value < min || value > max) {

            plugin.getLogger().warning("Значение для " + path + " в секции (" + value + ") выходит за пределы допустимого диапазона [" + min + ", " + max + "]. Используется значение по умолчанию: " + def);
            return def;

        }

        return value;

    }

    public boolean getBoolean(String key) {

        Object value = cachedValues.get(key);

        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        plugin.getLogger().warning("Ключ '" + key + "' не является boolean. Получено: " + (value != null ? value.getClass() : "null"));
        return false;

    }

    public int getInt(String key) {

        Object value = cachedValues.get(key);

        if (value instanceof Integer) {
            return (Integer) value;
        }

        plugin.getLogger().warning("Ключ '" + key + "' не является int. Получено: " + (value != null ? value.getClass() : "null"));
        return 0;

    }

    public String getString(String key) {

        Object value = cachedValues.get(key);
        return value != null ? value.toString() : "";

    }

    public float getFloat(String key) {

        Object value = cachedValues.get(key);
        if (value instanceof Double d) {
            return d.floatValue();
        } else if (value instanceof Float f) {
            return f;
        } else {

            plugin.getLogger().warning("Ключ '" + key + "' не является числом с плавающей точкой");
            return 0.0f;

        }
    }

    @SuppressWarnings("unchecked")
    public List<Announcement> getAnnouncements() {
        return (List<Announcement>) cachedValues.get(ANNOUNCES_MESSAGES);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) cachedValues.get(key);
    }

    public void reload() {

        loadConfig();
        cacheConfigValues();

        plugin.getLogger().info("Конфигурация успешно перезагружена.");

    }

    public record Announcement(String messageFormat, String hoverText, ClickEvent.Action clickAction, String clickValue,
                               String title, String subtitle, int fadeIn, int stay, int fadeOut, String soundName,
                               float soundVolume, float soundPitch, String bossBarText, BarColor bossBarColor,
                               BarStyle bossBarStyle, int duration, boolean progressDecay) {

    }
}