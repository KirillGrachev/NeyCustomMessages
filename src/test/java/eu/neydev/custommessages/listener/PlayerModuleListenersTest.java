package eu.neydev.custommessages.listener;

import eu.neydev.custommessages.config.type.ChatSettings;
import eu.neydev.custommessages.config.type.MessageModule;
import eu.neydev.custommessages.config.type.NotificationBundle;
import eu.neydev.custommessages.service.Notifier;
import eu.neydev.custommessages.support.StubConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Слушатели событий игрока: включенный модуль ЗАМЕНЯет ванильное
 * сообщение и доставляет комплект, выключенный модуль или выключенный
 * плагин оставляют ванильное сообщение нетронутым.
 */
class PlayerModuleListenersTest {

    private final Player player = mock(Player.class);
    private final Notifier notifier = mock(Notifier.class);
    private StubConfig config;

    private final NotificationBundle joinBundle = new NotificationBundle(
            new ChatSettings(true, "joined {player}", null, null, null),
            NotificationBundle.EMPTY.title(),
            NotificationBundle.EMPTY.sound(),
            NotificationBundle.EMPTY.bossBar());

    @BeforeEach
    void setUp() {

        when(player.getName()).thenReturn("Steve");
        config = new StubConfig().withBundle(MessageModule.JOIN, joinBundle)
                .withBundle(MessageModule.QUIT, joinBundle)
                .withBundle(MessageModule.DEATH, joinBundle);

    }

    @Test
    @DisplayName("Вход: ванильное сообщение заменяется, комплект доставляется")
    void joinReplacesVanillaMessageAndDelivers() {

        PlayerJoinEvent event = new PlayerJoinEvent(player, "Steve joined the game");

        new PlayerJoinListener(config, notifier).onJoin(event);

        assertNull(event.getJoinMessage(), "ванильное сообщение снято");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> placeholders = ArgumentCaptor.forClass(Map.class);

        verify(notifier).notifyModule(eq(player), eq(joinBundle), placeholders.capture());
        assertEquals("Steve", placeholders.getValue().get("{player}"));

    }

    @Test
    @DisplayName("Выключенный модуль входа оставляет ванильное сообщение")
    void disabledJoinModuleKeepsVanillaMessage() {

        config.withModuleEnabled(MessageModule.JOIN, false);
        PlayerJoinEvent event = new PlayerJoinEvent(player, "Steve joined the game");

        new PlayerJoinListener(config, notifier).onJoin(event);

        assertEquals("Steve joined the game", event.getJoinMessage());
        verifyNoInteractions(notifier);

    }

    @Test
    @DisplayName("Мастер-выключатель плагина глушит модуль входа")
    void disabledPluginKeepsVanillaJoinMessage() {

        config.withPluginEnabled(false);
        PlayerJoinEvent event = new PlayerJoinEvent(player, "Steve joined the game");

        new PlayerJoinListener(config, notifier).onJoin(event);

        assertEquals("Steve joined the game", event.getJoinMessage());
        verifyNoInteractions(notifier);

    }

    @Test
    @DisplayName("Выход: ванильное сообщение заменяется, комплект доставляется")
    void quitReplacesVanillaMessageAndDelivers() {

        PlayerQuitEvent event = new PlayerQuitEvent(player, "Steve left the game");

        new PlayerQuitListener(config, notifier).onQuit(event);

        assertNull(event.getQuitMessage());
        verify(notifier).notifyModule(eq(player), eq(joinBundle), any());

    }

    @Test
    @DisplayName("Выключенный модуль выхода оставляет ванильное сообщение")
    void disabledQuitModuleKeepsVanillaMessage() {

        config.withModuleEnabled(MessageModule.QUIT, false);
        PlayerQuitEvent event = new PlayerQuitEvent(player, "Steve left the game");

        new PlayerQuitListener(config, notifier).onQuit(event);

        assertEquals("Steve left the game", event.getQuitMessage());
        verifyNoInteractions(notifier);

    }

}
