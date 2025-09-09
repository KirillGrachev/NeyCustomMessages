package com.ney.messages.config;

public record DeathConfig(
        boolean enabled,
        MessageConfig message,
        TitleConfig title,
        SoundConfig sound
) {}