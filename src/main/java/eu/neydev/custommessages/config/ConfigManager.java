package eu.neydev.custommessages.config;

import eu.neydev.custommessages.NeyCustomMessages;
import eu.neydev.custommessages.config.type.Announcement;
import eu.neydev.custommessages.config.type.BossBarSettings;
import eu.neydev.custommessages.config.type.ChatSettings;
import eu.neydev.custommessages.config.type.ConsoleMessage;
import eu.neydev.custommessages.config.type.MessageKey;
import eu.neydev.custommessages.config.type.MessageModule;
import eu.neydev.custommessages.config.type.NotificationBundle;
import eu.neydev.custommessages.config.type.PermissionNode;
import eu.neydev.custommessages.config.type.SoundSettings;
import eu.neydev.custommessages.config.type.TitleSettings;
import eu.neydev.custommessages.service.ConsoleService;
import eu.neydev.custommessages.util.HexColorUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

/**
 * Менеджер конфигурации NeyCustomMessages.
 *
 * Значения читаются один раз и собираются в неизменяемый снапшот, который
 * подменяется одной volatile-записью. Версия 1.x размазывала чтение по
 * пяти слоям (loader, parser, provider, validator, ключи) и собирала
 * рекорды модулей ОДИН раз при загрузке: reload пересоздавал весь
 * контейнер сервисов целиком, а между отменой задачи анонсов и созданием
 * новой конфигурации слушатели продолжали работать со старыми рекордами.
 *
 * Некорректные значения загрузку не роняют: подставляется дефолт, а в
 * консоль уходит предупреждение через {@link ConsoleService}. Дефолты
 * совпадают с поставляемым config.yml: в 1.x код и файл расходились почти
 * во всем (join.enabled false в коде против true в файле, interval 60
 * против 15, цвет босс-бара всегда YELLOW в коде против GREEN/BLUE в
 * файле), и расхождение всплывало ровно тогда, когда файл потерян или бит.
 *
 * Конфигурации 1.x читаются без ручной миграции:
 * <ul>
 *   <li>секции модулей join/quit/death на верхнем уровне находятся, когда
 *       modules.* отсутствуют, - с подсказкой LEGACY_PATH;</li>
 *   <li>секция announces.* читается как announcements.*, announces.messages.*
 *       как announcements.list.*;</li>
 *   <li>переименованные ключи message-&gt;chat, fadeIn-&gt;fade_in,
 *       fadeOut-&gt;fade_out, progress-decay-&gt;progress_decay и
 *       clickAction-&gt;click_action читаются по старым именам;</li>
 *   <li>тексты команд commands.ncm.* читаются как legacy-пути ключей
 *       messages.usage / messages.reload / messages.no_permission и узла
 *       permissions.reload.</li>
 * </ul>
 * Файл на диске не переписывается.
 *
 * Тексты модулей и анонсов хранятся НЕокрашенными: плейсхолдеры
 * подставляются при доставке, а окраска выполняется после подстановки,
 * как в 1.x. Служебные сообщения командного слоя, наоборот, окрашены
 * при загрузке - в них подставляются только числа и слова состояния.
 *
 * Источник истины один - {@code plugin.getConfig()}: отдельного парсинга
 * файла менеджер не ведет, reload использует штатный reloadConfig().
 * Поврежденный YAML не тонет в тишине: Bukkit глотает
 * InvalidConfigurationException и отдает пустую конфигурацию, поэтому файл
 * перечитывается явно и ошибка уходит в SEVERE-лог со стектрейсом. В 1.x
 * битый конфиг отключал плагин целиком; 2.0 работает на дефолтах и кричит.
 */
public class ConfigManager implements CustomMessagesConfig {

    private final NeyCustomMessages plugin;
    private final ConsoleService consoleService;
    private final List<Runnable> reloadListeners = new CopyOnWriteArrayList<>();

