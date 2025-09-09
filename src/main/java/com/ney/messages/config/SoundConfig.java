package com.ney.messages.config;

public record SoundConfig(
        boolean enabled,
        String name,
        float volume,
        float pitch
) {}