package eu.neydev.custommessages.config;

public record QuitConfig(
        boolean enabled,
        MessageConfig message,
        TitleConfig title,
        SoundConfig sound,
        BossBarConfig bossBar
) {}