    private static final String PATH_ENABLED = "settings.enabled";
    private static final String PATH_MESSAGES = "messages.";
    private static final String PATH_MESSAGE_PREFIX = "messages.prefix";
    private static final String PATH_MESSAGES_ENABLED = "messages.enabled";
    private static final String PATH_PERMISSIONS_ENABLED = "permissions.enabled";
    private static final String PATH_PERMISSION_RELOAD = "permissions.reload";

    private static final String SECTION_MODULES = "modules.";
    private static final String SECTION_ANNOUNCEMENTS = "announcements";
    private static final String SECTION_ANNOUNCEMENTS_LEGACY = "announces";

    /**
     * Дефолты совпадают с поставляемым config.yml (см. javadoc класса).
     */
    private static final boolean DEFAULT_PERMISSIONS_ENABLED = true;
    private static final int DEFAULT_INTERVAL = 15;
    private static final boolean DEFAULT_RANDOM = true;

    /** Окрашенные дефолты служебных сообщений: fallback не пересобирает строки на каждый вызов. */
    private static final Map<MessageKey, List<String>> DEFAULT_MESSAGES;

    /** Полностью собранные дефолты модулей: используются, когда секции модуля нет ни в новой, ни в старой форме. */
    private static final Map<MessageModule, ModuleSettings> DEFAULT_MODULES;

    static {

        Map<MessageKey, List<String>> defaults = new EnumMap<>(MessageKey.class);

        for (MessageKey key : MessageKey.values()) {
            defaults.put(key, coloredLines(key.getDefaultMessage()));
        }

        DEFAULT_MESSAGES = Collections.unmodifiableMap(defaults);

        Map<MessageModule, ModuleSettings> modules = new EnumMap<>(MessageModule.class);

        modules.put(MessageModule.JOIN, new ModuleSettings(true, new NotificationBundle(
                new ChatSettings(true, "&a[+] &e{player} &7joined the server!", null, null, null),
                new TitleSettings(true, "&aWelcome!", "&e{player}", 10, 70, 20),
                new SoundSettings(true, resolveSound("ENTITY_PLAYER_LEVELUP"), 1.0f, 1.0f),
                new BossBarSettings(true, "&aHello, {player}!",
                        BarColor.GREEN, BarStyle.SOLID, 5, false))));

        modules.put(MessageModule.QUIT, new ModuleSettings(true, new NotificationBundle(
                new ChatSettings(true, "&c[-] &e{player} &7left the server.", null, null, null),
                new TitleSettings(false, "&cBye!", "&7See you, {player}...", 10, 50, 20),
                new SoundSettings(false, resolveSound("ENTITY_ENDERMAN_TELEPORT"), 0.8f, 0.9f),
                new BossBarSettings(true, "&cGoodbye, {player}!",
                        BarColor.BLUE, BarStyle.SOLID, 5, false))));

        modules.put(MessageModule.DEATH, new ModuleSettings(true, new NotificationBundle(
                new ChatSettings(true, "&4[☠] &e{player} &7died. Cause: {death_cause}",
                        null, null, null),
                new TitleSettings(true, "&4You died!", "&7Cause: {death_cause}", 5, 60, 15),
                new SoundSettings(true, resolveSound("ENTITY_GENERIC_EXPLODE"), 1.0f, 1.2f),
                new BossBarSettings(true, "&4You died, {player}!",
                        BarColor.RED, BarStyle.SOLID, 5, false))));

        DEFAULT_MODULES = Collections.unmodifiableMap(modules);

    }

    private record ModuleSettings(boolean enabled, NotificationBundle bundle) {
    }

    private record AnnouncementsSection(boolean enabled,
                                        int interval,
                                        boolean random,
                                        List<Announcement> list) {
    }

    private record MessagingSection(boolean enabled,
                                    String prefix,
                                    Map<MessageKey, List<String>> messages) {
    }

