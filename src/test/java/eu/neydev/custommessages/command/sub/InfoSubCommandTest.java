package eu.neydev.custommessages.command.sub;

import eu.neydev.custommessages.command.CommandContext;
import eu.neydev.custommessages.config.type.Announcement;
import eu.neydev.custommessages.config.type.MessageKey;
import eu.neydev.custommessages.config.type.MessageModule;
import eu.neydev.custommessages.config.type.NotificationBundle;
import eu.neydev.custommessages.config.type.PermissionNode;
import eu.neydev.custommessages.service.MessageService;
import eu.neydev.custommessages.support.StubConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Подкоманда info: диагностика собирается из снапшота конфигурации,
 * булевы значения подставляются словами из той же конфигурации.
 */
class InfoSubCommandTest {

    private static final String TEMPLATE =
            "v={version} j={join} q={quit} d={death} a={announcements_state} "
                    + "c={count} i={interval} m={mode} p={permissions}";

    private final Plugin plugin = mock(Plugin.class);
    private final CommandSender sender = mock(CommandSender.class);
    private final StubConfig config = new StubConfig()
            .withMessage(MessageKey.INFO, TEMPLATE)
            .withMessage(MessageKey.STATE_ON, "on")
            .withMessage(MessageKey.STATE_OFF, "off");

    @BeforeEach
    void setUp() {
        when(plugin.getDescription()).thenReturn(new PluginDescriptionFile(
                "NeyCustomMessages", "2.0.0", "eu.neydev.custommessages.NeyCustomMessages"));
    }

    private InfoSubCommand subCommand() {
        return new InfoSubCommand(plugin, config, new MessageService(config));
    }

    private String sent() {

        ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);

        verify(sender).sendMessage(message.capture());
        return message.getValue();

    }

    @Test
    @DisplayName("Название и узел права совпадают с plugin.yml")
    void nameAndPermissionMatchPluginYml() {

        assertEquals("info", subCommand().getName());
        assertEquals(PermissionNode.RELOAD, subCommand().getPermission());

    }

    @Test
    @DisplayName("Дефолты стаба совпадают с поставляемым конфигом")
    void reportsEverySettingFromTheSnapshot() {

        assertTrue(subCommand().execute(CommandContext.of(sender), new String[0]));

        assertEquals("v=2.0.0 j=on q=on d=on a=on c=0 i=15 m=random p=on", sent());

    }

    @Test
    @DisplayName("Число анонсов и интервал читаются из снапшота")
    void reportsAnnouncementCounters() {

        config.withAnnouncements(
                        new Announcement("a", NotificationBundle.EMPTY),
                        new Announcement("b", NotificationBundle.EMPTY))
                .withInterval(30);

        subCommand().execute(CommandContext.of(sender), new String[0]);

        assertTrue(sent().contains("c=2 i=30"), sent());

    }

    @Test
    @DisplayName("Круговой порядок печатается своим словом")
    void roundRobinModeIsReported() {

        config.withRandomOrder(false);

        subCommand().execute(CommandContext.of(sender), new String[0]);

        assertTrue(sent().contains("m=round-robin"), sent());

    }

    @Test
    @DisplayName("Каждый выключенный флаг печатается словом state_off")
    void disabledFlagsUseStateOff() {

        config.withModuleEnabled(MessageModule.JOIN, false)
                .withModuleEnabled(MessageModule.QUIT, false)
                .withModuleEnabled(MessageModule.DEATH, false)
                .withAnnouncementsEnabled(false)
                .withPermissionsEnabled(false);

        subCommand().execute(CommandContext.of(sender), new String[0]);

        assertEquals("v=2.0.0 j=off q=off d=off a=off c=0 i=15 m=random p=off", sent());

    }

    @Test
    @DisplayName("Выключенное слово-фрагмент дает пустую подстановку")
    void disabledStateFragmentBecomesEmpty() {

        config.withMessage(MessageKey.STATE_ON);

        subCommand().execute(CommandContext.of(sender), new String[0]);

        assertTrue(sent().contains("j= "), sent());

    }

}
