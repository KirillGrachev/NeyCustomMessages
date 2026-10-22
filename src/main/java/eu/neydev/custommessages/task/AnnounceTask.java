package eu.neydev.custommessages.task;

import eu.neydev.custommessages.NeyCustomMessages;
import eu.neydev.custommessages.service.AnnouncementService;
import eu.neydev.custommessages.util.LoggerHelper;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

/**
 * Задача ротации анонсов.
 *
 * Исключение внутри прохода глушится и уходит в лог: упавший один раз
 * анонс не должен отменять BukkitTask и оставлять сервер без объявлений
 * до следующего reload.
 */
public class AnnounceTask extends BukkitRunnable {

    private final AnnouncementService announcementService;
    private final LoggerHelper loggerHelper;

    public AnnounceTask(@NotNull NeyCustomMessages plugin,
                        @NotNull AnnouncementService announcementService) {
        this.announcementService = announcementService;
        this.loggerHelper = plugin.getLoggerHelper();
    }

    @Override
    public void run() {

        try {
            announcementService.runOnce();
        } catch (Exception e) {
            loggerHelper.error("AnnounceTask", e, "Failed to run announcement");
        }

    }

}
