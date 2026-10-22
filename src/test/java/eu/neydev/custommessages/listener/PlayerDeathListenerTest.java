package eu.neydev.custommessages.listener;

import eu.neydev.custommessages.config.type.ChatSettings;
import eu.neydev.custommessages.config.type.MessageModule;
import eu.neydev.custommessages.config.type.NotificationBundle;
import eu.neydev.custommessages.service.Notifier;
import eu.neydev.custommessages.support.StubConfig;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Слушатель смерти: замена ванильного сообщения, плейсхолдер причины
 * и отсутствие принудительного респавна (дефект 1.x).
 */
class PlayerDeathListenerTest {

    private final Player player = mock(Player.class);
    private final Notifier notifier = mock(Notifier.class);
    private StubConfig config;

    private final NotificationBundle bundle = new NotificationBundle(
            new ChatSettings(true, "{player} died: {death_cause}", null, null, null),
            NotificationBundle.EMPTY.title(),
            NotificationBundle.EMPTY.sound(),
            NotificationBundle.EMPTY.bossBar());

    @BeforeEach
    void setUp() {

        when(player.getName()).thenReturn("Steve");
        config = new StubConfig().withBundle(MessageModule.DEATH, bundle);

    }

    private PlayerDeathEvent deathEvent(String vanillaMessage) {
        return new PlayerDeathEvent(player, mock(DamageSource.class),
                List.<ItemStack>of(), 0, vanillaMessage);
    }

    @Test
    @DisplayName("Ванильное сообщение снимается, причина уходит в плейсхолдер")
    void deathReplacesMessageAndCarriesTheCause() {

        PlayerDeathEvent event = deathEvent("Steve fell from a high place");

        new PlayerDeathListener(config, notifier).onDeath(event);

        assertNull(event.getDeathMessage(), "ванильное сообщение снято");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> placeholders = ArgumentCaptor.forClass(Map.class);

        verify(notifier).notifyModule(eq(player), eq(bundle), placeholders.capture());
        assertEquals("Steve", placeholders.getValue().get("{player}"));
        assertEquals("Steve fell from a high place", placeholders.getValue().get("{death_cause}"));

    }

    @Test
    @DisplayName("Null-причина заменяется словом unknown")
    void nullCauseBecomesUnknown() {

        PlayerDeathEvent event = deathEvent(null);

        new PlayerDeathListener(config, notifier).onDeath(event);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> placeholders = ArgumentCaptor.forClass(Map.class);

        verify(notifier).notifyModule(eq(player), eq(bundle), placeholders.capture());
        assertEquals("unknown", placeholders.getValue().get("{death_cause}"));

    }

    @Test
    @DisplayName("Выключенный модуль смерти оставляет ванильное сообщение")
    void disabledDeathModuleKeepsVanillaMessage() {

        config.withModuleEnabled(MessageModule.DEATH, false);
        PlayerDeathEvent event = deathEvent("Steve fell");

        new PlayerDeathListener(config, notifier).onDeath(event);

        assertEquals("Steve fell", event.getDeathMessage());
        verifyNoInteractions(notifier);

    }

    @Test
    @DisplayName("Мастер-выключатель плагина глушит модуль смерти")
    void disabledPluginKeepsVanillaDeathMessage() {

        config.withPluginEnabled(false);
        PlayerDeathEvent event = deathEvent("Steve fell");

        new PlayerDeathListener(config, notifier).onDeath(event);

        assertEquals("Steve fell", event.getDeathMessage());
        verifyNoInteractions(notifier);

    }

}
