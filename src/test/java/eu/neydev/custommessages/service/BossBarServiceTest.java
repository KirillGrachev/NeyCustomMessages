package eu.neydev.custommessages.service;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import eu.neydev.custommessages.config.type.BossBarSettings;
import org.bukkit.Server;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Босс-бары на живом планировщике MockBukkit и рукописных моках бара:
 * один бар на игрока, снятие по длительности, затухание прогресса по
 * тикам и полная очистка на reload. ServerMock не ведет реестр созданных
 * баров, поэтому бары собираются из ответов createBossBar.
 */
class BossBarServiceTest {

    private ServerMock serverMock;
    private final List<BossBar> createdBars = new ArrayList<>();
    private BossBarService service;
    private PlayerMock player;

    /** Прогресс каждого созданного бара, параллельно списку createdBars. */
    private final List<double[]> progresses = new ArrayList<>();

    @BeforeEach
    void setUp() {

        serverMock = MockBukkit.mock();
        player = serverMock.addPlayer("Steve");

        Plugin plugin = mock(Plugin.class);
        Server server = mock(Server.class);

        when(plugin.getServer()).thenReturn(server);
        when(server.getScheduler()).thenReturn(serverMock.getScheduler());
        when(server.createBossBar(anyString(), any(BarColor.class), any(BarStyle.class)))
                .thenAnswer(invocation -> {

                    BossBar bar = mock(BossBar.class);
                    List<Player> viewers = new ArrayList<>();
                    double[] progress = {1.0};

                    doAnswer(add -> {
                        viewers.add(add.getArgument(0));
                        return null;
                    }).when(bar).addPlayer(any(Player.class));

                    doAnswer(remove -> {
                        viewers.remove(remove.getArgument(0));
                        return null;
                    }).when(bar).removePlayer(any(Player.class));

                    doAnswer(clear -> {
                        viewers.clear();
                        return null;
                    }).when(bar).removeAll();

                    when(bar.getPlayers()).thenReturn(viewers);
                    when(bar.getProgress()).thenAnswer(get -> progress[0]);
                    doAnswer(set -> {
                        progress[0] = set.getArgument(0);
                        return null;
                    }).when(bar).setProgress(anyDouble());

                    createdBars.add(bar);
                    progresses.add(progress);
                    return bar;

                });

        service = new BossBarService(plugin);

    }

    @AfterEach
    void tearDown() {
        service.removeAll();
        MockBukkit.unmock();
    }

    private BossBarSettings settings(int duration, boolean decay) {
        return new BossBarSettings(true, "text", BarColor.GREEN, BarStyle.SOLID, duration, decay);
    }

    @Test
    @DisplayName("Показанный бар виден игроку")
    void shownBarReachesThePlayer() {

        service.show(player, "text", settings(5, false));

        assertEquals(1, createdBars.size());
        assertTrue(createdBars.get(0).getPlayers().contains(player));

    }

    @Test
    @DisplayName("Второй показ заменяет предыдущий бар")
    void secondShowReplacesTheFirst() {

        service.show(player, "first", settings(5, false));
        BossBar firstBar = createdBars.get(0);

        service.show(player, "second", settings(5, false));

        assertFalse(firstBar.getPlayers().contains(player), "старый бар снят");
        assertEquals(2, createdBars.size(), "новый бар создан");
        assertTrue(createdBars.get(1).getPlayers().contains(player));

    }

    @Test
    @DisplayName("Бар без затухания снимается ровно через длительность")
    void barIsRemovedAfterDuration() {

        service.show(player, "text", settings(3, false));

        serverMock.getScheduler().performTicks(59);
        assertTrue(createdBars.get(0).getPlayers().contains(player), "еще висит");

        serverMock.getScheduler().performTicks(2);
        assertFalse(createdBars.get(0).getPlayers().contains(player), "снята после 3 секунд");

    }

    @Test
    @DisplayName("Затухание уменьшает прогресс каждый тик и снимает бар в нуле")
    void decayShrinksProgressAndRemovesAtZero() {

        service.show(player, "text", settings(4, true));

        serverMock.getScheduler().performTicks(40);
        assertEquals(0.5, progresses.get(0)[0], 0.02,
                "половина длительности - половина прогресса");

        serverMock.getScheduler().performTicks(45);
        assertFalse(createdBars.get(0).getPlayers().contains(player),
                "бар снят по достижении нуля");

    }

    @Test
    @DisplayName("Оффлайн-игрок бар не получает")
    void offlinePlayerIsSkipped() {

        Player offline = mock(Player.class);
        when(offline.isOnline()).thenReturn(false);

        service.show(offline, "text", settings(5, false));

        assertEquals(0, createdBars.size());

    }

    @Test
    @DisplayName("RemoveAll снимает бары у всех игроков")
    void removeAllClearsEverything() {

        PlayerMock second = serverMock.addPlayer("Alex");

        service.show(player, "one", settings(5, false));
        service.show(second, "two", settings(5, false));

        service.removeAll();

        for (BossBar bar : createdBars) {
            assertTrue(bar.getPlayers().isEmpty(), "бары пусты после removeAll");
        }

    }

    @Test
    @DisplayName("Снятие без бара и с null-игроком проходит молча")
    void removeWithoutBarIsNoop() {

        service.remove(player);
        service.remove(null);

        assertEquals(0, createdBars.size());

    }

    @Test
    @DisplayName("Затухающий бар снимается, когда игрок вышел")
    void decayTaskRemovesBarWhenPlayerGoesOffline() {

        Player leaving = mock(Player.class);
        java.util.UUID id = java.util.UUID.randomUUID();

        when(leaving.isOnline()).thenReturn(true);
        when(leaving.getUniqueId()).thenReturn(id);

        service.show(leaving, "text", settings(10, true));
        assertEquals(1, createdBars.size());

        when(leaving.isOnline()).thenReturn(false);
        serverMock.getScheduler().performTicks(2);

        assertTrue(createdBars.get(0).getPlayers().isEmpty(),
                "бар снят сразу после выхода игрока");

    }

    @Test
    @DisplayName("Ручное снятие отменяет задачу удаления")
    void manualRemoveCancelsTheTask() {

        service.show(player, "text", settings(5, false));
        service.remove(player);

        assertFalse(createdBars.get(0).getPlayers().contains(player));

        serverMock.getScheduler().performTicks(200);

        assertEquals(1, createdBars.size(), "новых баров не появилось");

    }

}
