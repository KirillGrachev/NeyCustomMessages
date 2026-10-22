package eu.neydev.custommessages.config;

import eu.neydev.custommessages.NeyCustomMessages;
import eu.neydev.custommessages.config.type.Announcement;
import eu.neydev.custommessages.config.type.MessageKey;
import eu.neydev.custommessages.config.type.MessageModule;
import eu.neydev.custommessages.config.type.NotificationBundle;
import eu.neydev.custommessages.config.type.PermissionNode;
import eu.neydev.custommessages.service.ConsoleService;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Проверка менеджера конфигурации на настоящем YAML-файле: дефолты,
 * чтение секций, устаревшие пути и ключи 1.x, валидация кликов и значений,
 * битый YAML, предупреждения и перезагрузка.
 *
 * Сервер не поднимается: плагин мокается, getDataFolder указывает на
 * временный каталог, а getConfig перечитывает файл на каждый вызов - так
 * reload() менеджера ведет себя как на живом ядре.
 */
class ConfigManagerTest {

    @TempDir
    Path tempDir;

    private final List<LogRecord> consoleRecords = new ArrayList<>();

    private NeyCustomMessages plugin() {

        NeyCustomMessages plugin = mock(NeyCustomMessages.class);

        when(plugin.getDataFolder()).thenReturn(tempDir.toFile());
        when(plugin.getLogger()).thenReturn(countingLogger());
        when(plugin.getConfig()).thenAnswer(answer -> loadFromDisk());
        return plugin;

    }

    private ConfigManager manager(String yaml) {

        writeConfig(yaml);

        NeyCustomMessages plugin = plugin();
        return new ConfigManager(plugin, new ConsoleService(plugin));

    }

    private YamlConfiguration loadFromDisk() {

        YamlConfiguration loaded = new YamlConfiguration();

        try {
            loaded.load(tempDir.resolve("config.yml").toFile());
        } catch (Exception ignored) {
            // Битый или отсутствующий файл: пустая конфигурация, как в продакшене
        }

        return loaded;

    }

    private Logger countingLogger() {

        Logger logger = Logger.getLogger("cm-config-test-" + System.nanoTime());

        logger.setUseParentHandlers(false);
        logger.addHandler(new Handler() {

            @Override
            public void publish(LogRecord record) {
                consoleRecords.add(record);
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }

        });

        return logger;

    }

    private void writeConfig(String yaml) {
        try {
            Files.writeString(tempDir.resolve("config.yml"), yaml);
        } catch (IOException exception) {
            throw new IllegalStateException("Не удалось записать тестовый config.yml", exception);
        }
    }

    private boolean hasRecord(Level level, String token) {
        return consoleRecords.stream()
                .filter(record -> record.getLevel().equals(level))
                .anyMatch(record -> String.valueOf(record.getMessage()).contains(token));
    }

    @Test
    @DisplayName("Пустой файл дает дефолты, совпадающие с поставляемым конфигом")
    void emptyFileFallsBackToCodeDefaults() {

        ConfigManager manager = manager("");

        assertTrue(manager.isPluginEnabled());
        assertTrue(manager.arePermissionsEnabled());
        assertTrue(manager.areMessagesEnabled());
        assertEquals("neycustommessages.reload", manager.getPermission(PermissionNode.RELOAD));

        for (MessageModule module : MessageModule.values()) {
            assertTrue(manager.isModuleEnabled(module));
        }

        NotificationBundle join = manager.getModuleBundle(MessageModule.JOIN);
        assertTrue(join.chat().enabled());
        assertEquals("&a[+] &e{player} &7joined the server!", join.chat().format(),
                "тексты модулей хранятся неокрашенными");
        assertTrue(join.title().enabled());
        assertEquals("&aWelcome!", join.title().title());
        assertEquals(70, join.title().stay());
        assertEquals(Sound.ENTITY_PLAYER_LEVELUP, join.sound().sound());
        assertTrue(join.bossBar().enabled());
        assertEquals(BarColor.GREEN, join.bossBar().color());

        NotificationBundle death = manager.getModuleBundle(MessageModule.DEATH);
        assertEquals(Sound.ENTITY_GENERIC_EXPLODE, death.sound().sound());
        assertEquals(BarColor.RED, death.bossBar().color());
        assertEquals(1.2f, death.sound().pitch());

        assertTrue(manager.areAnnouncementsEnabled());
        assertEquals(15, manager.getAnnouncementInterval());
        assertTrue(manager.isRandomAnnouncementOrder());
        assertEquals(List.of(), manager.getAnnouncements());

        assertEquals(3, manager.getMessages(MessageKey.USAGE).size(),
                "заголовок и две команды");
        assertFalse(hasRecord(Level.SEVERE, "broken"), "пустой файл - не поломка");
        assertFalse(hasRecord(Level.WARNING, "Legacy"), "нечего мигрировать");

    }

