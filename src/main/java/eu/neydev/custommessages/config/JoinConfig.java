package eu.neydev.custommessages.config;

public record JoinConfig(
        boolean enabled,
        MessageConfig message,
        TitleConfig title,
        SoundConfig sound,
        BossBarConfig bossBar
) {}