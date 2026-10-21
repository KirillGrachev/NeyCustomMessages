package eu.neydev.custommessages.config;

import java.util.List;

public record AnnouncementsConfig(
        boolean enabled,
        int interval,
        boolean random,
        List<AnnouncementConfig> messages
) {}