    @Test
    @DisplayName("Отсутствующий файл не считается битым")
    void missingFileIsNotBroken() {

        NeyCustomMessages plugin = plugin();
        ConfigManager manager = new ConfigManager(plugin, new ConsoleService(plugin));

        assertTrue(manager.isPluginEnabled());
        assertFalse(hasRecord(Level.SEVERE, "broken"));

    }

    @Test
    @DisplayName("Все секции v2 читаются из файла")
    void readsEverySection() {

        ConfigManager manager = manager("""
                settings:
                  enabled: false
                permissions:
                  enabled: false
                  reload: "cm.reload"
                modules:
                  join:
                    enabled: false
                    chat:
                      enabled: false
                      format: "bye {player}"
                    title:
                      enabled: true
                      title: "T"
                      subtitle: "S"
                      fade_in: 1
                      stay: 2
                      fade_out: 3
                    sound:
                      enabled: true
                      name: BLOCK_NOTE_BLOCK_PLING
                      volume: 0.5
                      pitch: 1.5
                    bossbar:
                      enabled: true
                      text: "bar {player}"
                      color: PURPLE
                      style: SEGMENTED_10
                      duration: 9
                      progress_decay: true
                announcements:
                  enabled: true
                  interval: 30
                  random: false
                  list:
                    first:
                      chat:
                        enabled: true
                        format: "&6ad"
                        hover: "hint"
                        click: "site"
                        click_action: RUN_COMMAND
                    second:
                      title:
                        enabled: true
                        title: "thanks"
                messages:
                  prefix: "&8[PFX]"
                  usage:
                    enabled: true
                    text: "use /ncm"
                """);

        assertFalse(manager.isPluginEnabled());
        assertFalse(manager.arePermissionsEnabled());
        assertEquals("cm.reload", manager.getPermission(PermissionNode.RELOAD));
        assertFalse(manager.isModuleEnabled(MessageModule.JOIN));

        NotificationBundle join = manager.getModuleBundle(MessageModule.JOIN);
        assertFalse(join.chat().enabled());
        assertEquals("bye {player}", join.chat().format());
        assertEquals(1, join.title().fadeIn());
        assertEquals(2, join.title().stay());
        assertEquals(3, join.title().fadeOut());
        assertEquals(Sound.BLOCK_NOTE_BLOCK_PLING, join.sound().sound());
        assertEquals(0.5f, join.sound().volume());
        assertEquals(1.5f, join.sound().pitch());
        assertEquals("bar {player}", join.bossBar().text());
        assertEquals(BarColor.PURPLE, join.bossBar().color());
        assertEquals(BarStyle.SEGMENTED_10, join.bossBar().style());
        assertEquals(9, join.bossBar().duration());
        assertTrue(join.bossBar().progressDecay());

        assertEquals(30, manager.getAnnouncementInterval());
        assertFalse(manager.isRandomAnnouncementOrder());

        List<Announcement> announcements = manager.getAnnouncements();
        assertEquals(List.of("first", "second"),
                announcements.stream().map(Announcement::id).toList(),
                "порядок объявления в файле сохранен");

        Announcement first = announcements.get(0);
        assertEquals("&6ad", first.bundle().chat().format());
        assertEquals("hint", first.bundle().chat().hover());
        assertEquals(ClickEvent.Action.RUN_COMMAND, first.bundle().chat().clickAction());
        assertEquals("/site", first.bundle().chat().clickValue(),
                "команда без слэша получает слэш");

        Announcement second = announcements.get(1);
        assertFalse(second.bundle().chat().enabled(), "незаданный чат анонса выключен");
        assertTrue(second.bundle().title().enabled());
        assertEquals("thanks", second.bundle().title().title());
        assertFalse(second.bundle().sound().enabled());
        assertFalse(second.bundle().bossBar().enabled());

        assertEquals("§8[PFX]", manager.getMessagePrefix());
        assertEquals(List.of("use /ncm"), manager.getMessages(MessageKey.USAGE));

        assertFalse(hasRecord(Level.WARNING, "Legacy"), "новый формат - без предупреждений");
        assertFalse(hasRecord(Level.WARNING, "Invalid"), "все значения корректны");

    }

