package eu.neydev.custommessages.announcement;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import eu.neydev.custommessages.NeyCustomMessages;
import eu.neydev.custommessages.config.type.Announcement;
import eu.neydev.custommessages.config.type.ConsoleMessage;
import eu.neydev.custommessages.config.type.NotificationBundle;
import eu.neydev.custommessages.service.ConsoleService;
import eu.neydev.custommessages.service.Notifier;
import eu.neydev.custommessages.support.StubConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Ротация анонсов на настоящем планировщике MockBukkit: старт через
 * интервал (не в тик запуска), остановка, перезапуск с новым интервалом,
 * отказы и живучесть задачи при исключении прохода.
 */
class AnnouncementServiceTest {

    private ServerMock server;
    private NeyCustomMessages plugin;
    private final ConsoleService consoleService = mock(ConsoleService.class);
    private final Notifier notifier = mock(Notifier.class);
    private StubConfig config;
    private AnnouncementService service;

    private final Announcement first = new Announcement("first", NotificationBundle.EMPTY);
    private final Announcement second = new Announcement("second", NotificationBundle.EMPTY);

    @BeforeEach
    void setUp() {

        server = MockBukkit.mock();
        plugin = MockBukkit.load(NeyCustomMessages.class);

        // встроенная ротация загруженного плагина мешает тестам - гасим ее
        plugin.getServices().getAnnouncementService().stop();

        config = new StubConfig().withAnnouncements(first, second).withRandomOrder(false);
        service = new AnnouncementService(plugin, config, consoleService,
                new AnnouncementSelector(new Random(3)), notifier);

    }

    @AfterEach
    void tearDown() {
        service.stop();
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Старт ставит задачу, первый проход - через полный интервал")
    void firstRunIsDelayedByInterval() {

        config.withInterval(2).withRandomOrder(false);
        service.start();

        assertTrue(service.isRunning());

        server.getScheduler().performTicks(19);
        verify(notifier, never()).broadcast(any());

        server.getScheduler().performTicks(21);
        verify(notifier).broadcast(first.bundle());

    }

    @Test
    @DisplayName("Круговая ротация идет по списку")
    void rotationCyclesThroughList() {

        config.withInterval(1).withRandomOrder(false);
        service.start();

        server.getScheduler().performTicks(20);
        server.getScheduler().performTicks(20);
        server.getScheduler().performTicks(20);

        ArgumentCaptor<NotificationBundle> bundles =
                ArgumentCaptor.forClass(NotificationBundle.class);

        verify(notifier, times(3)).broadcast(bundles.capture());
        assertEquals(List.of(first.bundle(), second.bundle(), first.bundle()),
                bundles.getAllValues().stream().toList());

    }

    @Test
    @DisplayName("Остановка снимает задачу")
    void stopCancelsRotation() {

        config.withInterval(1);
        service.start();
        service.stop();

        assertFalse(service.isRunning());

        server.getScheduler().performTicks(100);
        verify(notifier, never()).broadcast(any());

    }

    @Test
    @DisplayName("Выключенный плагин или анонсы не запускают ротацию")
    void disabledConfigDoesNotStart() {

        config.withPluginEnabled(false);
        service.start();
        assertFalse(service.isRunning());

        config.withPluginEnabled(true).withAnnouncementsEnabled(false);
        service.start();
        assertFalse(service.isRunning());

    }

    @Test
    @DisplayName("Пустой список дает предупреждение в консоль и не запускает задачу")
    void emptyListWarnsAndDoesNotStart() {

        config.withAnnouncements();
        service.start();

        assertFalse(service.isRunning());
        verify(consoleService).log(ConsoleMessage.NO_ANNOUNCEMENTS);

    }

    @Test
    @DisplayName("Перезапуск подхватывает новый интервал")
    void restartPicksUpNewInterval() {

        config.withInterval(10);
        service.start();

        config.withInterval(1);
        service.restart();

        server.getScheduler().performTicks(20);
        verify(notifier).broadcast(first.bundle());

    }

    @Test
    @DisplayName("Повторный старт идемпотентен: одна задача, а не две")
    void startIsIdempotent() {

        config.withInterval(5);
        service.start();
        service.start();

        server.getScheduler().performTicks(100);

        verify(notifier).broadcast(any());

    }

    @Test
    @DisplayName("Выключение конфига без перезапуска глушит проходы")
    void disabledConfigSilencesRunningRotation() {

        config.withInterval(1);
        service.start();

        config.withPluginEnabled(false);
        server.getScheduler().performTicks(40);

        verify(notifier, never()).broadcast(any());
        assertTrue(service.isRunning(), "задача жива - конфигурация может включиться обратно");

    }

    @Test
    @DisplayName("Опустевший список анонсов пропускается молча")
    void emptiedListIsSkippedSilently() {

        config.withInterval(1);
        service.start();

        config.withAnnouncements();
        server.getScheduler().performTicks(40);

        verify(notifier, never()).broadcast(any());

    }

    @Test
    @DisplayName("Исключение прохода глушится, ротация продолжается")
    void failureDoesNotKillRotation() {

        config.withInterval(1);
        doThrow(new IllegalStateException("notifier exploded"))
                .doNothing()
                .when(notifier).broadcast(any());

        service.start();

        server.getScheduler().performTicks(20);
        server.getScheduler().performTicks(20);

        verify(notifier, times(2)).broadcast(any());
        assertTrue(service.isRunning(), "задача не отменена исключением");

    }

}
