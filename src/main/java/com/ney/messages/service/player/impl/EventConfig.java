package com.ney.messages.service.player.impl;

public record EventConfig(String enabled,
                          String messageEnabled,
                          String messageFormat,
                          String titleEnabled,
                          String title,
                          String subtitle,
                          String fadeIn,
                          String stay,
                          String fadeOut,
                          String soundEnabled,
                          String soundName,
                          String soundVolume,
                          String soundPitch) {}