    @Test
    @DisplayName("Секции модулей 1.x на верхнем уровне читаются с подсказкой")
    void legacyTopLevelModulesAreRead() {

        ConfigManager manager = manager("""
                join:
                  enabled: true
                  message:
                    enabled: true
                    format: "&a{player} came"
                  title:
                    enabled: true
                    title: "hi"
                    subtitle: ""
                    fadeIn: 3
                    stay: 4
                    fadeOut: 5
                """);

        NotificationBundle join = manager.getModuleBundle(MessageModule.JOIN);

        assertEquals("&a{player} came", join.chat().format());
        assertEquals(3, join.title().fadeIn());
        assertEquals(5, join.title().fadeOut());
        assertEquals(Sound.ENTITY_PLAYER_LEVELUP, join.sound().sound(),
                "незаданный звук дополняется дефолтом модуля");

        assertTrue(hasRecord(Level.WARNING, "join.*"), "подсказка LEGACY_PATH на секцию модуля");
        assertTrue(hasRecord(Level.WARNING, "join.message"), "подсказка на переименованный ключ chat");
        assertTrue(hasRecord(Level.WARNING, "fadeIn"), "подсказка на переименованный ключ fade_in");

    }

    @Test
    @DisplayName("Секция announces 1.x читается как announcements")
    void legacyAnnouncesSectionIsRead() {

        ConfigManager manager = manager("""
                announces:
                  enabled: true
                  interval: 45
                  random: false
                  messages:
                    ad_1:
                      message:
                        enabled: true
                        format: "&6old ad"
                        hover: "old hint"
                        click: "https://t.me/NeyOff"
                        clickAction: OPEN_URL
                      bossbar:
                        enabled: true
                        text: "bar"
                        color: YELLOW
                        style: SOLID
                        duration: 8
                        progress-decay: true
                """);

        assertEquals(45, manager.getAnnouncementInterval());
        assertFalse(manager.isRandomAnnouncementOrder());

        List<Announcement> announcements = manager.getAnnouncements();
        assertEquals(1, announcements.size());
        assertEquals("ad_1", announcements.get(0).id());
        assertEquals("&6old ad", announcements.get(0).bundle().chat().format());
        assertEquals(ClickEvent.Action.OPEN_URL, announcements.get(0).bundle().chat().clickAction());
        assertTrue(announcements.get(0).bundle().bossBar().progressDecay());

        assertTrue(hasRecord(Level.WARNING, "announces.*"), "подсказка на секцию");
        assertTrue(hasRecord(Level.WARNING, "announces.messages.*"), "подсказка на список");
        assertTrue(hasRecord(Level.WARNING, "clickAction"), "подсказка на имя действия");
        assertTrue(hasRecord(Level.WARNING, "progress-decay"), "подсказка на имя флага");

    }

