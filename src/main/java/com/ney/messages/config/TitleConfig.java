package com.ney.messages.config;

public record TitleConfig(
        boolean enabled,
        String title,
        String subtitle,
        int fadeIn,
        int stay,
        int fadeOut
) {}