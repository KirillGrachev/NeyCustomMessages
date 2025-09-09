package com.ney.messages.config;

import net.md_5.bungee.api.chat.ClickEvent;

public record AnnouncementConfig(
        MessageConfig message,
        TitleConfig title,
        SoundConfig sound,
        BossBarConfig bossBar,
        String hoverText,
        ClickEvent.Action clickAction,
        String clickValue
) {}