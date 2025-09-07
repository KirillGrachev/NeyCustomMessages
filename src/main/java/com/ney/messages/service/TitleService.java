package com.ney.messages.service;

import org.bukkit.entity.Player;

public class TitleService {

    public void sendTitle(Player player, String title,
                          String subtitle, int fadeIn,
                          int stay, int fadeOut) {

        if (player == null || !player.isOnline()) return;

        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);

    }
}