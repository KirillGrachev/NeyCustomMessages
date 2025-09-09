package com.ney.messages.task;

import com.ney.messages.service.interfaces.IAnnouncementService;
import org.bukkit.scheduler.BukkitRunnable;

public class AnnounceTask extends BukkitRunnable {

    private final IAnnouncementService announcementService;

    public AnnounceTask(IAnnouncementService announcementService) {
        this.announcementService = announcementService;
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