    @Test
    @DisplayName("Тексты и права команд 1.x читаются по legacy-путям")
    void legacyCommandTextsAreRead() {

        ConfigManager manager = manager("""
                commands:
                  ncm:
                    usage: "&6old usage"
                    permission: "old.reload"
                    subcommands:
                      reload:
                        message-success: "&aold success"
                        message-no-permission: "&cold denial"
                """);

        assertEquals(List.of("§6old usage"), manager.getMessages(MessageKey.USAGE));
        assertEquals(List.of("§aold success"), manager.getMessages(MessageKey.RELOAD));
        assertEquals(List.of("§cold denial"), manager.getMessages(MessageKey.NO_PERMISSION));
        assertEquals("old.reload", manager.getPermission(PermissionNode.RELOAD));

        assertTrue(hasRecord(Level.WARNING, "commands.ncm.usage"));
        assertTrue(hasRecord(Level.WARNING, "commands.ncm.permission"));

    }

    @Test
    @DisplayName("Новый путь побеждает legacy без предупреждения")
    void newPathWinsOverLegacy() {

        ConfigManager manager = manager("""
                messages:
                  usage:
                    enabled: true
                    text: "new usage"
                commands:
                  ncm:
                    usage: "old usage"
                """);

        assertEquals(List.of("new usage"), manager.getMessages(MessageKey.USAGE));
        assertFalse(hasRecord(Level.WARNING, "commands.ncm.usage"),
                "новый ключ на месте - подсказка не печатается");

    }

    @Test
    @DisplayName("Плоская строка на новом пути читается как текст")
    void flatStringOnNewPathIsRead() {

        ConfigManager manager = manager("""
                messages:
                  reload: "flat reload text"
                """);

        assertEquals(List.of("flat reload text"), manager.getMessages(MessageKey.RELOAD));
        assertFalse(hasRecord(Level.WARNING, "Legacy"));

    }

    @Test
    @DisplayName("Битые значения дают дефолты и предупреждения")
    void invalidValuesFallBackWithWarnings() {

        ConfigManager manager = manager("""
                permissions:
                  reload: " "
                modules:
                  join:
                    title:
                      fade_in: -5
                    sound:
                      name: NOT_A_SOUND
                      volume: -1
                    bossbar:
                      color: NOT_A_COLOR
                      style: XXX
                      duration: 0
                announcements:
                  interval: abc
                """);

        assertEquals("neycustommessages.reload", manager.getPermission(PermissionNode.RELOAD));

        NotificationBundle join = manager.getModuleBundle(MessageModule.JOIN);
        assertEquals(10, join.title().fadeIn());
        assertEquals(Sound.ENTITY_PLAYER_LEVELUP, join.sound().sound());
        assertEquals(1.0f, join.sound().volume());
        assertEquals(BarColor.GREEN, join.bossBar().color());
        assertEquals(BarStyle.SOLID, join.bossBar().style());
        assertEquals(5, join.bossBar().duration());

        assertEquals(15, manager.getAnnouncementInterval());

        assertTrue(hasRecord(Level.WARNING, "permissions.reload"));
        assertTrue(hasRecord(Level.WARNING, "modules.join.title.fade_in"));
        assertTrue(hasRecord(Level.WARNING, "modules.join.sound.name"));
        assertTrue(hasRecord(Level.WARNING, "modules.join.sound.volume"));
        assertTrue(hasRecord(Level.WARNING, "modules.join.bossbar.color"));
        assertTrue(hasRecord(Level.WARNING, "modules.join.bossbar.style"));
        assertTrue(hasRecord(Level.WARNING, "modules.join.bossbar.duration"));
        assertTrue(hasRecord(Level.WARNING, "announcements.interval"));

    }

