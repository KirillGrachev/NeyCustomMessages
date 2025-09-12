package com.ney.messages.service.impl;

import com.ney.messages.config.AnnouncementConfig;
import com.ney.messages.config.AnnouncementsConfig;
import com.ney.messages.service.impl.announcement.broadcast.AnnouncementBroadcaster;
import com.ney.messages.service.impl.announcement.scheduler.AnnouncementScheduler;
import com.ney.messages.service.interfaces.IAnnouncementService;
import com.ney.messages.util.LoggerHelper;

import java.util.List;

public class AnnouncementServiceImpl implements IAnnouncementService {

    private final AnnouncementsConfig config;
    private final AnnouncementScheduler scheduler;
    private final AnnouncementBroadcaster broadcaster;
    private final LoggerHelper loggerHelper;

    public AnnouncementServiceImpl(AnnouncementsConfig config,
                                   AnnouncementScheduler scheduler,
                                   AnnouncementBroadcaster broadcaster,
                                   LoggerHelper loggerHelper) {
        this.config = config;
        this.scheduler = scheduler;
        this.broadcaster = broadcaster;
        this.loggerHelper = loggerHelper;
    }

    @Override
    public void runAnnouncement() {

        if (!config.enabled()) return;

        List<AnnouncementConfig> announcements = config.messages();
        if (announcements.isEmpty()) {

            loggerHelper.warn("Announcement","No announcements available to display!");
            return;

        }

        AnnouncementConfig announcement = scheduler.selectNext(announcements, config.random());
        broadcaster.broadcastToAllPlayers(announcement);

    }
}