package eu.neydev.custommessages.announcement;

import eu.neydev.custommessages.config.CustomMessagesConfig;
import eu.neydev.custommessages.config.type.Announcement;
import eu.neydev.custommessages.config.type.ConsoleMessage;
import eu.neydev.custommessages.service.ConsoleService;
import eu.neydev.custommessages.service.Notifier;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.logging.Level;

/**
 * Ротация анонсов: задача, выбор и рассылка в одном месте.
 *
 * Сервис сам владеет своим BukkitTask (start/stop/restart): в 1.x ручкой
 * задачи жонглировал менеджер жизненного цикла, и reload мог оставить
 * сервер без ротации, если пересборка контейнера падала между отменой и
 * новым стартом. Первый проход выполняется ЧЕРЕЗ интервал, а не в тик
 * запуска: в 1.x runTaskTimer с нулевой задержкой рассылал объявление
 * в момент, когда игроки еще грузились после старта сервера, и повторял
 * это на каждый reload.
 *
 * Список анонсов читается живьем из снапшота на каждый проход: reload
 * подменяет снапшот атомарно, и ротация подхватывает новый список сама;
 * перезапуск задачи нужен только когда изменился интервал.
 */
public class AnnouncementService {

    private final Plugin plugin;
    private final CustomMessagesConfig config;
    private final ConsoleService consoleService;
    private final AnnouncementSelector selector;
    private final Notifier notifier;

    private @Nullable BukkitTask task;

    public AnnouncementService(@NotNull Plugin plugin,
                               @NotNull CustomMessagesConfig config,
                               @NotNull ConsoleService consoleService,
                               @NotNull AnnouncementSelector selector,
                               @NotNull Notifier notifier) {
        this.plugin = plugin;
        this.config = config;
        this.consoleService = consoleService;
        this.selector = selector;
        this.notifier = notifier;
    }

    /**
     * Запускает ротацию, если она включена и список не пуст.
     * Предыдущая задача отменяется: start идемпотентен.
     */
    public void start() {

        stop();

        if (!config.isPluginEnabled() || !config.areAnnouncementsEnabled()) {
            return;
        }

        if (config.getAnnouncements().isEmpty()) {

            consoleService.log(ConsoleMessage.NO_ANNOUNCEMENTS);
            return;

        }

        long intervalTicks = config.getAnnouncementInterval() * 20L;

        task = plugin.getServer().getScheduler()
                .runTaskTimer(plugin, this::tick, intervalTicks, intervalTicks);

    }

    /**
     * Останавливает ротацию, если она идет.
     */
    public void stop() {

        if (task != null) {

            task.cancel();
            task = null;

        }

    }

    /**
     * Перезапускает ротацию с текущими настройками: вызывается после reload.
     */
    public void restart() {
        start();
    }

    /**
     * Идет ли ротация сейчас.
     */
    public boolean isRunning() {
        return task != null;
    }

    private void tick() {

        try {

            runOnce();

        } catch (RuntimeException exception) {
            // Исключение одного прохода не должно отменять повторяющуюся
            // задачу: ротация продолжается, ошибка уходит в лог
            plugin.getLogger().log(Level.SEVERE,
                    "Announcement run failed, the rotation continues", exception);
        }

    }

    private void runOnce() {

        if (!config.isPluginEnabled() || !config.areAnnouncementsEnabled()) {
            return;
        }

        List<Announcement> announcements = config.getAnnouncements();

        if (announcements.isEmpty()) {
            return;
        }

        notifier.broadcast(selector.selectNext(announcements,
                config.isRandomAnnouncementOrder()).bundle());

    }

}
