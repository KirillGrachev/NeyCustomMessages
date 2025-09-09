package com.ney.messages.config;

public record QuitConfig(
        boolean enabled,
        MessageConfig message,
        TitleConfig title,
        SoundConfig sound
) {}