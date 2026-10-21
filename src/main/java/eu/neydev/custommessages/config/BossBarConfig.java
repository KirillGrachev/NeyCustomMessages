package eu.neydev.custommessages.config;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;

public record BossBarConfig(
        boolean enabled,
        String text,
        BarColor color,
        BarStyle style,
        int duration,
        boolean progressDecay
) {}