package eu.neydev.custommessages.config.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Ключи служебных сообщений плагина (командный слой).
 *
 * Каждый ключ хранит путь в config.yml, дефолтный текст на английском и
 * признак включенности по умолчанию. {@code legacyPath} - полный путь
 * версии 1.x: тексты команд жили в секции commands.ncm, в 2.0 они
 * переехали в общую messages-секцию; старая конфигурация продолжает
 * читаться с подсказкой LEGACY_PATH, файл на диске не переписывается.
 * Контентные тексты модулей и анонсов сюда не входят - они принадлежат
 * своим секциям и доставляются Notifier'ом.
 */
public enum MessageKey {

    STATE_ON("state_on", null, true, "&aenabled"),

    STATE_OFF("state_off", null, true, "&cdisabled"),

    NO_PERMISSION("no_permission", "commands.ncm.subcommands.reload.message-no-permission",
            true, "{prefix}&#ff6b6bYou do not have permission to do this."),

    USAGE("usage", "commands.ncm.usage", true,
            "{prefix}&#8ce0ffNeyCustomMessages commands:\n"
                    + "&f/ncm reload &8- &7reload the configuration\n"
                    + "&f/ncm info &8- &7modules, announcements and permission state"),

    RELOAD("reload", "commands.ncm.subcommands.reload.message-success", true,
            "{prefix}&#9dff8cConfiguration reloaded &7({time} ms, {announcements} announcements, every {interval}s)."),

    INFO("info", null, true, "{prefix}&7Version: &f{version}\n"
            + "&7Join: &f{join}\n"
            + "&7Quit: &f{quit}\n"
            + "&7Death: &f{death}\n"
            + "&7Announcements: &f{announcements_state} &8({count} configured, every {interval}s, {mode})\n"
            + "&7Permissions: &f{permissions}");

    private final String configKey;
    private final @Nullable String legacyPath;
    private final boolean defaultEnabled;
    private final String defaultMessage;

    MessageKey(String configKey, @Nullable String legacyPath,
               boolean defaultEnabled, String defaultMessage) {
        this.configKey = configKey;
        this.legacyPath = legacyPath;
        this.defaultEnabled = defaultEnabled;
        this.defaultMessage = defaultMessage;
    }

    public @NotNull String getConfigKey() {
        return configKey;
    }

    /**
     * Полный путь значения в конфигурации 1.x или null, если ключ появился в 2.0.
     */
    public @Nullable String getLegacyPath() {
        return legacyPath;
    }

    /**
     * Включено ли сообщение, если его секции нет в конфигурации.
     */
    public boolean isDefaultEnabled() {
        return defaultEnabled;
    }

    public @NotNull String getDefaultMessage() {
        return defaultMessage;
    }

}
