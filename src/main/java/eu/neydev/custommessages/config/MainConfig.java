package eu.neydev.custommessages.config;

public record MainConfig(
        DeathConfig death,
        JoinConfig join,
        QuitConfig quit,
        AnnouncementsConfig announcements
) {}