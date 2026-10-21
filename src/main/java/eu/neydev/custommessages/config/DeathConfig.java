package eu.neydev.custommessages.config;

public record DeathConfig(
        boolean enabled,
        MessageConfig message,
        TitleConfig title,
        SoundConfig sound,
        BossBarConfig bossBar
) {}