    private record ConfigSnapshot(boolean pluginEnabled,
                                  boolean permissionsEnabled,
                                  String reloadPermission,
                                  Map<MessageModule, ModuleSettings> modules,
                                  AnnouncementsSection announcements,
                                  MessagingSection messaging) {
    }

    /**
     * Текущий снапшот. Единственное изменяемое поле менеджера; подменяется
     * атомарно, поэтому составные значения никогда не читаются «наполовину».
     */
    private volatile ConfigSnapshot snapshot;

    private FileConfiguration config;

    public ConfigManager(@NotNull NeyCustomMessages plugin, @NotNull ConsoleService consoleService) {

        this.plugin = plugin;
        this.consoleService = consoleService;

        plugin.saveDefaultConfig();

        this.config = plugin.getConfig();

        validateReadable();
        this.snapshot = buildSnapshot();

    }

    /**
     * Перезагружает конфигурацию и уведомляет подписанные компоненты.
     */
    public void reload() {

        plugin.reloadConfig();

        this.config = plugin.getConfig();

        validateReadable();

        this.snapshot = buildSnapshot();

        reloadListeners.forEach(Runnable::run);

    }

    /**
     * Подписывает компонент на перезагрузку конфигурации.
     *
     * @param listener действие, выполняемое после reload
     */
    public void onReload(@NotNull Runnable listener) {
        reloadListeners.add(listener);
    }

    @Override
    public boolean isPluginEnabled() {
        return snapshot.pluginEnabled();
    }

    @Override
    public boolean areMessagesEnabled() {
        return snapshot.messaging().enabled();
    }

    @Override
    public @NotNull String getMessagePrefix() {
        return snapshot.messaging().prefix();
    }

    @Override
    public @NotNull List<String> getMessages(@NotNull MessageKey key) {
        return snapshot.messaging().messages()
                .getOrDefault(key, DEFAULT_MESSAGES.getOrDefault(key, List.of()));
    }

    @Override
    public boolean arePermissionsEnabled() {
        return snapshot.permissionsEnabled();
    }

    @Override
    public @NotNull String getPermission(@NotNull PermissionNode node) {
        return snapshot.reloadPermission();
    }

    @Override
    public boolean isModuleEnabled(@NotNull MessageModule module) {
        return snapshot.modules().get(module).enabled();
    }

    @Override
    public @NotNull NotificationBundle getModuleBundle(@NotNull MessageModule module) {
        return snapshot.modules().get(module).bundle();
    }

    @Override
    public boolean areAnnouncementsEnabled() {
        return snapshot.announcements().enabled();
    }

    @Override
    public int getAnnouncementInterval() {
        return snapshot.announcements().interval();
    }

    @Override
    public boolean isRandomAnnouncementOrder() {
        return snapshot.announcements().random();
    }

    @Override
    public @NotNull List<Announcement> getAnnouncements() {
        return snapshot.announcements().list();
    }

    /**
     * Явно перечитывает файл, если результат штатной загрузки пуст:
     * поврежденный YAML должен кричать в консоль, а не тихо работать на дефолтах.
     */
    private void validateReadable() {

        File configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists() || !config.getKeys(false).isEmpty()) {
            return;
        }