    @Test
    @DisplayName("Клик без значения и не-URL игнорируются с предупреждением")
    void brokenClicksAreIgnored() {

        ConfigManager manager = manager("""
                announcements:
                  list:
                    no_value:
                      chat:
                        enabled: true
                        format: "a"
                        click_action: OPEN_URL
                    bad_url:
                      chat:
                        enabled: true
                        format: "b"
                        click: "not a url"
                        click_action: OPEN_URL
                    bad_action:
                      chat:
                        enabled: true
                        format: "c"
                        click: "/help"
                        click_action: TELEPORT
                """);

        List<Announcement> announcements = manager.getAnnouncements();

        assertNull(announcements.get(0).bundle().chat().clickAction(), "действие без значения");
        assertNull(announcements.get(1).bundle().chat().clickAction(), "не-URL");
        assertNull(announcements.get(2).bundle().chat().clickAction(), "неизвестное действие");

        assertTrue(hasRecord(Level.WARNING, "no click value"), String.join("|",
                consoleRecords.stream().map(r -> String.valueOf(r.getMessage())).toList()));
        assertTrue(hasRecord(Level.WARNING, "not a valid http(s) URL"));
        assertTrue(hasRecord(Level.WARNING, "TELEPORT"),
                "неизвестное действие названо в предупреждении");

    }

    @Test
    @DisplayName("SUGGEST_COMMAND без слэша получает слэш")
    void suggestCommandGetsLeadingSlash() {

        ConfigManager manager = manager("""
                announcements:
                  list:
                    ad:
                      chat:
                        enabled: true
                        format: "x"
                        click: "shop open"
                        click_action: SUGGEST_COMMAND
                """);

        assertEquals("/shop open",
                manager.getAnnouncements().get(0).bundle().chat().clickValue());

    }

    @Test
    @DisplayName("Битый YAML кричит в SEVERE и работает на дефолтах")
    void brokenYamlLogsSevere() {

        ConfigManager manager = manager("modules: [unclosed");

        assertTrue(hasRecord(Level.SEVERE, "broken"));
        assertTrue(manager.isPluginEnabled(), "дефолты подхватились");

    }

    @Test
    @DisplayName("Перезагрузка подхватывает правки файла и уведомляет подписчиков")
    void reloadPicksUpChangesAndNotifiesListeners() {

        ConfigManager manager = manager("announcements:\n  interval: 20");

        AtomicInteger notifications = new AtomicInteger();
        manager.onReload(notifications::incrementAndGet);

        assertEquals(20, manager.getAnnouncementInterval());

        writeConfig("announcements:\n  interval: 40");
        manager.reload();

        assertEquals(40, manager.getAnnouncementInterval());
        assertEquals(1, notifications.get());

    }

    @Test
    @DisplayName("Неполная секция модуля дополняется дефолтами повыводно")
    void partialModuleSectionFallsBackPerOutput() {

        ConfigManager manager = manager("""
                modules:
                  quit:
                    chat:
                      enabled: true
                      format: "gone {player}"
                """);

        NotificationBundle quit = manager.getModuleBundle(MessageModule.QUIT);

        assertEquals("gone {player}", quit.chat().format());
        assertEquals(Sound.ENTITY_ENDERMAN_TELEPORT, quit.sound().sound(),
                "звук из дефолта модуля, не пустой");
        assertFalse(quit.sound().enabled(), "включенность тоже из дефолта модуля");
        assertEquals(BarColor.BLUE, quit.bossBar().color());
        assertFalse(quit.title().enabled());

    }

    @Test
    @DisplayName("Служебные сообщения окрашены, тексты модулей сырые")
    void serviceMessagesAreColoredModuleTextsAreNot() {

        ConfigManager manager = manager("""
                messages:
                  usage:
                    enabled: true
                    text: "&euse /ncm"
                modules:
                  join:
                    chat:
                      enabled: true
                      format: "&ajoined"
                """);

        assertEquals(List.of("§euse /ncm"), manager.getMessages(MessageKey.USAGE));
        assertEquals("&ajoined", manager.getModuleBundle(MessageModule.JOIN).chat().format());

    }

    @Test
    @DisplayName("Выключенная секция сообщения дает пустой список")
    void disabledMessageSectionGivesEmptyList() {

        ConfigManager manager = manager("""
                messages:
                  info:
                    enabled: false
                    text: "hidden"
                """);

        assertEquals(List.of(), manager.getMessages(MessageKey.INFO));

    }

