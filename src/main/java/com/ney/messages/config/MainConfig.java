package com.ney.messages.config;

public record MainConfig(
        DeathConfig death,
        JoinConfig join,
        QuitConfig quit,
        AnnouncementsConfig announcements
) {}