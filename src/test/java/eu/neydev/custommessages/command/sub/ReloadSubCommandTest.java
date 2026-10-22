package eu.neydev.custommessages.command.sub;

import eu.neydev.custommessages.command.CommandContext;
import eu.neydev.custommessages.config.ConfigManager;
import eu.neydev.custommessages.config.type.Announcement;
import eu.neydev.custommessages.config.type.MessageKey;
import eu.neydev.custommessages.config.type.NotificationBundle;
import eu.neydev.custommessages.config.type.PermissionNode;
import eu.neydev.custommessages.service.MessageService;
import eu.neydev.custommessages.support.StubConfig;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Подкоманда reload: конфигурация действительно перечитывается, а
 * отправитель получает подтверждение со временем, числом анонсов и
 * интервалом.
 */
class ReloadSubCommandTest {

    private final ConfigManager configManager = mock(ConfigManager.class);
    private final CommandSender sender = mock(CommandSender.class);
    private final StubConfig config = new StubConfig()
            .withMessage(MessageKey.RELOAD,
                    "reloaded in {time} ms, {announcements} announcements, every {interval}s");

    private ReloadSubCommand subCommand() {
        return new ReloadSubCommand(configManager, new MessageService(config));
    }

    @Test
    @DisplayName("Название и узел права совпадают с plugin.yml")
    void nameAndPermissionMatchPluginYml() {

        assertEquals("reload", subCommand().getName());
        assertEquals(PermissionNode.RELOAD, subCommand().getPermission());

    }

    @Test
    @DisplayName("Конфигурация перечитывается, отправитель получает время и счетчики")
    void reloadsConfigAndReportsCounters() {

        when(configManager.getAnnouncements()).thenReturn(List.of(
                new Announcement("a", NotificationBundle.EMPTY),
                new Announcement("b", NotificationBundle.EMPTY)));
        when(configManager.getAnnouncementInterval()).thenReturn(30);

        assertTrue(subCommand().execute(CommandContext.of(sender), new String[0]));

        verify(configManager).reload();

        ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);

        verify(sender).sendMessage(message.capture());
        assertTrue(message.getValue().matches("reloaded in \\d+ ms, 2 announcements, every 30s"),
                message.getValue());

    }

    @Test
    @DisplayName("Аргументы после названия подкоманды игнорируются")
    void extraArgumentsAreIgnored() {

        assertTrue(subCommand().execute(CommandContext.of(sender), new String[]{"now", "please"}));

        verify(configManager).reload();

    }

    @Test
    @DisplayName("Автодополнения у reload нет")
    void completionIsEmpty() {
        assertEquals(List.of(), subCommand().complete(CommandContext.of(sender), new String[0]));
    }

}
