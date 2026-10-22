package eu.neydev.custommessages.service;

import eu.neydev.custommessages.config.type.PermissionNode;
import eu.neydev.custommessages.support.StubConfig;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Узел плагина один и он функциональный: выключенная система прав
 * разрешает всем, включенная спрашивает строку узла.
 */
class PermissionServiceTest {

    private final Player player = mock(Player.class);
    private final ConsoleCommandSender console = mock(ConsoleCommandSender.class);

    @Test
    @DisplayName("Выключенная система прав разрешает всем")
    void disabledSystemAllowsEveryone() {

        PermissionService service =
                new PermissionService(new StubConfig().withPermissionsEnabled(false));

        assertTrue(service.has(player, PermissionNode.RELOAD));
        assertTrue(service.has(null, PermissionNode.RELOAD), "даже без отправителя");

    }

    @Test
    @DisplayName("Включенная система прав спрашивает дефолтный узел")
    void enabledSystemChecksDefaultNode() {

        PermissionService service = new PermissionService(new StubConfig());
        when(player.hasPermission("neycustommessages.reload")).thenReturn(true);

        assertTrue(service.has(player, PermissionNode.RELOAD));
        verify(player).hasPermission("neycustommessages.reload");

    }

    @Test
    @DisplayName("Без узла доступ закрыт")
    void missingNodeDeniesAccess() {

        PermissionService service = new PermissionService(new StubConfig());
        when(player.hasPermission("neycustommessages.reload")).thenReturn(false);

        assertFalse(service.has(player, PermissionNode.RELOAD));

    }

    @Test
    @DisplayName("Null-отправитель при включенной системе не проходит")
    void nullSenderIsDeniedWhenEnabled() {

        assertFalse(new PermissionService(new StubConfig()).has(null, PermissionNode.RELOAD));

    }

    @Test
    @DisplayName("Консоль проверяется так же, как игрок")
    void consoleIsCheckedLikePlayer() {

        when(console.hasPermission("neycustommessages.reload")).thenReturn(true);

        assertTrue(new PermissionService(new StubConfig()).has(console, PermissionNode.RELOAD));

    }

}
