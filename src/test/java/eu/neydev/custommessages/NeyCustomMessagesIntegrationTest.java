package eu.neydev.custommessages;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import eu.neydev.custommessages.config.type.MessageModule;
import org.bukkit.command.PluginCommand;
import org.bukkit.damage.DamageSource;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Интеграционные тесты на MockBukkit: плагин загружается в живой серверный
 * мок, команды исполняются через command map, события ходят через настоящий
 * диспетчер, ротация анонсов тикает настоящим планировщиком, конфигурация
 * сохраняется в папку плагина.
 */
class NeyCustomMessagesIntegrationTest {

    private ServerMock server;
    private NeyCustomMessages plugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(NeyCustomMessages.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private List<String> drainMessages(PlayerMock player, int count) {

        List<String> messages = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            messages.add(String.valueOf(player.nextMessage()));
        }

        return messages;

    }

    @Test
    @DisplayName("Плагин стартует: сервисы, команды и права на месте")
    void pluginStartsAndWiresEverything() {

        assertTrue(plugin.isEnabled());
        assertNotNull(plugin.getServices());
        assertNotNull(plugin.getConfigManager());
        assertNotNull(plugin.getConsoleService());

        PluginCommand command = plugin.getCommand("ncm");

        assertNotNull(command, "команда из plugin.yml найдена");
        assertNotNull(command.getExecutor(), "executor привязан");
        assertNotNull(command.getTabCompleter(), "tab-completer привязан");

        assertNotNull(server.getPluginManager().getPermission("neycustommessages.reload"));
        assertNotNull(server.getPluginManager().getPermission("neycustommessages.admin"));

    }

    @Test
    @DisplayName("Поставляемый config.yml читается в документированные значения")
    void shippedConfigCarriesDocumentedDefaults() {

        assertTrue(plugin.getDataFolder().toPath().resolve("config.yml").toFile().exists());

        for (MessageModule module : MessageModule.values()) {
            assertTrue(plugin.getConfigManager().isModuleEnabled(module));
        }

        assertTrue(plugin.getConfigManager().areAnnouncementsEnabled());
        assertEquals(15, plugin.getConfigManager().getAnnouncementInterval());
        assertEquals(2, plugin.getConfigManager().getAnnouncements().size(),
                "два анонса из поставки");
        assertTrue(plugin.getConfigManager().getModuleBundle(MessageModule.JOIN)
                .chat().format().contains("joined the server"));

    }

    @Test
    @DisplayName("Вход игрока заменяет ванильное сообщение и рассылает свое")
    void joinEventReplacesTheVanillaMessage() {

        PlayerMock player = server.addPlayer("Steve");

        List<String> messages = drainMessages(player, 1);

        assertTrue(messages.get(0).contains("Steve"), messages.get(0));
        assertTrue(messages.get(0).contains("joined the server"), messages.get(0));

    }

    @Test
    @DisplayName("Выход игрока рассылает сообщение модуля")
    void quitEventDeliversModuleMessage() {

        PlayerMock player = server.addPlayer("Steve");
        drainMessages(player, 1);

        server.getPluginManager().callEvent(new PlayerQuitEvent(player, "Steve left the game"));

        List<String> messages = drainMessages(player, 1);

        assertTrue(messages.get(0).contains("left the server"), messages.get(0));

    }

    @Test
    @DisplayName("Смерть рассылает сообщение с причиной из ваниллы")
    void deathEventDeliversTheCause() {

        PlayerMock player = server.addPlayer("Steve");
        drainMessages(player, 1);

        PlayerDeathEvent event = new PlayerDeathEvent(player, mock(DamageSource.class),
                List.<ItemStack>of(), 0, "Steve fell from a high place");

        server.getPluginManager().callEvent(event);

        List<String> messages = drainMessages(player, 1);

        assertTrue(messages.get(0).contains("Steve fell from a high place"), messages.get(0));
        assertEquals("null", String.valueOf(event.getDeathMessage()),
                "ванильное сообщение снято");

    }