        try {
            new YamlConfiguration().load(configFile);
        } catch (IOException | InvalidConfigurationException exception) {
            plugin.getLogger().log(Level.SEVERE,
                    "config.yml is broken and cannot be parsed - built-in defaults are used. "
                            + "Fix the file and run /ncm reload.",
                    exception);
        }

    }

    private @NotNull ConfigSnapshot buildSnapshot() {

        Map<MessageModule, ModuleSettings> modules = new EnumMap<>(MessageModule.class);

        for (MessageModule module : MessageModule.values()) {
            modules.put(module, buildModule(module));
        }

        return new ConfigSnapshot(
                config.getBoolean(PATH_ENABLED, true),
                config.getBoolean(PATH_PERMISSIONS_ENABLED, DEFAULT_PERMISSIONS_ENABLED),
                readReloadPermission(),
                Collections.unmodifiableMap(modules),
                buildAnnouncements(),
                buildMessagingSection()
        );

    }

    private @NotNull ModuleSettings buildModule(@NotNull MessageModule module) {

        ConfigurationSection section = resolveModuleSection(module);

        if (section == null) {
            return DEFAULT_MODULES.get(module);
        }

        NotificationBundle defaults = DEFAULT_MODULES.get(module).bundle();
        String basePath = section.getCurrentPath();

        return new ModuleSettings(
                section.getBoolean("enabled", true),
                new NotificationBundle(
                        buildChat(section, basePath, defaults.chat(), false),
                        buildTitle(section, basePath, defaults.title()),
                        buildSound(section, basePath, defaults.sound()),
                        buildBossBar(section, basePath, defaults.bossBar())
                )
        );

    }

    /**
     * Находит секцию модуля: новая modules.&lt;key&gt;, иначе старая
     * верхнеуровневая &lt;key&gt; с подсказкой LEGACY_PATH.
     */
    private @Nullable ConfigurationSection resolveModuleSection(@NotNull MessageModule module) {

        String path = SECTION_MODULES + module.getConfigKey();
        ConfigurationSection section = config.getConfigurationSection(path);

        if (section != null) {
            return section;
        }

        ConfigurationSection legacy = config.getConfigurationSection(module.getConfigKey());

        if (legacy == null) {
            return null;
        }

        consoleService.log(ConsoleMessage.LEGACY_PATH,
                "path", module.getConfigKey() + ".*",
                "replacement", path + ".*");
        return legacy;

    }

    private @NotNull AnnouncementsSection buildAnnouncements() {

        ConfigurationSection section = config.getConfigurationSection(SECTION_ANNOUNCEMENTS);
        String basePath = SECTION_ANNOUNCEMENTS;

        if (section == null) {

            section = config.getConfigurationSection(SECTION_ANNOUNCEMENTS_LEGACY);

            if (section != null) {

                consoleService.log(ConsoleMessage.LEGACY_PATH,
                        "path", SECTION_ANNOUNCEMENTS_LEGACY + ".*",
                        "replacement", SECTION_ANNOUNCEMENTS + ".*");
                basePath = SECTION_ANNOUNCEMENTS_LEGACY;

            }

        }

        if (section == null) {
            return new AnnouncementsSection(true, DEFAULT_INTERVAL, DEFAULT_RANDOM, List.of());
        }

        return new AnnouncementsSection(
                section.getBoolean("enabled", true),
                parsePositiveInt(section.get("interval"), DEFAULT_INTERVAL, basePath + ".interval"),
                section.getBoolean("random", DEFAULT_RANDOM),
                readAnnouncementList(section, basePath)
        );

    }

    private @NotNull List<Announcement> readAnnouncementList(@NotNull ConfigurationSection section,
                                                             @NotNull String basePath) {

        ConfigurationSection list = section.getConfigurationSection("list");

        if (list == null) {

            list = section.getConfigurationSection("messages");

            if (list != null) {
                consoleService.log(ConsoleMessage.LEGACY_PATH,
                        "path", basePath + ".messages.*",
                        "replacement", SECTION_ANNOUNCEMENTS + ".list.*");
            }

        }

        if (list == null) {
            return List.of();
        }

        List<Announcement> announcements = new ArrayList<>();

        for (String key : list.getKeys(false)) {

            ConfigurationSection entry = list.getConfigurationSection(key);

            if (entry == null) {
                continue;
            }

            String entryPath = basePath + ".list." + key;

            announcements.add(new Announcement(key, new NotificationBundle(
                    buildChat(entry, entryPath, ChatSettings.EMPTY, true),
                    buildTitle(entry, entryPath, TitleSettings.EMPTY),
                    buildSound(entry, entryPath, SoundSettings.EMPTY),
                    buildBossBar(entry, entryPath, BossBarSettings.EMPTY)
            )));

        }

        return List.copyOf(announcements);

    }

    /**
     * Читает секцию чата: новая chat, иначе старая message с подсказкой.
     *
     * @param interactive true для анонсов: читаются hover, click и click_action
     */
    private @NotNull ChatSettings buildChat(@NotNull ConfigurationSection parent,
                                            @NotNull String basePath,
                                            @NotNull ChatSettings defaults,
                                            boolean interactive) {

        ConfigurationSection section = parent.getConfigurationSection("chat");
        String path = basePath + ".chat";

        if (section == null) {

            section = parent.getConfigurationSection("message");

            if (section != null) {
                consoleService.log(ConsoleMessage.LEGACY_PATH,
                        "path", basePath + ".message",
                        "replacement", path);
                path = basePath + ".message";
            }

        }

        if (section == null) {
            return defaults;
        }

        boolean enabled = section.getBoolean("enabled", defaults.enabled());
        String format = readFormat(section, path + ".format", defaults.format());

        if (!interactive) {
            return new ChatSettings(enabled, format == null ? "" : format, null, null, null);
        }

        String hover = blankToNull(section.getString("hover"));
        String clickValue = blankToNull(section.getString("click"));
        ClickEvent.Action action = readClickAction(section, path);

        if (action != null) {

            if (clickValue == null) {

                consoleService.log(ConsoleMessage.CLICK_IGNORED,
                        "path", path + ".click_action",
                        "reason", "no click value is set");
                action = null;

            } else if (action == ClickEvent.Action.OPEN_URL && !isValidUrl(clickValue)) {

                consoleService.log(ConsoleMessage.CLICK_IGNORED,
                        "path", path + ".click",
                        "reason", "not a valid http(s) URL: " + clickValue);
                action = null;

            }

        }

        if (action == ClickEvent.Action.RUN_COMMAND || action == ClickEvent.Action.SUGGEST_COMMAND) {
            clickValue = clickValue.startsWith("/") ? clickValue : "/" + clickValue;
        }

        if (action == null) {
            clickValue = null;
        }

        return new ChatSettings(enabled, format == null ? "" : format, hover, action, clickValue);

    }

    /**
     * Читает текст сообщения: только строка. Список или число на месте
     * format означают опечатку в файле - Bukkit привел бы их к строке
     * вида "[line]", которая и ушла бы игрокам буквально.
     */
    private @NotNull String readFormat(@NotNull ConfigurationSection section,
                                       @NotNull String path,
                                       @NotNull String defaultValue) {

        Object raw = section.get("format");

        if (raw == null) {
            return defaultValue;
        }

        if (raw instanceof String text) {
            return text;
        }

        consoleService.log(ConsoleMessage.INVALID_VALUE,
                "path", path,
                "value", String.valueOf(raw),
                "defaultValue", defaultValue);
        return defaultValue;

    }

    private @Nullable ClickEvent.Action readClickAction(@NotNull ConfigurationSection section,
                                                        @NotNull String path) {

        Object raw = section.get("click_action");
        String keyPath = path + ".click_action";

        if (raw == null) {

            raw = section.get("clickAction");

            if (raw != null) {
                consoleService.log(ConsoleMessage.LEGACY_PATH,
                        "path", path + ".clickAction",
                        "replacement", keyPath);
                keyPath = path + ".clickAction";
            }

        }

        if (raw == null) {
            return null;
        }

        String name = String.valueOf(raw).trim().toUpperCase(Locale.ROOT);

        if (name.isEmpty()) {
            return null;
        }

        try {
            return ClickEvent.Action.valueOf(name);
        } catch (IllegalArgumentException exception) {

            consoleService.log(ConsoleMessage.INVALID_VALUE,
                    "path", keyPath,
                    "value", String.valueOf(raw),
                    "defaultValue", "none");
            return null;

        }

    }

    private boolean isValidUrl(@NotNull String value) {

        try {

            URI uri = new URI(value.trim());
            String scheme = uri.getScheme();

            return "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);

        } catch (URISyntaxException exception) {
            return false;
        }

    }

    private @NotNull TitleSettings buildTitle(@NotNull ConfigurationSection parent,
                                              @NotNull String basePath,
                                              @NotNull TitleSettings defaults) {

        ConfigurationSection section = parent.getConfigurationSection("title");

        if (section == null) {
            return defaults;
        }

        String path = basePath + ".title";

        return new TitleSettings(
                section.getBoolean("enabled", defaults.enabled()),
                section.getString("title", defaults.title()),
                section.getString("subtitle", defaults.subtitle()),
                parseNonNegativeInt(readWithLegacy(section, "fade_in", "fadeIn", path),
                        defaults.fadeIn(), path + ".fade_in"),
                parseNonNegativeInt(section.get("stay"), defaults.stay(), path + ".stay"),
                parseNonNegativeInt(readWithLegacy(section, "fade_out", "fadeOut", path),
                        defaults.fadeOut(), path + ".fade_out")
        );

    }

    private @NotNull SoundSettings buildSound(@NotNull ConfigurationSection parent,
                                              @NotNull String basePath,
                                              @NotNull SoundSettings defaults) {

        ConfigurationSection section = parent.getConfigurationSection("sound");

        if (section == null) {
            return defaults;
        }

        String path = basePath + ".sound";

        return new SoundSettings(
                section.getBoolean("enabled", defaults.enabled()),
                parseSound(section.get("name"), path + ".name", defaults.sound()),
                parseNonNegativeFloat(section.get("volume"), defaults.volume(), path + ".volume"),
                parseNonNegativeFloat(section.get("pitch"), defaults.pitch(), path + ".pitch")
        );

    }

    private @NotNull BossBarSettings buildBossBar(@NotNull ConfigurationSection parent,
                                                  @NotNull String basePath,
                                                  @NotNull BossBarSettings defaults) {

        ConfigurationSection section = parent.getConfigurationSection("bossbar");

        if (section == null) {
            return defaults;
        }

        String path = basePath + ".bossbar";

        return new BossBarSettings(
                section.getBoolean("enabled", defaults.enabled()),
                section.getString("text", defaults.text()),
                parseBarColor(section.get("color"), path + ".color", defaults.color()),
                parseBarStyle(section.get("style"), path + ".style", defaults.style()),
                parsePositiveInt(section.get("duration"), defaults.duration(), path + ".duration"),
                section.getBoolean(readLegacyKey(section, "progress_decay", "progress-decay", path),
                        defaults.progressDecay())
        );

    }

    /**
     * Возвращает значение нового ключа, иначе старого с подсказкой LEGACY_PATH.
     */
    private @Nullable Object readWithLegacy(@NotNull ConfigurationSection section,
                                            @NotNull String key,
                                            @NotNull String legacyKey,
                                            @NotNull String path) {

        Object value = section.get(key);

        if (value != null) {
            return value;
        }

        Object legacy = section.get(legacyKey);

        if (legacy != null) {
            consoleService.log(ConsoleMessage.LEGACY_PATH,
                    "path", path + "." + legacyKey,
                    "replacement", path + "." + key);
        }

        return legacy;

    }

    /**
     * Возвращает имя ключа для getBoolean: новый ключ, иначе старый с
     * подсказкой LEGACY_PATH (для булевых значений, где null и false
     * неразличимы на уровне section.get).
     */
    private @NotNull String readLegacyKey(@NotNull ConfigurationSection section,
                                          @NotNull String key,
                                          @NotNull String legacyKey,
                                          @NotNull String path) {

        if (section.isSet(key)) {
            return key;
        }

        if (section.isSet(legacyKey)) {

            consoleService.log(ConsoleMessage.LEGACY_PATH,
                    "path", path + "." + legacyKey,
                    "replacement", path + "." + key);
            return legacyKey;

        }

        return key;

    }

    /**
     * Разрешает имя звука один раз при загрузке снапшота.
     *
     * В 1.x Sound.valueOf вызывался на каждую доставку и неизвестное имя
     * роняло IllegalArgumentException прямо в обработчик события.
     */
    private @Nullable Sound parseSound(@Nullable Object raw, @NotNull String path,
                                       @Nullable Sound defaultSound) {

        if (raw == null) {
            return defaultSound;
        }

        String name = String.valueOf(raw).trim().toUpperCase(Locale.ROOT);

        try {
            return Sound.valueOf(name);
        } catch (IllegalArgumentException exception) {

            consoleService.log(ConsoleMessage.INVALID_VALUE,
                    "path", path,
                    "value", String.valueOf(raw),
                    "defaultValue", String.valueOf(defaultSound));
            return defaultSound;

        }

    }

    /**
     * Звук дефолтов модулей. api-version '1.20' гарантирует, что все три
     * имени (ENTITY_PLAYER_LEVELUP, ENTITY_ENDERMAN_TELEPORT,
     * ENTITY_GENERIC_EXPLODE) есть в перечислении - они там с 1.13,
     * поэтому защитного catch здесь нет.
     */
    private static @NotNull Sound resolveSound(@NotNull String name) {
        return Sound.valueOf(name);
    }

    private @NotNull BarColor parseBarColor(@Nullable Object raw, @NotNull String path,
                                            @NotNull BarColor defaultColor) {

        if (raw == null) {
            return defaultColor;
        }

        try {
            return BarColor.valueOf(String.valueOf(raw).trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {

            consoleService.log(ConsoleMessage.INVALID_VALUE,
                    "path", path,
                    "value", String.valueOf(raw),
                    "defaultValue", defaultColor.name());
            return defaultColor;

        }

    }

    private @NotNull BarStyle parseBarStyle(@Nullable Object raw, @NotNull String path,
                                            @NotNull BarStyle defaultStyle) {

        if (raw == null) {
            return defaultStyle;
        }

        try {
            return BarStyle.valueOf(String.valueOf(raw).trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {

            consoleService.log(ConsoleMessage.INVALID_VALUE,
                    "path", path,
                    "value", String.valueOf(raw),
                    "defaultValue", defaultStyle.name());
            return defaultStyle;

        }

    }

    private int parsePositiveInt(@Nullable Object raw, int defaultValue, @NotNull String path) {

        if (raw == null) {
            return defaultValue;
        }

        try {

            int value = Integer.parseInt(String.valueOf(raw).trim());

            if (value >= 1) {
                return value;
            }

        } catch (NumberFormatException exception) {
            // уходим в предупреждение ниже
        }

        consoleService.log(ConsoleMessage.INVALID_VALUE,
                "path", path,
                "value", String.valueOf(raw),
                "defaultValue", String.valueOf(defaultValue));
        return defaultValue;

    }

    private int parseNonNegativeInt(@Nullable Object raw, int defaultValue, @NotNull String path) {

        if (raw == null) {
            return defaultValue;
        }

        try {

            int value = Integer.parseInt(String.valueOf(raw).trim());

            if (value >= 0) {
                return value;
            }

        } catch (NumberFormatException exception) {
            // уходим в предупреждение ниже
        }

        consoleService.log(ConsoleMessage.INVALID_VALUE,
                "path", path,
                "value", String.valueOf(raw),
                "defaultValue", String.valueOf(defaultValue));
        return defaultValue;

    }

    private float parseNonNegativeFloat(@Nullable Object raw, float defaultValue,
                                        @NotNull String path) {

        if (raw == null) {
            return defaultValue;
        }

        try {

            double value = Double.parseDouble(String.valueOf(raw).trim());

            if (value >= 0.0 && Double.isFinite(value)) {
                return (float) value;
            }

        } catch (NumberFormatException exception) {
            // уходим в предупреждение ниже
        }

        consoleService.log(ConsoleMessage.INVALID_VALUE,
                "path", path,
                "value", String.valueOf(raw),
                "defaultValue", String.valueOf(defaultValue));
        return defaultValue;

    }

    private @NotNull String readReloadPermission() {

        String raw = config.getString(PATH_PERMISSION_RELOAD);

        if (raw != null && !raw.isBlank()) {
            return raw;
        }

        if (raw != null) {
            consoleService.log(ConsoleMessage.INVALID_VALUE,
                    "path", PATH_PERMISSION_RELOAD,
                    "value", raw,
                    "defaultValue", PermissionNode.RELOAD.getDefaultPermission());
            return PermissionNode.RELOAD.getDefaultPermission();
        }

        for (String legacyPath : PermissionNode.RELOAD.getLegacyPaths()) {

            String legacy = config.getString(legacyPath);

            if (legacy != null && !legacy.isBlank()) {

                consoleService.log(ConsoleMessage.LEGACY_PATH,
                        "path", legacyPath,
                        "replacement", PATH_PERMISSION_RELOAD);
                return legacy;

            }

        }

        return PermissionNode.RELOAD.getDefaultPermission();

    }

    private @NotNull MessagingSection buildMessagingSection() {

        Map<MessageKey, List<String>> messages = new EnumMap<>(MessageKey.class);

        for (MessageKey key : MessageKey.values()) {
            messages.put(key, readServiceMessage(key));
        }

        return new MessagingSection(
                config.getBoolean(PATH_MESSAGES_ENABLED, true),
                HexColorUtil.color(config.getString(PATH_MESSAGE_PREFIX, "")),
                Collections.unmodifiableMap(messages)
        );

    }

    /**
     * Читает служебное сообщение: секция 2.0 (enabled/text), плоская строка
     * на том же пути, иначе плоская строка 1.x по legacy-пути с подсказкой.
     */
    private @NotNull List<String> readServiceMessage(@NotNull MessageKey key) {

        String path = PATH_MESSAGES + key.getConfigKey();

        ConfigurationSection section = config.getConfigurationSection(path);

        if (section != null) {

            if (!section.getBoolean("enabled", key.isDefaultEnabled())) {
                return List.of();
            }

            return coloredText(section.get("text"), key.getDefaultMessage());

        }

        String flat = config.getString(path);

        if (flat != null) {
            return coloredLines(flat);
        }

        String legacyPath = key.getLegacyPath();

        if (legacyPath != null) {

            String legacy = config.getString(legacyPath);

            if (legacy != null) {

                consoleService.log(ConsoleMessage.LEGACY_PATH,
                        "path", legacyPath,
                        "replacement", path);
                return coloredLines(legacy);

            }

        }

        return coloredLines(key.getDefaultMessage());

    }

    private @NotNull List<String> coloredText(@Nullable Object raw, @NotNull String defaultMessage) {

        if (raw == null) {
            return coloredLines(defaultMessage);
        }

        if (raw instanceof List<?> list) {

            List<String> lines = new ArrayList<>(list.size());

            for (Object element : list) {
                if (element != null) {
                    lines.add(HexColorUtil.color(String.valueOf(element)));
                }
            }

            return Collections.unmodifiableList(lines);

        }

        return coloredLines(String.valueOf(raw));

    }

    /**
     * Окрашивает текст и режет его на строки.
     *
     * Многострочные дефолты (usage, info) живут одним значением с \n,
     * а отправляются списком: так один и тот же текст одинаково читается и в
     * коде, и в config.yml, где он задан списком строк. Лимит -1 у split
     * обязателен - без него хвостовые пустые строки теряются, и дефолт
     * сообщения расходится с тем, что лежит в файле.
     */
    private static @NotNull List<String> coloredLines(@NotNull String text) {
        return List.of(HexColorUtil.color(text).split("\n", -1));
    }

    private static @Nullable String blankToNull(@Nullable String value) {
        return value == null || value.isBlank() ? null : value;
    }

}
