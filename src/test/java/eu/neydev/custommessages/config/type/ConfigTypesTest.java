package eu.neydev.custommessages.config.type;

import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Контракты перечислений и рекордов конфигурации: уникальные ключи,
 * непустые дефолты, совпадение с plugin.yml и логика isPlayable/hasContent.
 */
class ConfigTypesTest {

    @Test
    @DisplayName("Модули уникальны и покрывают три события игрока")
    void modulesAreUnique() {

        Set<String> keys = new HashSet<>();

        for (MessageModule module : MessageModule.values()) {
            assertTrue(keys.add(module.getConfigKey()), "дубль ключа " + module.getConfigKey());
        }

        assertEquals(3, MessageModule.values().length);

    }

    @Test
    @DisplayName("Ключи служебных сообщений уникальны и имеют непустые дефолты")
    void messageKeysAreUnique() {

        Set<String> keys = new HashSet<>();

        for (MessageKey key : MessageKey.values()) {

            assertTrue(keys.add(key.getConfigKey()), "дубль ключа " + key.getConfigKey());
            assertFalse(key.getConfigKey().isBlank(), "пустой ключ у " + key);
            assertNotNull(key.getDefaultMessage(), "нет дефолта у " + key);
            assertFalse(key.getDefaultMessage().isBlank(), "пустой дефолт у " + key);
            assertTrue(key.isDefaultEnabled(), "сообщение выключено по умолчанию: " + key);

        }

        assertEquals(MessageKey.values().length, keys.size());

    }

    @Test
    @DisplayName("Legacy-пути помечены ровно у трех текстов команд 1.x")
    void onlyCommandTextsCarryLegacyPaths() {

        assertNotNull(MessageKey.USAGE.getLegacyPath());
        assertNotNull(MessageKey.RELOAD.getLegacyPath());
        assertNotNull(MessageKey.NO_PERMISSION.getLegacyPath());

        for (MessageKey key : MessageKey.values()) {
            if (key != MessageKey.USAGE && key != MessageKey.RELOAD && key != MessageKey.NO_PERMISSION) {
                assertNull(key.getLegacyPath(), "неожиданный legacy-путь у " + key);
            }
        }

    }

    @Test
    @DisplayName("Узел права совпадает с plugin.yml и помнит оба пути 1.x")
    void permissionNodeMatchesPluginYml() {

        assertEquals("reload", PermissionNode.RELOAD.getConfigKey());
        assertEquals("neycustommessages.reload", PermissionNode.RELOAD.getDefaultPermission());
        assertEquals(1, PermissionNode.values().length);

        String[] legacy = PermissionNode.RELOAD.getLegacyPaths();

        assertNotNull(legacy);
        assertEquals(2, legacy.length, "в 1.x право задавалось в двух местах");

    }

    @Test
    @DisplayName("Шаблоны консоли непустые, диагностические - со своими плейсхолдерами")
    void consoleTemplatesCarryPlaceholders() {

        for (ConsoleMessage message : ConsoleMessage.values()) {
            assertFalse(message.getDefaultTemplate().isBlank(), "пустой шаблон у " + message);
        }

        assertTrue(ConsoleMessage.INVALID_VALUE.getDefaultTemplate().contains("{path}"));
        assertTrue(ConsoleMessage.INVALID_VALUE.getDefaultTemplate().contains("{value}"));
        assertTrue(ConsoleMessage.INVALID_VALUE.getDefaultTemplate().contains("{defaultValue}"));
        assertTrue(ConsoleMessage.LEGACY_PATH.getDefaultTemplate().contains("{replacement}"));
        assertTrue(ConsoleMessage.COMMAND_MISSING.getDefaultTemplate().contains("{command}"));
        assertTrue(ConsoleMessage.COMMAND_UNREGISTER_FAILED.getDefaultTemplate().contains("{reason}"));
        assertTrue(ConsoleMessage.CLICK_IGNORED.getDefaultTemplate().contains("{reason}"));

    }

    @Test
    @DisplayName("Звук играется только при включенной настройке и разрешенном имени")
    void soundIsPlayableOnlyWhenEnabledAndResolved() {

        assertTrue(new SoundSettings(true, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f).isPlayable());
        assertFalse(new SoundSettings(false, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f).isPlayable());
        assertFalse(new SoundSettings(true, null, 1.0f, 1.0f).isPlayable());
        assertFalse(SoundSettings.EMPTY.isPlayable());

    }

    @Test
    @DisplayName("Титр с обоими пустыми текстами не считается контентом")
    void titleContentRequiresText() {

        assertFalse(new TitleSettings(true, "", "", 1, 1, 1).hasContent());
        assertTrue(new TitleSettings(true, "t", "", 1, 1, 1).hasContent());
        assertTrue(new TitleSettings(true, "", "s", 1, 1, 1).hasContent());
        assertFalse(TitleSettings.EMPTY.hasContent());

    }

    @Test
    @DisplayName("Интерактивность чата определяется hover или click")
    void chatInteractivityIsDetected() {

        assertFalse(new ChatSettings(true, "t", null, null, null).isInteractive());
        assertTrue(new ChatSettings(true, "t", "hint", null, null).isInteractive());
        assertTrue(new ChatSettings(true, "t", null,
                ClickEvent.Action.OPEN_URL, "https://x").isInteractive());
        assertFalse(ChatSettings.EMPTY.isInteractive());

    }

    @Test
    @DisplayName("Пустой комплект выключен целиком")
    void emptyBundleIsSilent() {

        NotificationBundle empty = NotificationBundle.EMPTY;

        assertFalse(empty.chat().enabled());
        assertFalse(empty.title().enabled());
        assertFalse(empty.sound().isPlayable());
        assertFalse(empty.bossBar().enabled());

    }

    @Test
    @DisplayName("Дефолты босс-бара валидны на живых перечислениях Bukkit")
    void bossBarDefaultsAreLiveEnums() {

        BossBarSettings defaults = BossBarSettings.EMPTY;

        assertEquals(BarColor.YELLOW, defaults.color());
        assertEquals(BarStyle.SOLID, defaults.style());

    }

}
