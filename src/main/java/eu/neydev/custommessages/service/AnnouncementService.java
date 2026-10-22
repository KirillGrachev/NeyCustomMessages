package eu.neydev.custommessages.service;

import eu.neydev.custommessages.config.CustomMessagesConfig;
import eu.neydev.custommessages.config.type.Announcement;
import eu.neydev.custommessages.service.impl.announcement.scheduler.AnnouncementScheduler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Один проход ротации анонсов.
 *
 * Читает список живьем из снапшота конфигурации на каждый тик задачи:
 * reload подменяет снапшот атомарно, и ротация подхватывает новый список
 * без пересборки сервисов. Пустой список просто пропускается -
 * предупреждение об этом печатается один раз при старте задачи.
 */
public class AnnouncementService {

    private final CustomMessagesConfig config;
    private final AnnouncementScheduler scheduler;
    private final Notifier notifier;

    public AnnouncementService(@NotNull CustomMessagesConfig config,
                               @NotNull AnnouncementScheduler scheduler,
                               @NotNull Notifier notifier) {
        this.config = config;
        this.scheduler = scheduler;
        this.notifier = notifier;
    }

    /**
     * Выполняет один проход: выбирает следующий анонс и рассылает его.
     */
    public void runOnce() {

        if (!config.isPluginEnabled() || !config.areAnnouncementsEnabled()) {
            return;
        }

        List<Announcement> announcements = config.getAnnouncements();

        if (announcements.isEmpty()) {
            return;
        }

        notifier.broadcast(scheduler.selectNext(announcements,
                config.isRandomAnnouncementOrder()).bundle());

    }

}
