package eu.neydev.custommessages.task;

import eu.neydev.custommessages.NeyCustomMessages;
import eu.neydev.custommessages.service.interfaces.IAnnouncementService;
import eu.neydev.custommessages.util.LoggerHelper;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class AnnounceTask extends BukkitRunnable {

    private final IAnnouncementService announcementService;
    private final LoggerHelper loggerHelper;

    public AnnounceTask(@NotNull NeyCustomMessages plugin,
                        IAnnouncementService announcementService) {
        this.announcementService = announcementService;
        this.loggerHelper = plugin.getLoggerHelper();
    }

    @Override
    public void run() {

        try {
            announcementService.runAnnouncement();
        } catch (Exception e) {
            loggerHelper.error("AnnounceTask", e, "Failed to run announcement");
        }

    }
}