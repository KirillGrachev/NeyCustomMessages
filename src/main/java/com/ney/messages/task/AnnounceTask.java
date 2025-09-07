package com.ney.messages.task;

import com.ney.messages.service.announcement.AnnouncementService;
import org.bukkit.scheduler.BukkitRunnable;

public class AnnounceTask extends BukkitRunnable {

    private final AnnouncementService announcementService;

    public AnnounceTask(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @Override
    public void run() {
        announcementService.runAnnouncement();
    }
}