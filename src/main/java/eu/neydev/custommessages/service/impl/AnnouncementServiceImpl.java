package eu.neydev.custommessages.service.impl;

import eu.neydev.custommessages.config.CustomMessagesConfig;
import eu.neydev.custommessages.config.type.Announcement;
import eu.neydev.custommessages.service.impl.announcement.broadcast.AnnouncementBroadcaster;
import eu.neydev.custommessages.service.impl.announcement.scheduler.AnnouncementScheduler;
import eu.neydev.custommessages.service.interfaces.IAnnouncementService;

import java.util.List;

public class AnnouncementServiceImpl implements IAnnouncementService {

    private final CustomMessagesConfig config;
    private final AnnouncementScheduler scheduler;
    private final AnnouncementBroadcaster broadcaster;

    public AnnouncementServiceImpl(CustomMessagesConfig config,
                                   AnnouncementScheduler scheduler,
                                   AnnouncementBroadcaster broadcaster) {
        this.config = config;
        this.scheduler = scheduler;
        this.broadcaster = broadcaster;
    }

    @Override
    public void runAnnouncement() {

        if (!config.isPluginEnabled() || !config.areAnnouncementsEnabled()) {
            return;
        }

        List<Announcement> announcements = config.getAnnouncements();

        if (announcements.isEmpty()) {
            return;
        }

        Announcement announcement =
                scheduler.selectNext(announcements, config.isRandomAnnouncementOrder());
        broadcaster.broadcastToAllPlayers(announcement);

    }

}