    @Test
    @DisplayName("Пустой click_action означает отсутствие действия без предупреждения")
    void emptyClickActionMeansNoAction() {

        ConfigManager manager = manager("""
                announcements:
                  list:
                    ad:
                      chat:
                        enabled: true
                        format: "x"
                        click: "https://example.com"
                        click_action: ""
                """);

        var chat = manager.getAnnouncements().get(0).bundle().chat();

        assertNull(chat.clickAction(), "пустая строка - действие не задано");
        assertNull(chat.clickValue(), "значение без действия не хранится");
        assertFalse(hasRecord(Level.WARNING, "Invalid"));

    }

    @Test
    @DisplayName("Скалярный элемент списка анонсов пропускается")
    void scalarAnnouncementEntryIsSkipped() {

        ConfigManager manager = manager("""
                announcements:
                  list:
                    broken: "just a string"
                    good:
                      chat:
                        enabled: true
                        format: "ok"
                """);

        List<Announcement> announcements = manager.getAnnouncements();

        assertEquals(1, announcements.size());
        assertEquals("good", announcements.get(0).id());

    }

    @Test
    @DisplayName("Секция чата без format и click_action дает пустые дефолты")
    void chatSectionWithoutKeysFallsBackToDefaults() {

        ConfigManager manager = manager("""
                announcements:
                  list:
                    ad:
                      chat:
                        enabled: true
                """);

        var chat = manager.getAnnouncements().get(0).bundle().chat();

        assertTrue(chat.enabled());
        assertEquals("", chat.format());
        assertNull(chat.clickAction());

    }

    @Test
    @DisplayName("Секции звука и босс-бара без ключей дополняются дефолтами модуля")
    void outputSectionsWithoutKeysUseModuleDefaults() {

        ConfigManager manager = manager("""
                modules:
                  join:
                    sound:
                      enabled: true
                    bossbar:
                      enabled: true
                      text: "hi"
                """);

        NotificationBundle join = manager.getModuleBundle(MessageModule.JOIN);

        assertEquals(Sound.ENTITY_PLAYER_LEVELUP, join.sound().sound());
        assertEquals(BarColor.GREEN, join.bossBar().color());
        assertEquals(BarStyle.SOLID, join.bossBar().style());
        assertFalse(hasRecord(Level.WARNING, "Invalid"));

    }

    @Test
    @DisplayName("Нечисловые fade_in и volume падают в дефолты с предупреждением")
    void nonNumericNumbersFallBack() {

        ConfigManager manager = manager("""
                modules:
                  join:
                    title:
                      fade_in: abc
                    sound:
                      volume: loud
                """);

        NotificationBundle join = manager.getModuleBundle(MessageModule.JOIN);

        assertEquals(10, join.title().fadeIn());
        assertEquals(1.0f, join.sound().volume());
        assertTrue(hasRecord(Level.WARNING, "modules.join.title.fade_in"));
        assertTrue(hasRecord(Level.WARNING, "modules.join.sound.volume"));

    }

    @Test
    @DisplayName("Секция сообщения без text дает дефолтный текст")
    void messageSectionWithoutTextFallsBackToDefault() {

        ConfigManager manager = manager("""
                messages:
                  info:
                    enabled: true
                """);

        assertTrue(manager.getMessages(MessageKey.INFO).get(0).contains("Version"));

    }

    @Test
    @DisplayName("Формат-список у чата анонса откатывается к пустой строке")
    void listShapedFormatFallsBackToEmpty() {

        ConfigManager manager = manager("""
                announcements:
                  list:
                    ad:
                      chat:
                        enabled: true
                        format:
                          - "line"
                """);

        assertEquals(1, manager.getAnnouncements().size());
        assertEquals("", manager.getAnnouncements().get(0).bundle().chat().format(),
                "список на месте строки откатывается к дефолту");
        assertTrue(hasRecord(Level.WARNING, ".chat.format"), "опечатка названа в предупреждении");

    }

}
