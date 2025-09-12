package com.ney.messages.service.impl.announcement.scheduler;

import com.ney.messages.config.AnnouncementConfig;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class AnnouncementScheduler {

    private final Random random = new Random();
    private final AtomicInteger currentIndex = new AtomicInteger(0);

    public AnnouncementConfig selectNext(List<AnnouncementConfig> announcements, boolean isRandom) {
        return isRandom ?
                announcements.get(random.nextInt(announcements.size())) :
                announcements.get(Math.abs(currentIndex.getAndIncrement()) % announcements.size());
    }
}