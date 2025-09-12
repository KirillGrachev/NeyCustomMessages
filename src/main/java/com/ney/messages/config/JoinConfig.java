package com.ney.messages.config;

public record JoinConfig(
        boolean enabled,
        MessageConfig message,
        TitleConfig title,
        SoundConfig sound,
        BossBarConfig bossBar
) {}