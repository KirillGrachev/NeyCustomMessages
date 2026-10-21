package eu.neydev.custommessages.config;

public record MessageConfig(
        boolean enabled,
        String format
) {}