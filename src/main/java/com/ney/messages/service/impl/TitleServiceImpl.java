package com.ney.messages.service.impl;

import com.ney.messages.service.interfaces.ITitleService;
import com.ney.messages.util.HexColorUtil;
import org.bukkit.entity.Player;

public class TitleServiceImpl implements ITitleService {

    @Override
    public void sendTitle(Player player, String title, String subtitle,
                          int fadeIn, int stay, int fadeOut) {

        if (player == null || !player.isOnline()) {
            return;
        }

        player.sendTitle(
                HexColorUtil.color(title),
                HexColorUtil.color(subtitle),
                fadeIn, stay, fadeOut
        );

    }
}