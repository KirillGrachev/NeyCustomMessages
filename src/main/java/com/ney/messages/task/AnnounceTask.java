package com.ney.messages.task;

import com.ney.messages.NeyCustomMessages;
import com.ney.messages.service.interfaces.IAnnouncementService;
import com.ney.messages.util.LoggerHelper;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Level;

public class AnnounceTask extends BukkitRunnable {

    private final IAnnouncementService announcementService;
    private final LoggerHelper loggerHelper;

    public AnnounceTask(NeyCustomMessages plugin,
                        IAnnouncementService announcementService) {
        this.announcementService = announcementService;
        this.loggerHelper = plugin.getLoggerHelper();
    }

    @Override
    public void run() {

        try {
            announcementService.runAnnouncement();
        } catch (Exception e) {
           e.printStackTrace();
        }

    }
}