    @Test
    @DisplayName("Ротация анонсов стартует через интервал и доходит до игроков")
    void announcementRotationReachesPlayers() throws IOException {

        PlayerMock player = server.addPlayer("Steve");
        drainMessages(player, 1);

        Files.writeString(plugin.getDataFolder().toPath().resolve("config.yml"), """
                announcements:
                  enabled: true
                  interval: 1
                  random: false
                  list:
                    only:
                      chat:
                        enabled: true
                        format: "&6[AD] rotation works"
                """);
        plugin.getConfigManager().reload();

        server.getScheduler().performTicks(20);

        List<String> messages = drainMessages(player, 1);

        assertTrue(messages.get(0).contains("[AD] rotation works"), messages.get(0));

    }

    @Test
    @DisplayName("Команда reload исполняется через command map")
    void reloadCommandRunsThroughCommandMap() {

        PlayerMock player = server.addPlayer("Steve");
        drainMessages(player, 1);
        player.setOp(true);

        assertTrue(player.performCommand("ncm reload"));

        assertTrue(String.valueOf(player.nextMessage()).contains("Configuration reloaded"));

    }

    @Test
    @DisplayName("Команда info печатает состояние из снапшота")
    void infoCommandReportsTheState() {

        PlayerMock player = server.addPlayer("Steve");
        drainMessages(player, 1);
        player.setOp(true);

        player.performCommand("ncm info");

        List<String> lines = drainMessages(player, 6);

        assertTrue(lines.get(0).contains("Version") && lines.get(0).contains("2.0.0"),
                lines.get(0));
        assertTrue(lines.get(4).contains("2 configured"), lines.get(4));

    }

    @Test
    @DisplayName("Без аргументов команда печатает справку")
    void commandWithoutArgumentsPrintsUsage() {

        PlayerMock player = server.addPlayer("Steve");
        drainMessages(player, 1);

        player.performCommand("ncm");

        assertTrue(String.valueOf(player.nextMessage()).contains("NeyCustomMessages commands:"));

    }

    @Test
    @DisplayName("Не-оператор не проходит reload и не видит его в дополнении")
    void reloadIsDeniedForNonOperators() {

        PlayerMock player = server.addPlayer("Steve");
        drainMessages(player, 1);

        player.performCommand("ncm reload");

        assertTrue(String.valueOf(player.nextMessage()).contains("permission"));

        PluginCommand command = plugin.getCommand("ncm");

        assertNotNull(command);
        assertEquals(List.of(), command.getTabCompleter().onTabComplete(player, command,
                "ncm", new String[]{""}), "reload и info скрыты из дополнения");

    }

    @Test
    @DisplayName("Оператору автодополнение предлагает подкоманды")
    void tabCompletionOffersSubCommandsToOperator() {

        PlayerMock player = server.addPlayer("Steve");
        drainMessages(player, 1);
        player.setOp(true);

        PluginCommand command = plugin.getCommand("ncm");

        assertNotNull(command);
        assertEquals(List.of("reload"), command.getTabCompleter().onTabComplete(player, command,
                "ncm", new String[]{"re"}));

    }

    @Test
    @DisplayName("Мастер-выключатель возвращает ванильные сообщения")
    void masterSwitchRestoresVanillaMessages() throws IOException {

        Files.writeString(plugin.getDataFolder().toPath().resolve("config.yml"),
                "settings:\n  enabled: false\n");
        plugin.getConfigManager().reload();

        PlayerMock player = server.addPlayer("Steve");

        assertFalse(String.valueOf(player.nextMessage()).contains("joined the server"),
                "ванильное сообщение входа осталось");

    }

    @Test
    @DisplayName("Отключение плагина проходит без ошибок и останавливает ротацию")
    void pluginStopsGracefully() {

        server.getPluginManager().disablePlugin(plugin);

        assertFalse(plugin.isEnabled());
        assertFalse(plugin.getServices().getAnnouncementService().isRunning());

    }

}
